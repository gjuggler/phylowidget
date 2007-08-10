package org.phylowidget.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
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
		if (s.indexOf(';') == -1)
			s = s + ';';
		// biojavax parser still sucks, so let's roll our own.
		System.out.println("input: " + s);
		StringBuffer sb = new StringBuffer(s);

		/*
		 * Some state-maintaining variables.
		 */

		/**
		 * Contains an Integer of the number of items for each depth level.
		 */
		int[] countForDepth = new int[10];
		/**
		 * The current depth level being parsed.
		 */
		int curDepth = 0;

		/*
		 * Parse into a series of tokens, each with a full taxon label and depth
		 * information as well as its preceding separator. EX: (A:3 or ,(C:3 or
		 * )int:1
		 */
		// String pre = "(([,]{0,1}[\\(\\)&&[^,]]{0,1}){1}";
		String pre = "((,\\(){0,1}(\\(){0,1}(\\)){0,1}(,){0,1}";
		// String pre = "([\\(\\)[,]]{1,2}";
		String mid = "[^,\\(\\)]*)";
		String post = "[,\\(\\);]{1}";

		Pattern p = Pattern.compile(pre + mid + post);
		Matcher m = p.matcher(sb);
		int findStart = 0;
		while (m.find(findStart))
		{
			/*
			 * If we're not at the end of the string, kick the regex matcher
			 * back one char because the character matched in the "post" part of
			 * the regex string might be important for the next node's token.
			 */
			if (m.end() < sb.length())
			{
				findStart = m.end() - 1;
			} else
				findStart = m.end();

			String token = m.group(1);
			System.out.println(token);

			boolean open = token.indexOf('(') != -1;
			boolean close = token.indexOf(')') != -1;
			token = token.replaceAll("[\\(\\)]", "");
			/*
			 * Parse the name and depth, if they exist.
			 */
			// System.out.println(curDepth + " " + countForDepth[curDepth]);
			/*
			 * Add one to the stack count for our current depth.
			 */
			if (open)
			{
				/*
				 * We're parsing at the next highest depth now.
				 */
				curDepth++;
				/*
				 * Grow the int array if necessary.
				 */
				if (curDepth >= countForDepth.length)
				{
					int[] newArr = new int[countForDepth.length << 2];
					System.arraycopy(countForDepth, 0, newArr, 0,
							countForDepth.length);
					countForDepth = newArr;
				}
			} else if (close)
			{
				/*
				 * Pop out the nodes added at this depth, create an internal
				 * node, and connect them all together.
				 */

				curDepth--;
			}
			/*
			 * Add one onto the node count for the current depth.
			 */
			countForDepth[curDepth]++;
		}

		for (int i = 0; i < countForDepth.length; i++)
		{
			System.out.println(countForDepth[i]);
		}
	}

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
