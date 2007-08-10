/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package nexus;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Represents Nexus trees blocks.
 * 
 * @author Richard Holland
 * @author Tobias Thierer
 * @author Jim Balhoff
 * @since 1.6
 */
public class TreesBlock extends NexusBlock.Abstract {

	/**
	 * A constant representing the name of Trees blocks.
	 */
	public static final String TREES_BLOCK = "TREES";

	private Map translations = new LinkedHashMap();

	private List comments = new ArrayList();

	private Map trees = new LinkedHashMap();

	private UndirectedGraph unweighted = new SimpleGraph(DefaultEdge.class);

	private WeightedGraph weighted =  new SimpleWeightedGraph(DefaultWeightedEdge.class);

	/**
	 * A simple representation of a Newick tree as a single string.
	 */
	public static class NewickTreeString {
		private String rootType;

		private String treeString;

		private boolean starred;

		/**
		 * Make the tree (un)rooted.
		 * 
		 * @param rootType
		 *            'U' for unrooted, 'R' for rooted, <tt>null</tt> for
		 *            unsure.
		 */
		public void setRootType(final String rootType) {
			this.rootType = rootType;
		}

		/**
		 * Set the Newick string describing the tree.
		 */
		public void setTreeString(final String treeString) {
			this.treeString = treeString;
		}

		/**
		 * Sets whether this tree has a star before it's name.
		 * 
		 * @param starred
		 *            <tt>true</tt> if it has one.
		 */
		public void setStarred(boolean starred) {
			this.starred = starred;
		}

		/**
		 * Tests whether this tree has a star before it's name.
		 * 
		 * @return starred <tt>true</tt> if it has one.
		 */
		public boolean isStarred() {
			return this.starred;
		}

		/**
		 * See if the tree is rooted.
		 * 
		 * @return 'U' for unrooted, 'R' for rooted, <tt>null</tt> for unsure.
		 */
		public String getRootType() {
			return this.rootType;
		}

		/**
		 * Get the Newick string describing the tree.
		 * 
		 * @return the tree string.
		 */
		public String getTreeString() {
			return this.treeString;
		}
	}

	/**
	 * Delegates to NexusBlock.Abstract constructor using TreesBlock.TREES_BLOCK
	 * as the name.
	 */
	public TreesBlock() {
		super(TreesBlock.TREES_BLOCK);
	}

	/**
	 * Add a translation.
	 * 
	 * @param label
	 *            the label to add.
	 * @param taxa
	 *            the taxa name this label will represent.
	 */
	public void addTranslation(final String label, final String taxa) {
		this.translations.put(label, taxa);
	}

	/**
	 * Removes the given translation.
	 * 
	 * @param label
	 *            the label to remove.
	 */
	public void removeTranslation(final String label) {
		this.translations.remove(label);
	}

	/**
	 * Checks to see if we contain the given translation.
	 * 
	 * @param label
	 *            the label to check for.
	 * @return <tt>true</tt> if we already contain it.
	 */
	public boolean containsTranslation(final String label) {
		return this.translations.containsKey(label);
	}

	/**
	 * Get the translations added so far.
	 * 
	 * @return the translations added so far.
	 */
	public Map getTranslations() {
		return this.translations;
	}

	/**
	 * Adds a tree.
	 * 
	 * @param label
	 *            the label to give the tree.
	 * @param tree
	 *            the tree to add.
	 */
	public void addTree(final String label, final NewickTreeString tree) {
		this.trees.put(label, tree);
	}

	/**
	 * Removes a tree.
	 * 
	 * @param label
	 *            the label to remove.
	 */
	public void removeTree(final String label) {
		this.trees.remove(label);
	}

	/**
	 * Checks to see if we contain the given tree.
	 * 
	 * @param label
	 *            the label to check for.
	 * @return <tt>true</tt> if we already contain it.
	 */
	public boolean containsTree(final String label) {
		return this.trees.containsKey(label);
	}

	/**
	 * Returns all trees.
	 * 
	 * @return all the selected trees.
	 */
	public Map getTrees() {
		return this.trees;
	}

	/**
	 * Returns a tree for given label
	 * @param label
             * 	 the label to select.
	 *
	 * @return selected tree.
             */
     	public Object getTree(final String label) {
		return this.trees.get(label);
	}

