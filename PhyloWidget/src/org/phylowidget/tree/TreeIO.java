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
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
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
			if (t instanceof PhyloTree)
			{
				PhyloTree pt = (PhyloTree) t;
				String str = f.getParent();
				pt.setBaseURL(str);
			}
			return parseReader(t, br);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static RootedTree parseReader(RootedTree t, BufferedReader br) throws Exception
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

	private static boolean isNeXML(String s)
	{
		return s.contains("nex:nexml");
	}

	public static RootedTree parseNewickString(RootedTree tree, String s)
	{
		// GJ 2009-03-10: Try to catch a NeXML file, and parse it using our NexmlIO.
		if (isNeXML(s))
		{
			NexmlIO io = new NexmlIO(tree.getClass());
			try
			{
				return io.parseString(s);
			} catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}

		boolean oldEnforceUniqueLabels = tree.getEnforceUniqueLabels();
		tree.setEnforceUniqueLabels(false);
		if (tree instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) tree;
			//			pt.setHoldCalculations(true);
		}

		//		System.out.println(s);
		//		System.out.println("Spn Nwk = "+s.equals(spnNwk));
		if (DEBUG)
			System.out.println(System.currentTimeMillis() + "\tStarting parse...");
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

				PWPlatform.getInstance().getThisAppContext().getPW().setMessage(
					"Error: to load a tree from a URL, please use PhyloWidget Full!");
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
				// Do nothing! Just continue as if we never did that...				
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		boolean nhx = (s.indexOf("NHX") != -1);
		boolean poorMans = (s.indexOf(POOR_MANS_NHX) != -1);

		/*
		 * Pre-process the string as a whole.
		 */
		if (s.startsWith("'("))
			s = s.substring(1);
		if (s.endsWith("'"))
			s = s.substring(0, s.length() - 1);
		if (s.indexOf(';') == -1)
			s = s + ';';

		/*
		 * Snag the annotations and store them in a hashtable for later retrieval.
		 */
		NHXHandler nhxHandler = new NHXHandler();
		boolean stripAnnotations = false;
		if (stripAnnotations)
			s = nhxHandler.stripAnnotations(s);

		/*
		 * Contains an Integer of the number of items for each depth level.
		 */
		int[] countForDepth = new int[50];
		/*
		 * A hashtable recording the first (i.e. first in order) child for each
		 * node. This will be used after parsing is complete to recreate the
		 * correct sorting order of nodes and leaves. key = parent node; value =
		 * first child node
		 */
		HashMap<DefaultVertex, DefaultVertex> firstChildren = new HashMap<DefaultVertex, DefaultVertex>();
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
		boolean withinNHX = false;
		/*
		 * Pattern matcher.
		 */
		String controlChars = "();,";
		/*
		 * Label and length temporary strings.
		 */
		StringBuffer temp = new StringBuffer(10000);
		String curLabel = new String();
		if (DEBUG)
			System.out.println(System.currentTimeMillis() + "\tChar loop...");

		long len = s.length();
		//		char[] chars = s.toCharArray();
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			boolean isControl = (c == '(' || c == ')' || c == ';' || c == ',');
			if (DEBUG)
			{
				if (i % (len / 50 + 1) == 0)
				{
					System.out.print(".");
				}
			}

			// GJ 2009-03-05 - NHX handling, so we can have commas within NHX annotations.
			if (c == '[' && !withinNHX)
				withinNHX = true;
			else if (withinNHX && c == ']')
				withinNHX = false;
			if (withinNHX)
				isControl = false;

			if (withinEscapedString)
			{
				temp.append(c);
				if (c == '\'')
					withinEscapedString = false;
				continue;
				// GJ 2008-09-29: only let a string become escaped if it's got an apostrophe on the first char. 
			} else if (c == '\'' && temp.length() == 0)
			{
				temp.append(c);
				withinEscapedString = true;
				continue;
			}
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

					// Do I need this stuff here? YUP.
					curLabel = temp.toString();
					curLabel = curLabel.trim();
					if (stripAnnotations)
						curLabel = nhxHandler.replaceAnnotation(curLabel);

					PhyloNode curNode = newNode(tree, curLabel, nhx, poorMans);

					if (c == ';')
					{
						// Can't forget to store which node is the root!
						root = curNode;
					}
					if (innerNode)
					{
						DefaultVertex child = null;
						for (int j = 0; j < countForDepth[curDepth]; j++)
						{
							// Pop out the child node and connect to the parent.
							child = vertices.pop();
							double length = lengths.pop();
							Object o = null;
							if (!tree.containsEdge(curNode, child))
								o = tree.addEdge(curNode, child);
							o = tree.getEdge(curNode, child);
							tree.setEdgeWeight(o, length);
						}
						// Flush out the depth counter for the current depth.
						countForDepth[curDepth] = 0;
						curDepth--;
						// Keep track of which element was first.
						firstChildren.put(curNode, child);
					}
					// Push onto the stack and keep count.
					vertices.push(curNode);
					lengths.push(curNode.getBranchLengthCache());
					countForDepth[curDepth]++;
					// Reset all the states.
					temp.replace(0, temp.length(), "");
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
			System.out.println(System.currentTimeMillis() + "\nSorting nodes...");
		/*
		 * Now, to recreate the newick file's node sorting. We previously
		 * recorded the "first" child node for each parent node, which we'll now
		 * use to determine whether we want to sort that node in forward or
		 * reverse.
		 */
		PhyloTree pt = (PhyloTree) tree;
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
		((CachedRootedTree) tree).modPlus();

		// GJ 2009-03-01: if we have a massive tree, never enforce unique labels (it's too slow!)
		if (tree.getNumEnclosedLeaves(tree.getRoot()) > 1000)
			tree.setEnforceUniqueLabels(false);
		else
			tree.setEnforceUniqueLabels(oldEnforceUniqueLabels);

		if (DEBUG)
			System.out.println(System.currentTimeMillis() + "\nDone loading tree!");
		return tree;
	}

	static RootedTree oldTree;

	public static void setOldTree(RootedTree t)
	{
		oldTree = t;
	}

	public static final String POOR_MANS_NHX = "**";
	public static final String POOR_MANS_DELIM = "*";

	static int newNodeCount = 0;

	static PhyloNode newNode(RootedTree t, String s, boolean useNhx, boolean poorMan)
	{
		PhyloNode v = new PhyloNode();

		//		newNodeCount++;
		//		if (newNodeCount % 100 == 0 && DEBUG)
		//			System.out.print("!");
		String nameAndLength = s;

		int nhxInd = -1;
		int altNhxInd = -1;
		if (useNhx)
		{
			nhxInd = s.indexOf("[&&NHX");
			altNhxInd = s.indexOf("[**NHX");
			if (nhxInd != -1 || altNhxInd != -1)
			{
				String nhx = "";
				if (nhxInd != -1)
				{
					nameAndLength = s.substring(0, nhxInd);
					nhx = s.substring(nhxInd, s.length());
					nhx = nhx.replaceAll("(\\[&&NHX:|\\])", "");
				} else if (altNhxInd != -1)
				{
					nameAndLength = s.substring(0, altNhxInd);
					nhx = s.substring(altNhxInd, s.length());
					//					System.out.println(nhx);
					nhx = nhx.replaceAll("(\\[\\*\\*NHX:|\\])", "");
				}
				//				System.out.println(nhx);
				String[] attrs = nhx.split(":");
				for (String attr : attrs)
				{
					//					System.out.println(attr);
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
		}

		int colonInd = nameAndLength.indexOf(":");
		String name = nameAndLength;
		double curLength = 1;
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
				e.printStackTrace();
				curLength = 1;
			}
		}

		v.setBranchLengthCache(curLength);

		// If there's no NHX annotation, try and break up the label using the "poor man's" NHX delimiters.
		if (poorMan && nhxInd == -1 && altNhxInd == -1)
		{
			int poorInd = name.indexOf(POOR_MANS_NHX);
			if (poorInd != -1)
			{
				String keeperName = name.substring(0, poorInd);
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
			//			PhyloNode existingNode = (PhyloNode) oldTree.getVertexForLabel(s);
			//			if (existingNode != null)
			//			{
			//				t.addVertex(existingNode);
			//				t.setLabel(existingNode,existingNode.getLabel());
			//				existingNode.setBranchLength(v.getBranchLength());
			//				return existingNode;
			//			}
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
	private static String translateName(String s)
	{
		String mapped = translationMap.get(s);
		if (mapped != null)
			return mapped;
		else
			return s;
	}

	public static String createNewickString(RootedTree tree)
	{
		TreeOutputConfig config = new TreeOutputConfig();
		config.outputNHX = false;
		return createTreeString(tree, config);
	}

	public static String createNHXString(RootedTree tree)
	{
		TreeOutputConfig config = new TreeOutputConfig();
		config.outputNHX = true;
		return createTreeString(tree, config);
	}
	
	public static String createNeXMLString(RootedTree tree)
	{
		NexmlIO io = new NexmlIO(tree.getClass());
		return io.createNeXMLString(tree);
	}

	private static String createTreeString(RootedTree tree, TreeOutputConfig config)
	{
		if (config == null)
			config = new TreeOutputConfig();

		StringBuffer sb = new StringBuffer();
		synchronized (tree)
		{
			outputVertex(tree, sb, tree.getRoot(), config);
		}
		return sb.toString() + ";";
	}

	private static void outputVertex(RootedTree tree, StringBuffer sb, DefaultVertex v, TreeOutputConfig config)
	{
		/*
		 * Ok, I was gonna make this one iterative instead of recursive (like
		 * the parser), but it's just too annoying. So maybe on reeeeally large
		 * trees, this will result in heap problems. Oh, well...
		 */
		if (!tree.isLeaf(v) || tree.isCollapsed(v)) // GJ 2008-10-15: Still output children if collapsed.
		{
			sb.append('(');
			List<DefaultVertex> l = tree.getChildrenOf(v);
			for (int i = 0; i < l.size(); i++)
			{
				outputVertex(tree, sb, l.get(i), config);
				if (i != l.size() - 1)
					sb.append(',');
			}
			sb.append(')');
		}
		// Call this to make the vertex's label nicely formatted for Nexus
		// output.
		String s =
				getNexusCompliantLabel(tree, v, config.includeStupidLabels, config.scrapeNaughtyChars,
					config.outputAllInnerNodes);
		if (s.length() != 0)
			sb.append(s);
		Object p = tree.getParentOf(v);
		if (p != null)
		{
			double ew = tree.getEdgeWeight(tree.getEdge(p, v));
			//			if (ew != 1.0)
			sb.append(":" + Double.toString(ew));
		}
		if (v instanceof PhyloNode && config.outputNHX)
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

	public static String getNexusCompliantLabel(RootedTree t, DefaultVertex v, boolean includeStupidLabels,
			boolean scrapeNaughtyChars, boolean outputAllInnerNodes)
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
			System.out.println(s);
			if (scrapeNaughtyChars)
			{
				/*
				 * If this setting is set, simply scrape away naughty characters from the label.
				 */
				s = m.replaceAll("");
				s = s.replaceAll(" ", "_");
			} else
			{
				Matcher quoteM = quotePattern.matcher(s);
				s = quoteM.replaceAll("''");
				s = "'" + s + "'";
			}
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
			boolean pr = outputAllInnerNodes;
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
		label = label.replaceAll("`", "'");
		if (label.indexOf("'") == 0)
		{
			label = label.substring(1, label.length() - 1);
			/*
			 * Now, fix back all internal single quotes.
			 */
			Matcher m = singleQuotePattern.matcher(label);
			label = m.replaceAll("'");
		}
		if (label.indexOf("_") != -1)
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
		 * The "?" is important here: we want to do this in a non-greedy manner.
		 */
		return matchGroup(s, "translate(.*?);", 1);
	}

	static String getTreeFromTreesBlock(String treesBlock)
	{
		return matchGroup(treesBlock, "^??tree (.*?);", 1);
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
		ImageLoader loader = PWPlatform.getInstance().getThisAppContext().trees().imageLoader;
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

	public static final class NHXHandler
	{
		HashMap<String, String> annotationMap = new HashMap<String, String>();

		public void clear()
		{
			annotationMap.clear();
		}

		Pattern annotationRegex = Pattern.compile("\\[.*?\\]");

		public String stripAnnotations(String s)
		{
			if (DEBUG)
				System.out.println("Stripping annotations... ");
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

		public String replaceAnnotation(String s)
		{
			int ind = s.indexOf(ANNOT_PREFIX);
			if (ind != -1)
			{
				String annot = s.substring(ind, s.length());
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

	static final class TreeOutputConfig
	{
		public boolean scrapeNaughtyChars;
		public boolean outputAllInnerNodes;
		public boolean outputNHX;
		public boolean includeStupidLabels;
		public boolean outputTreeImages;

		public TreeOutputConfig()
		{
			outputNHX = true;
			includeStupidLabels = false;
			outputTreeImages = false;
			scrapeNaughtyChars = true;
			outputAllInnerNodes = false;
		}
	}
}
