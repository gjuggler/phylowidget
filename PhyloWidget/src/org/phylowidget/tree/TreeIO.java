/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.WeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.PhyloWidget;

public class TreeIO
{

	public static RootedTree parseFile(RootedTree t, File f)
	{
		try
		{
			URI uri = f.toURI();
			URL url = uri.toURL();
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			return parseReader(t, br);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static RootedTree parseReader(RootedTree t, BufferedReader br)
	{
		String line;
		StringBuffer buff = new StringBuffer();

		boolean isNexus = false;
		try
		{
			while ((line = br.readLine()) != null)
			{
				if (line.indexOf("#NEXUS") != -1)
				{
					isNexus = true;
				}
				buff.append(line);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		if (isNexus)
		{
			return parseNewickString(t,getNewickFromNexus(buff.toString()));
		}
		return parseNewickString(t, buff.toString());
	}

	public static RootedTree parseNewickString(RootedTree tree, String s)
	{
		Object root = null;
		/*
		 * Pre-process the string as a whole.
		 */
		if (s.indexOf(';') == -1)
			s = s + ';';
		// System.out.println("input: " + s);
		/*
		 * String buffer which we'll be parsing from.
		 */
		//		StringBuffer sb = new StringBuffer(s);
		/*
		 * Contains an Integer of the number of items for each depth level.
		 */
		int[] countForDepth = new int[1];
		/*
		 * A hashtable recording the first (i.e. first in order) child for each
		 * node. This will be used after parsing is complete to recreate the
		 * correct sorting order of nodes and leaves. key = parent node; value =
		 * first child node
		 */
		HashMap firstChildren = new HashMap();
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
		boolean withinEscapedString = false;
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
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (withinEscapedString)
			{
				temp.append(c);
				if (c == '\'')
					withinEscapedString = false;
				continue;
			} else if (c == '\'')
			{
				temp.append(c);
				withinEscapedString = true;
				continue;
			}
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
						// Do I need this stuff here? YUP.
						curLabel = temp.toString();
						curLabel = curLabel.trim();
					}
					// Create a vertex for the current label and length.
					curLabel = parseNexusLabel(curLabel);
					Object curNode = newNode(tree, curLabel);
					//					Object curNode = tree.createAndAddVertex(curLabel);
					if (c == ';')
					{
						// Can't forget to store which node is the root!
						root = curNode;
					}
					if (innerNode)
					{
						Object child = null;
						for (int j = 0; j < countForDepth[curDepth]; j++)
						{
							// Pop out the child node and connect to the parent.
							child = vertices.pop();
							double length = ((Double) lengths.pop())
									.doubleValue();
							if (!tree.containsEdge(curNode, child))
								tree.addEdge(curNode, child);
							Object o = tree.getEdge(curNode, child);
							((WeightedGraph) tree).setEdgeWeight(o, length);
						}
						// Flush out the depth counter for the current depth.
						countForDepth[curDepth] = 0;
						curDepth--;
						// Keep track of which element was first.
						firstChildren.put(curNode, child);
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
				curLabel = curLabel.trim();
				temp.replace(0, temp.length(), "");
				parsingNumber = true;
			} else
			{
				temp.append(c);
			}
		}
		tree.setRoot(root);

		/*
		 * Now, to recreate the newick file's node sorting. We previously
		 * recorded the "first" child node for each parent node, which we'll now
		 * use to determine whether we want to sort that node in forward or
		 * reverse.
		 */
		BreadthFirstIterator dfi = new BreadthFirstIterator(tree, tree
				.getRoot());
		while (dfi.hasNext())
		{
			Object p = dfi.next();
			if (!tree.isLeaf(p))
			{
				tree.sorting.put(p, RootedTree.REVERSE);
				List l = tree.getChildrenOf(p);
				if (l.get(0) != firstChildren.get(p))
					tree.sorting.put(p, RootedTree.FORWARD);
			}
		}
		/*
		 * If the oldTree was set, unset it.
		 */
		oldTree = null;
		if (tree instanceof CachedRootedTree)
		{
			((CachedRootedTree) tree).modPlus();
		}
		return tree;
	}

	static RootedTree oldTree;

	public static void setOldTree(RootedTree t)
	{
		oldTree = t;
	}

	static Object newNode(RootedTree t, String s)
	{
		Object newNode = null;
		if (oldTree != null)
		{
			newNode = oldTree.getVertexForLabel(s);
		}
		if (newNode == null)
		{
			newNode = t.createVertex(s);
		}
		t.addVertex(newNode);
		return newNode;
	}

	public static String createNewickString(RootedTree tree)
	{
		StringBuffer sb = new StringBuffer();
		outputVertex(tree, sb, tree.getRoot());
		return sb.toString();
	}

	private static void outputVertex(RootedTree tree, StringBuffer sb, Object v)
	{
		/*
		 * Ok, I was gonna make this one iterative instead of recursive (like
		 * the parser), but it's just too annoying. So maybe on reeeeally large
		 * trees, this will result in heap problems. Oh, well...
		 */
		if (!tree.isLeaf(v))
		{
			sb.append('(');
			List l = tree.getChildrenOf(v);
			for (int i = 0; i < l.size(); i++)
			{
				outputVertex(tree, sb, l.get(i));
				if (i != l.size() - 1)
					sb.append(',');
			}
			sb.append(')');
		}
		// Call this to make the vertex's label nicely formatted for Nexus
		// output.
		String s = getNexusCompliantLabel(tree, v);
		if (s.length() != 0)
			sb.append(s);
		Object p = tree.getParentOf(v);
		if (p != null)
		{
			double ew = tree.getEdgeWeight(tree.getEdge(p, v));
			if (ew != 1.0)
				sb.append(":" + Double.toString(ew));
		}
	}

	static Pattern escaper = Pattern.compile("([^a-zA-Z0-9])");

	public static String escapeRE(String str)
	{
		return escaper.matcher(str).replaceAll("\\\\$1");
	}

	static String naughtyChars = "()[]{}/\\,;:=*'\"`<>^-+~";
	static String naughtyRegex = "[" + escapeRE(naughtyChars) + "]";
	static Pattern naughtyPattern = Pattern.compile(naughtyRegex);

	static Pattern quotePattern = Pattern.compile("'");

	private static String getNexusCompliantLabel(RootedTree t, Object v)
	{
		String s = v.toString();
		Matcher m = naughtyPattern.matcher(s);
		if (m.find())
		{
			/*
			 * If we have bad characters in the label, we:
			 * 
			 * 1. escape the whole thing in single quotes
			 * 
			 * 2. double-escape single quotes
			 */
			Matcher quoteM = quotePattern.matcher(s);
			s = quoteM.replaceAll("''");
			s = "'" + s + "'";
		} else
		{
			// Otherwise, just turn whitespace into underbars.
			s = s.replaceAll(" ", "_");
		}
		/*
		 * Now, if the label is just a number (i.e. "#123") we assume that this
		 * is an unlabeled node, and the number was just inserted by PhyloWidget
		 * to keep the node labels unique.
		 */
		if (!UniqueLabeler.isLabelSignificant(s) && !t.isLeaf(v))
		{
			boolean pr = PhyloWidget.ui.outputAllInnerNodes;
			if (!pr)
			{
				s = "";
			}
		}
		return s;
	}

	static Pattern singleQuotePattern = Pattern.compile("('')");

	private static String parseNexusLabel(String label)
	{
		if (label.indexOf("'") == 0)
		{
			label = label.substring(1, label.length() - 1);
			/*
			 * Now, fix back all internal single quotes.
			 */
			Matcher m = singleQuotePattern.matcher(label);
			label = m.replaceAll("'");
		}
		label = label.replace('_', ' ');
		label = label.trim();
		return label;
	}

	/*
	 * Nexus parsing stuff...
	 */

	static Pattern createPattern(String pattern)
	{
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE
				| Pattern.DOTALL);
	}

	static String removeComments(String s)
	{
		Pattern commentFinder = createPattern("(\\[.*?\\])");
		Matcher m = commentFinder.matcher(s);
		String output = m.replaceAll("");
		return output;
	}

	static String matchGroup(String s, String pattern, int groupNumber)
	{
		Pattern p = createPattern(pattern);
		Matcher m = p.matcher(s);
		m.find();
		try
		{
			return m.group(groupNumber);
		} catch (Exception e)
		{
			return "";
		}
	}

	static String getTreesBlock(String s)
	{
		return matchGroup(s, "begin trees;(.*)end;", 1);
	}

	static String getTranslateBlock(String s)
	{
		/*
		 * The "?" is important here: we want to 
		 */
		return matchGroup(s, "translate(.*?);", 1);
	}

	static public String getTreeFromTrees(String treesBlock)
	{
		return matchGroup(treesBlock, "tree(.*?);", 1);
	}

	static public String translateFirstTree(String treesBlock)
	{
		String trans = getTranslateBlock(treesBlock);
		HashMap<String, String> map = new HashMap<String, String>();

		if (trans.length() > 0)
		{
			String[] pairs = trans.split(",");
			for (String pair : pairs)
			{
				pair = pair.trim();
				String[] twoS = pair.split("[\\s]+");
				String from = twoS[0].trim();
				String to = twoS[1].trim();
				map.put(from, to);
			}
		}
		/*
		 * Get the first tree from the trees block.
		 */
		String tree = getTreeFromTrees(treesBlock);

		for (String key : map.keySet())
		{
			tree = tree.replaceAll(key, map.get(key));
		}

		return tree;
	}

	static public String getNewickFromNexus(String s)
	{
		/*
		 * First, remove all comments from the Nexus string.
		 */
		s = removeComments(s);

		/*
		 * Now, grab the Trees block from the file.
		 */
		s = getTreesBlock(s);

		/*
		 * Grab the first tree out of the trees block and translate it (if applicable)
		 */
		s = translateFirstTree(s);

		s = s.substring(s.indexOf("=") + 1);
		s = s.trim();
		return s;
	}
}
