package org.phylowidget.tree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
//		System.out.println("input: " + s);
		/*
		 * String buffer which we'll be parsing from.
		 */
		StringBuffer sb = new StringBuffer(s);
		/*
		 * Contains an Integer of the number of items for each depth level.
		 */
		int[] countForDepth = new int[1];
		/*
		 * A hashtable recording the first (i.e. first in order) child for each
		 * node. This will be used after parsing is complete to reconstitute the
		 * correct sorting order of nodes and leaves. key = parent node value =
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
		for (int i = 0; i < sb.length(); i++)
		{
			char c = sb.charAt(i);
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
					Object curNode = tree.createAndAddVertex(curLabel);
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
		return tree;
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
		 * Now, if the label is just a number (i.e. "_123") we assume that this
		 * is an unlabeled node, and the number was just inserted by PhyloWidget
		 * to keep the node labels unique.
		 */
		if (s.lastIndexOf('_') == 0)
		{
			s = "";
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
}