	/**
	 * Add a tree, converting unweighted graph (JGraphT) to NewickString
	 *
	 * @param label
	 * 		  the label to add
	 *
	 * @param treegraph
	 * 		  the treegraph to convert.
     	 */
	public void addTree(final String label, UndirectedGraph treegraph) {
	
		final NewickTreeString tree = new NewickTreeString();
		String temp = treegraph.toString();
		String [] tokens = null;
	
		// extract the tree string part from JGraphT
		tokens = temp.split("\\[");            
		temp = tokens[2];
		tokens = temp.split("\\]");	
		temp = tokens[0];
		
		// parse all vertices and store it in the string array tokens
		temp = temp.replaceAll("{", "");
		temp = temp.replaceAll("}", "");
		temp = temp.replaceAll(" ", "");
		tokens = temp.split(",");               
		temp = "";
		
		for(int i = 0 ; i < tokens.length; i = i + 4){
			if( tokens[i].matches("p[0-9]") == false && tokens[i+3].matches("p[0-9]")== false && tokens[i].equals(tokens[i+3]) == false){
				temp = "(" + tokens[i] +", " + tokens[i+3] + ")" ;	
				for(int j = 0; j < tokens.length; j++){
					if(tokens[j].equals(tokens[i+1]) && tokens[j].equals(tokens[i+2])  && j != i+1 && j!= i+2){
						if(j > i+3) 
							tokens[j] = temp;
						else if(j < i) 
							temp = "(" + tokens[j-3] + ", " + temp + ")";		
					}
				}
			}
		}	 

		tree.setTreeString(temp);                           
		this.trees.put(label, tree);
	}

	/**
	 * Add a tree, converting weighted graph (JGraphT) to NewickString
	 *
	 * @param label
	 * 		  the label to add
	 *
	 * @param treegraph
	 * 		  the treegraph to convert.
             */
	public void addTree(final String label, WeightedGraph treegraph) {
	
		final NewickTreeString tree = new NewickTreeString();
		String temp = treegraph.toString();
		String [] tokens = null;
	
		// extract the tree string part from JGraphT
		tokens = temp.split("\\[");            
		temp = tokens[2];
		tokens = temp.split("\\]");	
		temp = tokens[0];
		
		// parse all vertices and store it in the string array tokens
		temp = temp.replaceAll("{", "");
		temp = temp.replaceAll("}", "");
		temp = temp.replaceAll(" ", "");
		tokens = temp.split(",");               
		temp = "";

		for(int i = 0 ; i < tokens.length; i = i + 4){	
			
			if( tokens[i].matches("p[0-9]") == false && tokens[i+3].matches("p[0-9]")== false && tokens[i].equals(tokens[i+3]) == false){
	
				if(tokens[i].startsWith("(") == false && tokens[i+3].startsWith("(") == false)
					temp = "(" + tokens[i]+ ":"+ treegraph.getEdgeWeight(treegraph.getEdge(tokens[i], tokens[i+1])) +", " + tokens[i+3] + ":"+ treegraph.getEdgeWeight(treegraph.getEdge(tokens[i+2], tokens[i+3])) + ")" ;
				else if (tokens[i].startsWith("(") && tokens[i+3].startsWith("(") == false)
					temp = "(" + tokens[i] +", " + tokens[i+3] + ":"+ treegraph.getEdgeWeight(treegraph.getEdge(tokens[i+2], tokens[i+3])) + ")" ;
				else if (tokens[i].startsWith("(") == false && tokens[i+3].startsWith("("))
					temp = "(" + tokens[i]+ ":"+ treegraph.getEdgeWeight(treegraph.getEdge(tokens[i], tokens[i+1])) +", " + tokens[i+3] +  ")" ;
				else if (tokens[i].startsWith("(")  && tokens[i+3].startsWith("("))
					temp = "(" + tokens[i]+ ", " + tokens[i+3] +  ")" ;
												
				for(int j = 0; j < tokens.length; j++){
					if(tokens[j].matches(tokens[i+1]) && tokens[j].matches(tokens[i+2]) && j != i+1 && j != i+2){
						
						double weight = 0.0;
						if(j%4 == 0)
							weight = treegraph.getEdgeWeight(treegraph.getEdge(tokens[j], tokens[j+1]));
						else if(j%4 == 3)
							weight = treegraph.getEdgeWeight(treegraph.getEdge(tokens[j], tokens[j-1]));
						
						if(j > i+3) 
							tokens[j] = temp + ":" + weight; 
						else if(j < i) 
							temp = "(" + tokens[j-3] + ":"+ treegraph.getEdgeWeight(treegraph.getEdge(tokens[j-3], tokens[j-2])) + ", " + temp + ":" + weight + ")";	
					}
				}
			}
		} 
		
		tree.setTreeString(temp);                           
		this.trees.put(label, tree);
	}	
	
