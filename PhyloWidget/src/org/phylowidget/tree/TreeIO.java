package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.andrewberman.sortedlist.ItemI;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import sun.security.provider.certpath.Vertex;

import nexus.TreesBlock;

public class TreeIO
{

	public static void parseNewick(String s)
	{
		SimpleDirectedWeightedGraph g = new SimpleDirectedWeightedGraph(
				DefaultWeightedEdge.class);

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
		double curLength = 0;
		int nodeCount = 0;
		for (int i = 0; i < sb.length(); i++)
		{
			char c = sb.charAt(i);
//			System.out.println(c);
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
				if (c == ')' || c == ',' || c == ';')
				{
					System.out.println(vertices.size());
					nodeCount++;
					// First, we need to finish up the label parsing.
					if (parsingNumber)
					{
						curLength = Double.parseDouble(temp.toString());
					} else
					{
						curLabel = temp.toString();
					}
					if (curLabel.length() == 0)
						curLabel = nodeCount + " " + curLength;
					System.out.println(curLabel);
					// Create a vertex for the current label and length.
					g.addVertex(curLabel);
					
					if (innerNode)
					{
						System.out.println("Trying to pop "+countForDepth[curDepth]);
						for (int j=0; j < countForDepth[curDepth]; j++)
						{
							String childLabel = (String) vertices.pop();
							Double lengthToChild = (Double) lengths.pop();
							
							g.addVertex(childLabel);
							g.addEdge(curLabel,childLabel);
							Object o = g.getEdge(curLabel, childLabel);
							g.setEdgeWeight(o, lengthToChild.doubleValue());
						}
						countForDepth[curDepth] = 0;
						curDepth--;
					}
					// Push onto the stack and keep count.
					vertices.push(curLabel);
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
					innerNode = true;
				}
			} else if (c == ':')
			{
				curLabel = temp.toString();
				temp.replace(0, temp.length(), "");
				parsingNumber = true;
			} else
			{
				temp.append(c);
			}
		}
		for (int i = 0; i < countForDepth.length; i++)
		{
			System.out.println(countForDepth[i]);
		}
		System.out.println(g);
	}

	/*
	 * public static void parseNewick(String s) { SimpleDirectedWeightedGraph g =
	 * new SimpleDirectedWeightedGraph( DefaultWeightedEdge.class); Pre-process
	 * the string as a whole. if (s.indexOf(';') == -1) s = s + ';';
	 * System.out.println("input: " + s); StringBuffer sb = new StringBuffer(s);
	 * Contains an Integer of the number of items for each depth level. int[]
	 * countForDepth = new int[1]; The current depth level being parsed. int
	 * curDepth = 0; Stacks for the vertices and their associated lengths. Stack
	 * vertices = new Stack(); Stack lengths = new Stack(); The regular
	 * expressions to be used to parse through the input file. O becomes an open
	 * bracket, and C becomes a close bracket. // String pre =
	 * "((,\\(){0,1}(\\(){0,1}(\\)){0,1}(,){0,1}"; // String pre =
	 * "((,\\(\\)){1,2}"; // String pre = "(O|,O|C|){1}"; String pre =
	 * "(O|,O|C){1}"; String mid = "([^,OC]*)"; // String post =
	 * "[,\\(\\);]{1})"; String post = "(C,|C|C;|,){1}"; Compile and run the
	 * regex. String full = pre + mid + post; full.replaceAll("C", "\\)");
	 * full.replaceAll("O", "\\)"); Pattern p = Pattern.compile(pre + mid +
	 * post); Matcher m = p.matcher(sb); int findStart = 0; while
	 * (m.find(findStart)) { If we're not at the end of the string, kick the
	 * regex matcher back one char because the character matched in the "post"
	 * part of the regex string might be important for the next node's token. //
	 * if (m.end(2) < sb.length()) // { findStart = m.end(2); // } else //
	 * findStart = m.end(2);
	 *//**
		 * Store whether we're opening or closing an internal node, and clear
		 * control chars from the token.
		 */
	/*
	 * String token = m.group(); System.out.println(token);
	 * System.out.println(m.group(2)); boolean open = token.indexOf('(') != -1;
	 * boolean close = token.startsWith(")"); boolean startsWithOpen =
	 * token.startsWith("("); boolean endsWithComma = token.endsWith(","); token =
	 * token.replaceAll("[\\(\\),;]", ""); Parse the name and depth, if they
	 * exist. String[] tokens = token.split(":"); // First element should always
	 * be the name. String label = tokens[0]; double length = 1; if
	 * (tokens.length == 2) { String lengthString = tokens[1]; length =
	 * Double.parseDouble(lengthString); } Do something special if we're opening
	 * or closing an internal node. if (open) { We're parsing at the next
	 * highest depth now. curDepth++; Grow the int array if necessary. if
	 * (curDepth >= countForDepth.length) { int[] newArr = new
	 * int[countForDepth.length << 2]; System.arraycopy(countForDepth, 0,
	 * newArr, 0, countForDepth.length); countForDepth = newArr; } } else if
	 * (close) { TODO: Pop out the nodes added at this depth, create an internal
	 * node, and connect them all together. curDepth--; } Create a new vertex
	 * and push onto the stack if necessary. if (!startsWithOpen ||
	 * endsWithComma) { // g.addVertex(label); // vertices.push(label); //
	 * lengths.push(new Double(length)); System.out.println("count it!");
	 * countForDepth[curDepth]++; } System.out.println("cd:" + curDepth); } for
	 * (int i = 0; i < countForDepth.length; i++) {
	 * System.out.println(countForDepth[i]); } }
	 */

	public static DirectedGraph rootedGraph(AbstractBaseGraph g)
	{
		SimpleDirectedWeightedGraph dir = new SimpleDirectedWeightedGraph(
				DefaultWeightedEdge.class);

		return null;
	}

	public static Tree graphToTree(Graph graph)
	{
		Set vertices = graph.vertexSet();

		Iterator i = vertices.iterator();
		while (i.hasNext())
		{
			Vertex v = (Vertex) i.next();
		}
		return null;
	}

}
