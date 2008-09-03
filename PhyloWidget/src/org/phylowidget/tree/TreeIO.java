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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jgrapht.WeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.images.ImageLoader;

public class TreeIO
{

	public static final boolean DEBUG = false;
	
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
		translationMap.clear();

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
			String newickFromNexus = getNewickFromNexus(buff.toString());
			//			System.out.println(newickFromNexus);
			return parseNewickString(t, newickFromNexus);
		}
		return parseNewickString(t, buff.toString());
	}

	static double curLength = 1;

	public static RootedTree parseNewickString(RootedTree tree, String s)
	{
		if (DEBUG)
			System.out.println(System.currentTimeMillis()+"\tStarting parse...");
		DefaultVertex root = null;
		/*
		 * See if this String is a valid URL... if it is, then load up the resource!
		 * 
		 *  Some good Nexus test files online here:
		 *  http://www.molevol.org/camel/projects/nexus/NEXUS/
		 */
		int endInd = Math.min(10, s.length() - 1);
		String test = s.substring(0, endInd).toLowerCase();
		if (test.startsWith("http://") || test.startsWith("ftp://") || test.startsWith("file://"))
		{
			try
			{
				URL url = new URL(s);
				BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
				return TreeIO.parseReader(tree, r);
			} catch (SecurityException e)
			{
				e.printStackTrace();
				PhyloWidget.setMessage("Error: to load a tree from a URL, please use PhyloWidget Full!");
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
				// Do nothing! Just continue as if we never did that...				
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		/*
		 * Pre-process the string as a whole.
		 */
		if (s.startsWith("'"))
			s = s.substring(1);
		if (s.endsWith("'"))
			s = s.substring(0, s.length() - 1);
		if (s.indexOf(';') == -1)
			s = s + ';';

		/*
		 * Snag the annotations and store them in a hashtable for later retrieval.
		 */
		s = NHXHandler.stripAnnotations(s);

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
		 * Stacks for the vertices and their associated lengths.
		 */
		Stack<DefaultVertex> vertices = new Stack<DefaultVertex>();
		Stack<Double> lengths = new Stack<Double>();
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
		if (DEBUG)
			System.out.println(System.currentTimeMillis()+"\tChar loop...");
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
						System.arraycopy(countForDepth, 0, newArr, 0, countForDepth.length);
						countForDepth = newArr;
					}
				}
				/*
				 * Lets' roll -- this block is where most of the dirty work is done.
				 * 
				 * Note that no matter what, this block will create a new
				 * vertex. If innerNode is set to true, then we've previously
				 * encountered a ')', then it will also create and "package up"
				 * the new inner node by connecting all the child nodes to the
				 * new inner node.
				 */
				if (c == ')' || c == ',' || c == ';')
				{
					// First, we need to finish up the label parsing.
					//					if (parsingNumber)
					//					{
					//						curLength = Double.parseDouble(temp.toString());
					//					} else
					//					{
					// Do I need this stuff here? YUP.
					curLabel = temp.toString();
					curLabel = curLabel.trim();
					//					}

					//					curLabel = parseNexusLabel(curLabel);
					DefaultVertex curNode = newNode(tree, curLabel);
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
							double length = ((Double) lengths.pop()).doubleValue();
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
					lengths.push(curLength);
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
			}
			//			else if (c == ':')
			//			{
			//				curLabel = temp.toString();
			//				curLabel = curLabel.replace('_', ' ');
			//				curLabel = curLabel.trim();
			//				temp.replace(0, temp.length(), "");
			//				parsingNumber = true;
			//			} 
			else
			{
				temp.append(c);
			}
		}
		tree.setRoot(root);
		if (DEBUG)
			System.out.println(System.currentTimeMillis()+"\tSorting nodes...");
		/*
		 * Now, to recreate the newick file's node sorting. We previously
		 * recorded the "first" child node for each parent node, which we'll now
		 * use to determine whether we want to sort that node in forward or
		 * reverse.
		 */
		BreadthFirstIterator dfi = new BreadthFirstIterator(tree, tree.getRoot());
		while (dfi.hasNext())
		{
			DefaultVertex p = (DefaultVertex) dfi.next();
			if (!tree.isLeaf(p))
			{
				List l = tree.getChildrenOf(p);
				if (l.get(0) != firstChildren.get(p))
				{
					tree.sorting.put(p, RootedTree.REVERSE);
				}
			}
		}
		/*
		 * If the oldTree was set, unset it.
		 */
		oldTree = null;
		/*
		 * ModPlus if we're a cached tree.
		 */
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

	public static final String POOR_MANS_NHX = "**";
	public static final String POOR_MANS_DELIM = "*";

	static DefaultVertex newNode(RootedTree t, String s)
	{
		PhyloNode v = new PhyloNode();

		/*
		 * The input string is the *entire* Newick / Nexus / NHX string, including all annotations and labels.
		 */
		s = NHXHandler.replaceAnnotation(s);
		
		int nhxInd = s.indexOf("[&&NHX");
		String nameAndLength = s;
		if (nhxInd != -1)
		{
			nameAndLength = s.substring(0, nhxInd);
			String nhx = s.substring(nhxInd, s.length());
			nhx = nhx.replaceAll("(\\[&&NHX:|\\])", "");
			String[] attrs = nhx.split(":");
			for (String attr : attrs)
			{
				/*
				 * All colons should be stored as "&colon;". Let's get them back.
				 */
				attr = attr.replaceAll(COLON_REPLACE, ":");
				int ind = attr.indexOf('=');
				if (ind != -1)
				{
					v.setAnnotation(attr.substring(0, ind), attr.substring(ind + 1, attr.length()));
				}
			}
		}

		int colonInd = nameAndLength.indexOf(":");
		String name = nameAndLength;
		if (colonInd != -1)
		{
			String length = nameAndLength.substring(colonInd + 1);
			name = nameAndLength.substring(0, colonInd);

			if (length.contains("[")) // We've got an annoying bootstrap value stored as )species_name:1.0[100] .
			{
				int startInd = length.indexOf("[");
				int endInd = length.indexOf("]");
				String bootstrap = length.substring(startInd + 1, endInd);
				length = length.substring(0, startInd);
				if (v instanceof PhyloNode)
				{
					PhyloNode pn = (PhyloNode) v;
					pn.setAnnotation("b", bootstrap);
				}
			}
			try
			{
				curLength = Double.parseDouble(length);
			} catch (Exception e)
			{
				curLength = 1;
			}
		}

		// If there's no NHX annotation, try and break up the label using the "poor man's" NHX delimiters.
		if (nhxInd == -1)
		{
			int poorInd = name.indexOf(POOR_MANS_NHX);
			if (poorInd != -1)
			{
				String keeperName = name.substring(0,poorInd);
				String nhx = name.substring(poorInd + POOR_MANS_NHX.length(), name.length());
//				System.out.println(nhx);
				String[] attrs = nhx.split("\\" + POOR_MANS_DELIM);
				for (int i = 0; i < attrs.length; i++)
				{
					String attr = attrs[i];
					i++;
					if (i > attrs.length - 1)
						break;
					String attr2 = attrs[i];
					v.setAnnotation(attr, attr2);
				}
				name = keeperName;
			}
		}

		s = name;
		s = translateName(s);
		s = parseNexusLabel(s);
		
		if (oldTree != null)
		{
			DefaultVertex existingNode = oldTree.getVertexForLabel(s);
			if (existingNode != null)
				return existingNode;
		}
		//		DefaultVertex o = t.createAndAddVertex();
		t.addVertex(v);
		t.setLabel(v, s);
		return v;
	}

	/**
	 * Translates this node label using the translation table.
	 * 
	 * @param s
	 */
	static String translateName(String s)
	{
		String mapped = translationMap.get(s);
		if (mapped != null)
			return mapped;
		else
			return s;
	}

	public static String createNewickString(RootedTree tree, boolean includeStupidLabels)
	{
		StringBuffer sb = new StringBuffer();
		outputVertex(tree, sb, tree.getRoot(), includeStupidLabels);
		return sb.toString() + ";";
	}

	private static void outputVertex(RootedTree tree, StringBuffer sb, DefaultVertex v, boolean includeStupidLabels)
	{
		/*
		 * Ok, I was gonna make this one iterative instead of recursive (like
		 * the parser), but it's just too annoying. So maybe on reeeeally large
		 * trees, this will result in heap problems. Oh, well...
		 */
		if (!tree.isLeaf(v))
		{
			sb.append('(');
			List<DefaultVertex> l = tree.getChildrenOf(v);
			for (int i = 0; i < l.size(); i++)
			{
				outputVertex(tree, sb, l.get(i), includeStupidLabels);
				if (i != l.size() - 1)
					sb.append(',');
			}
			sb.append(')');
		}
		// Call this to make the vertex's label nicely formatted for Nexus
		// output.
		String s = getNexusCompliantLabel(tree, v, includeStupidLabels);
		if (s.length() != 0)
			sb.append(s);
		Object p = tree.getParentOf(v);
		if (p != null)
		{
			double ew = tree.getEdgeWeight(tree.getEdge(p, v));
			if (ew != 1.0)
				sb.append(":" + Double.toString(ew));
		}
		if (v instanceof PhyloNode)
		{
			/*
			 * Output annotations if they exist.
			 */
			PhyloNode n = (PhyloNode) v;
			HashMap<String, String> annot = n.getAnnotations();
			if (annot != null)
			{
				sb.append("[&&NHX");
				/*
				 * Okay, we have some annotations.
				 */
				Set<String> set = annot.keySet();
				for (String st : set)
				{
					if (st.length() == 0)
						continue; // Deal with stupid keys.
					String value = annot.get(st);
					/*
					 * Turn all colons into "&colon;".
					 */
					value = value.replaceAll(":", COLON_REPLACE);
					sb.append(":" + st + "=" + value);
				}
				sb.append("]");
			}
		}
	}

	static final String COLON_REPLACE = "&colon;";
	static Pattern escaper = Pattern.compile("([^a-zA-Z0-9])");

	public static String escapeRE(String str)
	{
		return escaper.matcher(str).replaceAll("\\\\$1");
	}

	static String naughtyChars = "()[]{}/\\,;:=*'\"`<>^-+~";
	static String naughtyRegex = "[" + escapeRE(naughtyChars) + "]";
	static Pattern naughtyPattern = Pattern.compile(naughtyRegex);

	static Pattern quotePattern = Pattern.compile("'");

	private static String getNexusCompliantLabel(RootedTree t, DefaultVertex v, boolean includeStupidLabels)
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
		if (!includeStupidLabels && !t.isLabelSignificant(s) && !t.isLeaf(v))
		{
			boolean pr = PhyloWidget.cfg.outputAllInnerNodes;
			if (!pr)
			{
				s = "";
			}
		}
		return s;
	}

	static Pattern singleQuotePattern = Pattern.compile("('')");
	private static HashMap<String, String> translationMap = new HashMap<String, String>();

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
		//		label = label.trim();
		return label;
	}

	/*
	 * Nexus parsing stuff...
	 */
	static Pattern createPattern(String pattern)
	{
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
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

	static String getTreeFromTreesBlock(String treesBlock)
	{
		return matchGroup(treesBlock, "tree(.*?);", 1);
	}

	static void getTranslationMap(String treesBlock)
	{
		String trans = getTranslateBlock(treesBlock);
		translationMap = new HashMap<String, String>();
		if (trans.length() > 0)
		{
			String[] pairs = trans.split(",");
			for (String pair : pairs)
			{
				pair = pair.trim();
				if (pair.length() < 1)
					continue;
				String[] twoS = pair.split("[\\s]+");
				String from = twoS[0].trim();
				String to = twoS[1].trim();
				translationMap.put(from, to);
			}
		}
	}

	static String getNewickFromNexus(String s)
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
		getTranslationMap(s);

		s = getTreeFromTreesBlock(s);

		s = s.substring(s.indexOf("=") + 1);
		s = s.trim();
		return s;
	}

	public static BufferedImage createBufferedImage(Image image)
	{
		if (image instanceof BufferedImage)
		{
			return (BufferedImage) image;
		}
		BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB); // ARGB to support transparency if in original image
		Graphics2D g = bi.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose(); // supposedly recommended for cleanup...
		return bi;
	}

	public static void outputTreeImages(RootedTree t, File dir)
	{
		ImageLoader loader = PhyloWidget.trees.imageLoader;
		ArrayList<PhyloNode> nodes = new ArrayList<PhyloNode>();
		t.getAll(t.getRoot(), null, nodes);

		int img_id = 0;
		for (PhyloNode n : nodes)
		{
			try
			{
				if (n.getAnnotation("img") != null)
				{
					String imgURL = n.getAnnotation("img");
					Image img = loader.getImageForNode(n);
					if (img != null)
					{
						img = createBufferedImage(img);
						File f = new File(dir.getAbsolutePath() + File.separator + img_id + ".jpg");
//						System.out.println(f);
						n.setAnnotation("img", f.toURL().toString());
						System.out.println(n.getAnnotation("img"));
						try
						{
							ImageIO.write((RenderedImage) img, "jpg", f);
						} catch (IOException e)
						{
							e.printStackTrace();
						}

						img_id++;
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
	}

	static class NHXHandler
	{
		static HashMap<String, String> annotationMap = new HashMap<String, String>();

		static void clear()
		{
			annotationMap.clear();
		}

		static Pattern annotationRegex = Pattern.compile("\\[.*?\\]");

		static String stripAnnotations(String s)
		{
			StringBuffer sb = new StringBuffer(s);

			int i = 1;
			Matcher m = annotationRegex.matcher(sb);
			int index = 0;
			while (m.find(index))
			{
				String annotation = m.group();
				// If we recognize a URL in the annotation, replace it with the colon replacement.
				annotation = annotation.replaceAll("http:", "http" + COLON_REPLACE);
				annotation = annotation.replaceAll("ftp:", "ftp" + COLON_REPLACE);
				String key = ANNOT_PREFIX + String.valueOf(i) + "#";
				annotationMap.put(key, annotation);
				sb.replace(m.start(), m.end(), key);
				index = m.start();
				i++;
			}
			return sb.toString();
		}

		private static final String ANNOT_PREFIX = "#ANNOT_";
		
		static String replaceAnnotation(String s)
		{
			int ind = s.indexOf(ANNOT_PREFIX);
			if (ind != -1)
			{
				String annot = s.substring(ind,s.length());
				String repl = annotationMap.get(annot);
				if (repl != null)
					s = s.replace(annot, repl);
			}
//			Set<String> set = annotationMap.keySet();
//			for (String key : set)
//			{
//				s = s.replace(key, annotationMap.get(key));
//			}
			return s;
		}
	}
}