	/**
	 * Get given (NewieckString) tree by label, converts it to unweighted graph (JGraphT).
             *
	 * @param label
	 * 		 label for tree selection 
	 *
	 * @return converted tree as undirectedGraph
	 */
	public UndirectedGraph getTreeAsJGraphT(final String label) {
	
		String temp, v1, v2, v3;
		String [] tokens;
		int len = 0, p_index=0; 
		Object s_temp1, s_temp2, s_temp3;
		TreesBlock.NewickTreeString t = new TreesBlock.NewickTreeString();
		Stack stack = new Stack();
	
		t = (TreesBlock.NewickTreeString) this.trees.get(label);

		temp = t.getTreeString();
		len = temp.length();                  
		tokens = temp.split("");             
		temp = "";
		
		for(int i = 0; i <= len; i++){
			if(tokens[i].equals("(")){
				p_index++;
			}
		}

		for(int i = 0; i <= len; i++)              
		{
			if( tokens[i].equals(",") ){          
				//push into stack if it is a word, or comma
				stack.push("p" + p_index);
				p_index--;
			}else if ( tokens[i].equals("(") || tokens[i].equals(" ") ){    
				// ignore "(" or " "
			}else if(tokens[i].equals(")")){	  
			 	//pop 3 elements if you see ")"					
				try{
					s_temp3 = stack.pop();    
					v3 = s_temp3.toString(); 
										
					try{
						s_temp2 = stack.pop();
						v2 = s_temp2.toString();
				
						try{
							s_temp1 = stack.pop();
							v1 = s_temp1.toString();
									
							this.unweighted.addVertex(v1);
							this.unweighted.addVertex(v2);
							this.unweighted.addVertex(v3);	
							this.unweighted.addEdge(v1,v2);
							this.unweighted.addEdge(v2,v3);		
										
							stack.push(v2);
						}catch(EmptyStackException e){}
					}catch(EmptyStackException e){}												
				}catch(EmptyStackException e){}
									
			}else{
				// if it is a letter, concatenate for the name, and push it to the stack 								
				if(tokens[i].equals(" ")){
					//ignore
				}else if(tokens[i+1].equals("(") || tokens[i+1].equals(")") || tokens[i+1].equals(",")) {
					temp = temp + tokens[i];
					stack.push(temp);
					temp = "";
				}else{
					temp = temp + tokens[i];
				}
			}
		}			
		return this.unweighted;
	}

