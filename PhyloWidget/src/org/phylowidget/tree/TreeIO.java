package org.phylowidget.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Stack;

import org.jgrapht.WeightedGraph;
import org.phylowidget.ui.PhyloNode;

public class TreeIO
{

	public static RootedTree parseFile(File f)
	{
		try
		{
			URI uri = f.toURI();
			URL url = uri.toURL();
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			return parseReader(br);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static RootedTree parseReader(BufferedReader br)
	{
		String line;
		StringBuffer buff = new StringBuffer();
		try
		{
			while ((line = br.readLine()) != null)
			{
				buff.append(line);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		return parseNewickString(buff.toString());
	}
	
	static RootedTree parseNewickString(String s)
	{
		return parseNewickString(new RootedTree(), s);
	}
	
	public static RootedTree parseNewickString(RootedTree tree, String s)
	{
		Object root = null;
		/*
		 * Pre-process the string as a whole.
		 */
		if (s.indexOf(';') == -1)
			s = s + ';';
		System.out.println("input: " + s);
		StringBuffer sb = new StringBuffer(s);
		/*
		 * Contains an Integer of the number of items for each depth level.
		 */
		int[] countForDepth = new int[1];
		/*
		 * The current depth level being parsed.
		 */
		int curDepth = 0;
		/*
		 * A simple counter (for labeling unlabeled nodes).
		 */
		int nodeCount = 0;
		/*
		 * Stacks for the vertices and their associated lengths.
		 */
		Stack vertices = new Stack();
		Stack lengths = new Stack();
		/*
		 * Booleans.
		 */
		boolean parsingNumber = false;
		boolean innerNode = false;
		/*
		 * Pattern matcher.
		 */
		String controlChars = "();,";
		/*
		 * Label and length temporary strings.
		 */
		StringBuffer temp = new StringBuffer();
		String curLabel = new String();
		double curLength = 1;
		for (int i = 0; i < sb.length(); i++)
		{
			char c = sb.charAt(i);
			// System.out.println(c);
			boolean isControl = controlChars.indexOf(c) != -1;
			if (isControl)
			{
				if (c == '(')
				{
					curDepth++;
					if (curDepth >= countForDepth.length)
					{
						int[] newArr = new int[countForDepth.length << 2];
						System.arraycopy(countForDepth, 0, newArr, 0,
								countForDepth.length);
						countForDepth = newArr;
					}
				}
				/*
				 * It's time to roll with the big boys -- this block is where
				 * most of the dirty work is done.
				 * 
				 * Note that no matter what, this block will create a new
				 * vertex. If innerNode is set to true, then we've previously
				 * encountered a ')', then it will also create and "package up"
				 * the new inner node by connecting all the child nodes to the
				 * new inner node.
				 */
				if (c == ')' || c == ',' || c == ';')
				{
					nodeCount++;
					// First, we need to finish up the label parsing.
					if (parsingNumber)
					{
						curLength = Double.parseDouble(temp.toString());
					} else
					{
						curLabel = temp.toString();
						curLabel = curLabel.replace('_', ' ');
					}
					if (curLabel.length() == 0)
						curLabel = String.valueOf(nodeCount);// + " " +
					// curLength;
					// Create a vertex for the current label and length.
					Object curNode = tree.createVertex(curLabel);
					tree.addVertex(curNode);
					if (c == ';')
					{
						// Can't forget to store which node is the root!
						root = curNode;
					}
					if (innerNode)
					{
						for (int j = 0; j < countForDepth[curDepth]; j++)
						{
							// Pop out the child node and connect to the parent.
							PhyloNode child = (PhyloNode) vertices.pop();
							double length = ((Double) lengths.pop())
									.doubleValue();
							if (!tree.containsEdge(curNode,child))
								tree.addEdge(curNode, child);
							Object o = tree.getEdge(curNode, child);
							((WeightedGraph) tree).setEdgeWeight(o, length);
						}
						// Flush out the depth counter for the current depth.
						countForDepth[curDepth] = 0;
						curDepth--;
					}
					// Push onto the stack and keep count.
					vertices.push(curNode);
					lengths.push(new Double(curLength));
					countForDepth[curDepth]++;
					// Reset all the states.
					temp.replace(0, temp.length(), "");
					curLength = 1;
					parsingNumber = false;
					innerNode = false;
				}
				if (c == ')')
				{
					/*
					 * If we see a ')' on this character, then flip the
					 * innerNode bit to true so that at the *next* control char,
					 * we know to handle packaging up *this* inner node.
					 */
					innerNode = true;
				}
			} else if (c == ':')
			{
				curLabel = temp.toString();
				curLabel = curLabel.replace('_', ' ');
				temp.replace(0, temp.length(), "");
				parsingNumber = true;
			} else
			{
				temp.append(c);
			}
		}
		// System.out.println(tree);
		tree.setRoot(root);
		return tree;
	}
	
	public static String createNewickString(RootedTree tree)
	{
		
		
		return null;
	}
}