	/**
	 * Get given (NewieckString) tree by label, converts it to weighted graph (JGraphT).
             *
	 * @param label
	 * 		 label for tree selection 
	 *
	 * @return converted tree as undirectedGraph
	 */
	public WeightedGraph getTreeAsWeightedJGraphT(final String label) {
	
		int len = 0, p_index=0; 
		String temp, v1, v2, v3, w1, w3;
		String [] tokens, temp_token;
		Object s_temp1, s_temp2, s_temp3;
		Object weight1, weight3;
		Stack stack = new Stack();
		Stack weight_stack = new Stack();
	
		TreesBlock.NewickTreeString t = new TreesBlock.NewickTreeString();
	
		t = (TreesBlock.NewickTreeString) this.trees.get(label);

		temp = t.getTreeString();
		len = temp.length();                  
		tokens = temp.split("");             
		temp = "";
		
		for(int i = 0; i <= len; i++)              
		{
			if( tokens[i].equals(",") ){          
				//push into stack if it is a word, or comma
				stack.push("p" + p_index);
				p_index++;
			}else if ( tokens[i].equals("(") || tokens[i].equals(" ") ){    
				// ignore "(" or " "
			}else if(tokens[i].equals(")")){	  
			 	//pop 3 elements if you see ")"		
				// TODO: Find a smarter way to pop the number of elements between the enclosing parens,
				// so we can deal with polytomies.
				try{
					s_temp3 = stack.pop();    
					v3 = s_temp3.toString(); 
					weight3 = weight_stack.pop();
					w3 = weight3.toString();
					
					try{
						s_temp2 = stack.pop();
						v2 = s_temp2.toString();
						
						try{
							s_temp1 = stack.pop();
							v1 = s_temp1.toString();
							weight1 = weight_stack.pop();
							w1 = weight1.toString();

							this.weighted.addVertex(v1);
							this.weighted.addVertex(v2);
							this.weighted.addVertex(v3);	
							this.weighted.addEdge(v1,v2);
							this.weighted.addEdge(v2,v3);

							this.weighted.setEdgeWeight(this.weighted.getEdge(v1,v2), Double.parseDouble(w1));	
							this.weighted.setEdgeWeight(this.weighted.getEdge(v2,v3), Double.parseDouble(w3));	
									
							stack.push(v2);
						}catch(EmptyStackException e){}
					}catch(EmptyStackException e){}												
				}catch(EmptyStackException e){}
									
			}else{
				// if it is a letter, concatenate for the name, and push it to the stack 								
				if(tokens[i].equals(" ")){
					//ignore
				}else if(tokens[i+1].equals("(") || tokens[i+1].equals(")") || tokens[i+1].equals(",")) {
					temp = temp + tokens[i];
					
					if(temp.startsWith(":")){

						temp = temp.replaceAll(":", "");
						weight_stack.push(temp);

					}else if(temp.startsWith(":") == false && (temp.indexOf(":") != -1)){
						
						temp_token = temp.split(":");
						stack.push(temp_token[0]);
						weight_stack.push(temp_token[1]);
					}
					temp = "";
				}else{
					temp = temp + tokens[i];
				}
			}
		}	

		return this.weighted;
	}
      
	/**
	 * Adds a comment.
	 * 
	 * @param comment
	 *            the comment to add.
	 */
	public void addComment(final NexusComment comment) {
		this.comments.add(comment);
	}

	/**
	 * Removes a comment.
	 * 
	 * @param comment
	 *            the comment to remove.
	 */
	public void removeComment(final NexusComment comment) {
		this.comments.remove(comment);
	}

	/**
	 * Returns all comments.
	 * 
	 * @return all the selected comments.
	 */
	public List getComments() {
		return this.comments;
	}

	protected void writeBlockContents(Writer writer) throws IOException {
		for (final Iterator i = this.comments.iterator(); i.hasNext();) {
			((NexusComment) i.next()).writeObject(writer);
			writer.write(NexusFileFormat.NEW_LINE);
		}
		writer.write(" TRANSLATE" + NexusFileFormat.NEW_LINE);
		for (final Iterator i = this.translations.entrySet().iterator(); i
				.hasNext();) {
			final Map.Entry entry = (Map.Entry) i.next();
			writer.write('\t');
			this.writeToken(writer, "" + entry.getKey());
			writer.write('\t');
			this.writeToken(writer, "" + entry.getValue());
			if (i.hasNext())
				writer.write(',');
			else
				writer.write(';');
			writer.write(NexusFileFormat.NEW_LINE);
		}
		for (final Iterator i = this.trees.entrySet().iterator(); i.hasNext();) {
			final Map.Entry entry = (Map.Entry) i.next();
			final NewickTreeString treeStr = (NewickTreeString) entry
					.getValue();
			writer.write(" TREE ");
			if (treeStr.isStarred())
				writer.write("* ");
			this.writeToken(writer, "" + entry.getKey());
			writer.write('=');
			if (treeStr.getRootType() != null)
				writer.write("[" + treeStr.getRootType() + "]");
			this.writeToken(writer, treeStr.getTreeString());
			writer.write(";" + NexusFileFormat.NEW_LINE);
		}
	}

}
