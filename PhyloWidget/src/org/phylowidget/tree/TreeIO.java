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
import java.net.MalformedURLException;
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
import org.phylowidget.ui.PhyloTree;

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
			return parseNewickString(t, getNewickFromNexus(buff.toString()));
		}
		return parseNewickString(t, buff.toString());
	}

	static double curLength = 1;
	
	public static RootedTree parseNewickString(RootedTree tree, String s)
	{
		DefaultVertex root = null;

		/*
		 * See if this String is a valid URL... if it is, then load up the resource!
		 * 
		 *  Some good Nexus test files online here:
		 *  http://www.molevol.org/camel/projects/nexus/NEXUS/
		 */
		int endInd = Math.min(10, s.length() - 1);
		String test = s.substring(0, endInd).toLowerCase();
		if (test.startsWith("http://") || test.startsWith("ftp://")
				|| test.startsWith("file://"))
		{
			try
			{
				URL url = new URL(s);
				BufferedReader r = new BufferedReader(new InputStreamReader(url
						.openStream()));
				return TreeIO.parseReader(tree, r);
			} catch (SecurityException e)
			{
				PhyloWidget
						.setMessage("Error: to load a tree from a URL, please use PhyloWidget Full!");
			} catch (MalformedURLException e)
			{
				// Do nothing! Just continue as if we never did that...				
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		/*
		 * Pre-process the string as a whole.
		 */
		if (s.indexOf(';') == -1)
			s = s + ';';

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
			DefaultVertex p = (DefaultVertex)dfi.next();
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

	static DefaultVertex newNode(RootedTree t, String s)
	{
		/*
		 * The input string is the *entire* Newick / Nexus / NHX string, including all annotations and labels.
		 */
		int nhxInd = s.indexOf("[&&NHX");
		String nameAndLength = s;
		DefaultVertex v = null;
		if (nhxInd != -1)
		{
			nameAndLength = s.substring(0,nhxInd);
			String nhx = s.substring(nhxInd,s.length());
			nhx = nhx.replaceAll("(\\[&&NHX:|\\])", "");
			String[] attrs = nhx.split(":");
			NHXNode node = new NHXNode();
			for (String attr : attrs)
			{
				String[] split = attr.split("=");
				if (split.length >= 2)
				{
					node.setAnnotation(split[0], split[1]);
				}
			}
			v = node;
		} else
		{
			v = new PhyloNode();
		}
		
		int colonInd = nameAndLength.indexOf(":");
		String name = nameAndLength;
		if (colonInd != -1)
		{
			String length = nameAndLength.substring(colonInd+1);
			name = nameAndLength.substring(0, colonInd);
			curLength = Double.parseDouble(length);
		}
		
		s = name;
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

	public static void main(String[] args)
	{
		String s = "((((((ENSGALP00000016814:0.1668[&&NHX:D=N:G=ENSGALG00000010339:T=9031],ENSGALP00000016813:0.3092[&&NHX:D=N:G=ENSGALG00000010338:T=9031])Gallus gallus:0.2645[&&NHX:D=Y:B=87:T=9031],ENSGALP00000016811:0.4267[&&NHX:D=N:G=ENSGALG00000010336:T=9031])Gallus gallus:0.0000[&&NHX:D=Y:B=38:T=9031],(((ENSLAFP00000008410:0.0766[&&NHX:D=N:G=ENSLAFG00000010039:T=9785],ENSETEP00000003960:0.2269[&&NHX:D=N:G=ENSETEG00000004845:T=9371])Afrotheria:0.0667[&&NHX:D=N:B=48:T=311790],ENSDNOP00000009304:0.1162[&&NHX:D=N:G=ENSDNOG00000012009:T=9361])Eutheria:0.0081[&&NHX:D=N:B=27:T=9347],((((ENSFCAP00000003438:0.0235[&&NHX:D=N:G=ENSFCAG00000003734:T=9685],ENSCAFP00000012760:0.0892[&&NHX:D=N:G=ENSCAFG00000008692:T=9615])Carnivora:0.0378[&&NHX:D=N:B=98:T=33554],ENSMLUP00000008738:0.2496[&&NHX:D=N:G=ENSMLUG00000009593:T=59463])Laurasiatheria:0.0065[&&NHX:D=N:B=1:T=314145],((((ENSP00000286758:0.0000[&&NHX:D=N:G=ENSG00000156234:T=9606],ENSPTRP00000027829:0.0030[&&NHX:D=N:G=ENSPTRG00000016197:T=9598])Homo/Pan/Gorilla group:0.0299[&&NHX:D=N:B=100:T=207598],ENSMMUP00000030517:0.0168[&&NHX:D=N:G=ENSMMUG00000023186:T=9544])Catarrhini:0.0850[&&NHX:D=N:B=99:T=9526],ENSOGAP00000010844:0.1342[&&NHX:D=N:G=ENSOGAG00000012119:T=30611])Primates:0.0007[&&NHX:D=N:B=41:T=9443],(ENSSTOP00000013697:0.1219[&&NHX:D=N:G=ENSSTOG00000015287:T=43179],ENSTBEP00000014146:0.1576[&&NHX:D=N:G=ENSTBEG00000016283:T=37347])Euarchontoglires:0.0237[&&NHX:D=N:B=15:T=314146])Euarchontoglires:0.0412[&&NHX:D=N:B=2:T=314146])Eutheria:0.0174[&&NHX:D=N:B=0:T=9347],((ENSEEUP00000006563:0.1790[&&NHX:D=N:G=ENSEEUG00000007201:T=9365],ENSBTAP00000011164:0.1968[&&NHX:D=N:G=ENSBTAG00000008479:T=9913])Laurasiatheria:0.0372[&&NHX:D=N:B=43:T=314145],(ENSMUSP00000023840:0.1213[&&NHX:D=N:G=ENSMUSG00000023078:T=10090],ENSRNOP00000034304:0.1326[&&NHX:D=N:G=ENSRNOG00000024899:T=10116])Murinae:0.2104[&&NHX:D=N:B=100:T=39107])Eutheria:0.0568[&&NHX:D=N:B=18:T=9347])Eutheria:0.0416[&&NHX:D=Y:B=1:T=9347])Eutheria:0.3642[&&NHX:D=N:B=5:T=9347])Amniota:0.1462[&&NHX:D=N:B=29:T=32524],((ENSGACP00000025229:0.0789[&&NHX:D=N:G=ENSGACG00000019078:T=69293],ENSORLP00000009833:0.1241[&&NHX:D=N:G=ENSORLG00000007849:T=8090])Smegmamorpha:0.0485[&&NHX:D=N:B=84:T=129949],(SINFRUP00000182392:0.0921[&&NHX:D=N:G=SINFRUG00000159726:T=31033],GSTENP00004865001:0.1050[&&NHX:D=N:G=GSTENG00004865001:T=99883])Tetraodontidae:0.0509[&&NHX:D=N:B=93:T=31031])Percomorpha:0.3507[&&NHX:D=N:B=80:T=32485])Euteleostomi:0.0290[&&NHX:D=N:B=16:T=117571],((((((((((ENSLAFP00000000407:0.0489[&&NHX:D=N:G=ENSLAFG00000000483:T=9785],ENSETEP00000013525:0.1402[&&NHX:D=N:G=ENSETEG00000016666:T=9371])Afrotheria:0.0204[&&NHX:D=N:B=18:T=311790],(((((ENSFCAP00000004310:0.0419[&&NHX:D=N:G=ENSFCAG00000004666:T=9685],ENSCAFP00000004516:0.0463[&&NHX:D=N:G=ENSCAFG00000003029:T=9615])Carnivora:0.0203[&&NHX:D=N:B=84:T=33554],ENSBTAP00000026275:0.0528[&&NHX:D=N:G=ENSBTAG00000019716:T=9913])Laurasiatheria:0.0156[&&NHX:D=N:B=69:T=314145],ENSDNOP00000002926:0.1066[&&NHX:D=N:G=ENSDNOG00000003801:T=9361])Eutheria:0.0091[&&NHX:D=N:B=10:T=9347],ENSOCUP00000010171:0.0420[&&NHX:D=N:G=ENSOCUG00000011835:T=9986])Eutheria:0.0000[&&NHX:D=N:B=0:T=9347],((((((ENSPTRP00000027770:0.0000[&&NHX:D=N:G=ENSPTRG00000016154:T=9598],ENSP00000379121:0.0060[&&NHX:D=N:G=ENSG00000169429:T=9606])Homo/Pan/Gorilla group:0.0076[&&NHX:D=N:B=100:T=207598],ENSMMUP00000005115:0.0239[&&NHX:D=N:G=ENSMMUG00000003836:T=9544])Catarrhini:0.0706[&&NHX:D=N:B=100:T=9526],(ENSOGAP00000007324:0.0000[&&NHX:D=N:G=ENSOGAG00000008185:T=30611],ENSOGAP00000009565:0.0000[&&NHX:D=N:G=ENSOGAG00000010690:T=30611])Otolemur garnettii:0.0903[&&NHX:D=Y:B=100:T=30611])Primates:0.0021[&&NHX:D=N:B=9:T=9443],(ENSSTOP00000002912:0.0436[&&NHX:D=N:G=ENSSTOG00000003271:T=43179],ENSCPOP00000009985:0.1822[&&NHX:D=N:G=ENSCPOG00000011103:T=10141])Rodentia:0.0081[&&NHX:D=N:B=2:T=9989])Euarchontoglires:0.0123[&&NHX:D=N:B=0:T=314146],ENSTBEP00000009324:0.0750[&&NHX:D=N:G=ENSTBEG00000010795:T=37347])Euarchontoglires:0.0000[&&NHX:D=N:B=0:T=314146],ENSMLUP00000004435:0.1347[&&NHX:D=N:G=ENSMLUG00000004871:T=59463])Eutheria:0.0074[&&NHX:D=N:B=0:T=9347])Eutheria:0.0259[&&NHX:D=Y:B=0:T=9347])Eutheria:0.0475[&&NHX:D=N:B=1:T=9347],ENSSARP00000006384:0.1249[&&NHX:D=N:G=ENSSARG00000007060:T=42254])Eutheria:0.0594[&&NHX:D=Y:B=5:T=9347],ENSMODP00000023940:0.1452[&&NHX:D=N:G=ENSMODG00000019182:T=13616])Theria:0.1216[&&NHX:D=N:B=48:T=32525],ENSOANP00000011009:0.3154[&&NHX:D=N:G=ENSOANG00000006910:T=9258])Mammalia:0.1436[&&NHX:D=N:B=73:T=40674],ENSOANP00000011016:0.4445[&&NHX:D=N:G=ENSOANG00000006914:T=9258])Mammalia:0.0220[&&NHX:D=Y:B=11:T=40674],(ENSGALP00000019049:0.0453[&&NHX:D=N:G=ENSGALG00000011668:T=9031],ENSGALP00000019051:0.1422[&&NHX:D=N:G=ENSGALG00000011670:T=9031])Gallus gallus:0.2074[&&NHX:D=Y:B=95:T=9031])Amniota:0.0320[&&NHX:D=N:B=3:T=32524],(((((((((ENSSARP00000005089:0.0000[&&NHX:D=N:G=ENSSARG00000005640:T=42254],ENSSARP00000001981:0.0267[&&NHX:D=N:G=ENSSARG00000002193:T=42254])Sorex araneus:0.0972[&&NHX:D=Y:B=94:T=42254],ENSFCAP00000002889:0.0938[&&NHX:D=N:G=ENSFCAG00000003138:T=9685])Laurasiatheria:0.0057[&&NHX:D=N:B=13:T=314145],((ENSOGAP00000009568:0.0000[&&NHX:D=N:G=ENSOGAG00000010692:T=30611],ENSOGAP00000002449:0.0280[&&NHX:D=N:G=ENSOGAG00000002735:T=30611])Otolemur garnettii:0.0369[&&NHX:D=Y:B=91:T=30611],(ENSOCUP00000011978:0.0105[&&NHX:D=N:G=ENSOCUG00000013937:T=9986],ENSOCUP00000015416:0.0274[&&NHX:D=N:G=ENSOCUG00000017942:T=9986])Oryctolagus cuniculus:0.0867[&&NHX:D=Y:B=99:T=9986])Euarchontoglires:0.0205[&&NHX:D=N:B=19:T=314146])Eutheria:0.0089[&&NHX:D=N:B=6:T=9347],((((ENSEEUP00000001277:0.0058[&&NHX:D=N:G=ENSEEUG00000001419:T=9365],ENSEEUP00000007754:0.0306[&&NHX:D=N:G=ENSEEUG00000008534:T=9365])Erinaceus europaeus:0.0104[&&NHX:D=Y:B=99:T=9365],ENSEEUP00000001654:0.0909[&&NHX:D=N:G=ENSEEUG00000001821:T=9365])Erinaceus europaeus:0.1002[&&NHX:D=Y:B=83:T=9365],ENSBTAP00000043330:0.0694[&&NHX:D=N:G=ENSBTAG00000027513:T=9913])Laurasiatheria:0.0000[&&NHX:D=N:B=0:T=314145],((((ENSMUSP00000074885:0.0339[&&NHX:D=N:G=ENSMUSG00000058427:T=10090],ENSRNOP00000003745:0.0733[&&NHX:D=N:G=ENSRNOG00000002792:T=10116])Murinae:0.0030[&&NHX:D=N:B=79:T=39107],(ENSMUSP00000031326:0.0245[&&NHX:D=N:G=ENSMUSG00000029379:T=10090],ENSRNOP00000034154:0.0483[&&NHX:D=N:G=ENSRNOG00000028043:T=10116])Murinae:0.0382[&&NHX:D=N:B=91:T=39107])Murinae:0.0841[&&NHX:D=Y:B=79:T=39107],(ENSRNOP00000003778:0.0152[&&NHX:D=N:G=ENSRNOG00000002802:T=10116],ENSMUSP00000031327:0.0262[&&NHX:D=N:G=ENSMUSG00000029380:T=10090])Murinae:0.1044[&&NHX:D=N:B=100:T=39107])Murinae:0.0287[&&NHX:D=Y:B=36:T=39107],ENSCPOP00000013707:0.2386[&&NHX:D=N:G=ENSCPOG00000015206:T=10141])Rodentia:0.0373[&&NHX:D=N:B=22:T=9989])Eutheria:0.0093[&&NHX:D=N:B=0:T=9347])Eutheria:0.0000[&&NHX:D=Y:B=0:T=9347],((((((((ENSP00000296031:0.0031[&&NHX:D=N:G=ENSG00000163739:T=9606],ENSPTRP00000027773:0.0031[&&NHX:D=N:G=ENSPTRG00000016157:T=9598])Homo/Pan/Gorilla group:0.0387[&&NHX:D=N:B=100:T=207598],ENSMMUP00000006023:0.0161[&&NHX:D=N:G=ENSMMUG00000004524:T=9544])Catarrhini:0.0039[&&NHX:D=N:B=9:T=9526],(((ENSMMUP00000010972:0.0033[&&NHX:D=N:G=ENSMMUG00000008367:T=9544],ENSMMUP00000016859:0.0149[&&NHX:D=N:G=ENSMMUG00000012834:T=9544])Macaca mulatta:0.0229[&&NHX:D=Y:B=99:T=9544],ENSMMUP00000035189:0.0382[&&NHX:D=N:G=ENSMMUG00000030303:T=9544])Macaca mulatta:0.0076[&&NHX:D=Y:B=28:T=9544],(ENSPTRP00000027777:0.0031[&&NHX:D=N:G=ENSPTRG00000016161:T=9598],ENSP00000296026:0.0125[&&NHX:D=N:G=ENSG00000163734:T=9606])Homo/Pan/Gorilla group:0.0125[&&NHX:D=N:B=100:T=207598])Catarrhini:0.0052[&&NHX:D=N:B=7:T=9526])Catarrhini:0.0036[&&NHX:D=Y:B=0:T=9526],(ENSPTRP00000027779:0.0035[&&NHX:D=N:G=ENSPTRG00000016163:T=9598],ENSP00000379110:0.0059[&&NHX:D=N:G=ENSG00000081041:T=9606])Homo/Pan/Gorilla group:0.0152[&&NHX:D=N:B=96:T=207598])Catarrhini:0.0297[&&NHX:D=Y:B=16:T=9526],ENSSTOP00000011950:0.0694[&&NHX:D=N:G=ENSSTOG00000013338:T=43179])Euarchontoglires:0.0131[&&NHX:D=N:B=13:T=314146],(ENSTBEP00000014254:0.0081[&&NHX:D=N:G=ENSTBEG00000016412:T=37347],ENSTBEP00000007795:0.0410[&&NHX:D=N:G=ENSTBEG00000008997:T=37347])Tupaia belangeri:0.0441[&&NHX:D=Y:B=96:T=37347])Euarchontoglires:0.0000[&&NHX:D=N:B=2:T=314146],(ENSMLUP00000016058:0.0234[&&NHX:D=N:G=ENSMLUG00000017608:T=59463],ENSMLUP00000006134:0.1011[&&NHX:D=N:G=ENSMLUG00000006717:T=59463])Myotis lucifugus:0.0681[&&NHX:D=Y:B=88:T=59463])Eutheria:0.0046[&&NHX:D=N:B=5:T=9347],((ENSDNOP00000012861:0.0519[&&NHX:D=N:G=ENSDNOG00000016593:T=9361],ENSDNOP00000013403:0.1201[&&NHX:D=N:G=ENSDNOG00000017292:T=9361])Dasypus novemcinctus:0.0277[&&NHX:D=Y:B=67:T=9361],(ENSETEP00000007404:0.0158[&&NHX:D=N:G=ENSETEG00000009123:T=9371],ENSETEP00000011799:0.0203[&&NHX:D=N:G=ENSETEG00000014549:T=9371])Echinops telfairi:0.0870[&&NHX:D=Y:B=100:T=9371])Eutheria:0.0178[&&NHX:D=N:B=17:T=9347])Eutheria:0.0234[&&NHX:D=N:B=0:T=9347])Eutheria:0.1241[&&NHX:D=Y:B=0:T=9347],ENSMODP00000023952:0.2669[&&NHX:D=N:G=ENSMODG00000019188:T=13616])Theria:0.0386[&&NHX:D=N:B=60:T=32525],(((((ENSTBEP00000007223:0.1613[&&NHX:D=N:G=ENSTBEG00000008350:T=37347],ENSOGAP00000006141:0.2718[&&NHX:D=N:G=ENSOGAG00000006864:T=30611])Euarchontoglires:0.0000[&&NHX:D=N:B=2:T=314146],((((ENSP00000296028:0.0000[&&NHX:D=N:G=ENSG00000163736:T=9606],ENSPTRP00000027775:0.0031[&&NHX:D=N:G=ENSPTRG00000016159:T=9598])Homo/Pan/Gorilla group:0.0147[&&NHX:D=N:B=99:T=207598],ENSMMUP00000006025:0.0168[&&NHX:D=N:G=ENSMMUG00000004526:T=9544])Catarrhini:0.1083[&&NHX:D=N:B=99:T=9526],(ENSMUSP00000031319:0.1056[&&NHX:D=N:G=ENSMUSG00000029372:T=10090],ENSRNOP00000003794:0.1157[&&NHX:D=N:G=ENSRNOG00000002829:T=10116])Murinae:0.1892[&&NHX:D=N:B=95:T=39107])Euarchontoglires:0.0026[&&NHX:D=N:B=0:T=314146],((ENSMLUP00000003882:0.0999[&&NHX:D=N:G=ENSMLUG00000004265:T=59463],ENSBTAP00000043338:0.1066[&&NHX:D=N:G=ENSBTAG00000032425:T=9913])Laurasiatheria:0.0470[&&NHX:D=N:B=76:T=314145],ENSCAFP00000004524:0.1277[&&NHX:D=N:G=ENSCAFG00000003033:T=9615])Laurasiatheria:0.0353[&&NHX:D=N:B=39:T=314145])Eutheria:0.0645[&&NHX:D=N:B=0:T=9347])Eutheria:0.0190[&&NHX:D=Y:B=1:T=9347],(((ENSDNOP00000006521:0.1200[&&NHX:D=N:G=ENSDNOG00000008429:T=9361],ENSETEP00000000388:0.1972[&&NHX:D=N:G=ENSETEG00000000483:T=9371])Eutheria:0.0340[&&NHX:D=N:B=34:T=9347],ENSSARP00000009649:0.2248[&&NHX:D=N:G=ENSSARG00000010693:T=42254])Eutheria:0.0000[&&NHX:D=N:B=1:T=9347],(((((ENSMUSP00000031320:0.0617[&&NHX:D=N:G=ENSMUSG00000029373:T=10090],ENSRNOP00000032276:0.1017[&&NHX:D=N:G=ENSRNOG00000028015:T=10116])Murinae:0.0956[&&NHX:D=N:B=77:T=39107],ENSOCUP00000015414:0.1509[&&NHX:D=N:G=ENSOCUG00000017941:T=9986])Glires:0.0000[&&NHX:D=N:B=2:T=314147],(((((ENSP00000296029:0.0031[&&NHX:D=N:G=ENSG00000163737:T=9606],ENSPTRP00000027774:0.0063[&&NHX:D=N:G=ENSPTRG00000016158:T=9598])Homo/Pan/Gorilla group:0.0000[&&NHX:D=N:B=97:T=207598],ENSMMUP00000006026:0.0256[&&NHX:D=N:G=ENSMMUG00000004527:T=9544])Catarrhini:0.0000[&&NHX:D=N:B=77:T=9526],((ENSPTRP00000027772:0.0334[&&NHX:D=N:G=ENSPTRG00000016156:T=9598],ENSP00000226524:0.0811[&&NHX:D=N:G=ENSG00000109272:T=9606])Homo/Pan/Gorilla group:0.0076[&&NHX:D=N:B=4:T=207598],ENSMMUP00000001731:0.0274[&&NHX:D=N:G=ENSMMUG00000001295:T=9544])Catarrhini:0.0111[&&NHX:D=N:B=2:T=9526])Catarrhini:0.0836[&&NHX:D=Y:B=4:T=9526],(ENSOGAP00000009570:0.0128[&&NHX:D=N:G=ENSOGAG00000010694:T=30611],ENSOGAP00000006553:0.0227[&&NHX:D=N:G=ENSOGAG00000007321:T=30611])Otolemur garnettii:0.0886[&&NHX:D=Y:B=98:T=30611])Primates:0.0000[&&NHX:D=N:B=30:T=9443],ENSTBEP00000008673:0.1879[&&NHX:D=N:G=ENSTBEG00000010038:T=37347])Euarchontoglires:0.0164[&&NHX:D=N:B=3:T=314146])Euarchontoglires:0.0053[&&NHX:D=N:B=0:T=314146],ENSFCAP00000001614:0.1946[&&NHX:D=N:G=ENSFCAG00000001743:T=9685])Eutheria:0.0130[&&NHX:D=N:B=0:T=9347],(((ENSSTOP00000011675:0.0658[&&NHX:D=N:G=ENSSTOG00000013032:T=43179],ENSSTOP00000013361:0.0784[&&NHX:D=N:G=ENSSTOG00000014918:T=43179])Spermophilus tridecemlineatus:0.1628[&&NHX:D=Y:B=99:T=43179],ENSBTAP00000043334:0.1363[&&NHX:D=N:G=ENSBTAG00000011961:T=9913])Eutheria:0.0201[&&NHX:D=N:B=2:T=9347],ENSLAFP00000009276:0.0967[&&NHX:D=N:G=ENSLAFG00000011089:T=9785])Eutheria:0.0551[&&NHX:D=N:B=0:T=9347])Eutheria:0.0129[&&NHX:D=Y:B=0:T=9347])Eutheria:0.1366[&&NHX:D=Y:B=1:T=9347])Eutheria:0.0461[&&NHX:D=Y:B=1:T=9347],((ENSLAFP00000015094:0.0782[&&NHX:D=N:G=ENSLAFG00000018004:T=9785],ENSETEP00000010048:0.0974[&&NHX:D=N:G=ENSETEG00000012391:T=9371])Afrotheria:0.0039[&&NHX:D=N:B=44:T=311790],((((((ENSSARP00000001249:0.0000[&&NHX:D=N:G=ENSSARG00000001372:T=42254],ENSSARP00000001660:0.0043[&&NHX:D=N:G=ENSSARG00000001848:T=42254])Sorex araneus:0.1417[&&NHX:D=Y:B=100:T=42254],ENSEEUP00000012197:0.1076[&&NHX:D=N:G=ENSEEUG00000013395:T=9365])Insectivora:0.0092[&&NHX:D=N:B=10:T=9362],(ENSFCAP00000013886:0.0495[&&NHX:D=N:G=ENSFCAG00000014969:T=9685],ENSCAFP00000034480:0.1284[&&NHX:D=N:G=ENSCAFG00000025016:T=9615])Carnivora:0.0546[&&NHX:D=N:B=57:T=33554])Laurasiatheria:0.0118[&&NHX:D=N:B=2:T=314145],ENSMLUP00000000183:0.1672[&&NHX:D=N:G=ENSMLUG00000000205:T=59463])Laurasiatheria:0.0000[&&NHX:D=N:B=0:T=314145],(((ENSRNOP00000003823:0.0886[&&NHX:D=N:G=ENSRNOG00000002843:T=10116],ENSMUSP00000031318:0.1128[&&NHX:D=N:G=ENSMUSG00000029371:T=10090])Murinae:0.1521[&&NHX:D=N:B=100:T=39107],ENSSTOP00000011680:0.1021[&&NHX:D=N:G=ENSSTOG00000013038:T=43179])Sciurognathi:0.0000[&&NHX:D=N:B=0:T=33553],ENSTBEP00000007482:0.0959[&&NHX:D=N:G=ENSTBEG00000008652:T=37347])Euarchontoglires:0.0241[&&NHX:D=N:B=0:T=314146])Eutheria:0.0057[&&NHX:D=N:B=0:T=9347],((((((ENSP00000379114:0.0056[&&NHX:D=N:G=ENSG00000163735:T=9606],ENSPTRP00000027776:0.0098[&&NHX:D=N:G=ENSPTRG00000016160:T=9598])Homo/Pan/Gorilla group:0.0243[&&NHX:D=N:B=100:T=207598],((ENSMMUP00000006024:0.0031[&&NHX:D=N:G=ENSMMUG00000030304:T=9544],ENSMMUP00000033801:0.0124[&&NHX:D=N:G=ENSMMUG00000029753:T=9544])Macaca mulatta:0.0027[&&NHX:D=Y:B=73:T=9544],ENSMMUP00000018534:0.0159[&&NHX:D=N:G=ENSMMUG00000029754:T=9544])Macaca mulatta:0.0287[&&NHX:D=Y:B=72:T=9544])Catarrhini:0.0201[&&NHX:D=N:B=99:T=9526],((ENSP00000226317:0.0000[&&NHX:D=N:G=ENSG00000124875:T=9606],ENSPTRP00000027771:0.0061[&&NHX:D=N:G=ENSPTRG00000016155:T=9598])Homo/Pan/Gorilla group:0.0171[&&NHX:D=N:B=100:T=207598],ENSMMUP00000023439:0.0429[&&NHX:D=N:G=ENSMMUG00000017829:T=9544])Catarrhini:0.0479[&&NHX:D=N:B=99:T=9526])Catarrhini:0.0442[&&NHX:D=Y:B=89:T=9526],ENSOCUP00000010175:0.0878[&&NHX:D=N:G=ENSOCUG00000011838:T=9986])Euarchontoglires:0.0047[&&NHX:D=N:B=0:T=314146],ENSDNOP00000004631:0.0634[&&NHX:D=N:G=ENSDNOG00000005976:T=9361])Eutheria:0.0058[&&NHX:D=N:B=0:T=9347],ENSBTAP00000043339:0.0773[&&NHX:D=N:G=ENSBTAG00000009812:T=9913])Eutheria:0.0058[&&NHX:D=N:B=1:T=9347])Eutheria:0.0152[&&NHX:D=Y:B=0:T=9347])Eutheria:0.1343[&&NHX:D=N:B=0:T=9347])Eutheria:0.0688[&&NHX:D=Y:B=0:T=9347],ENSMODP00000023941:0.3001[&&NHX:D=N:G=ENSMODG00000019184:T=13616])Theria:0.0444[&&NHX:D=N:B=5:T=32525])Theria:0.0215[&&NHX:D=Y:B=8:T=32525],((ENSOANP00000011010:0.0037[&&NHX:D=N:G=ENSOANG00000006911:T=9258],ENSOANP00000010614:0.0043[&&NHX:D=N:G=ENSOANG00000006655:T=9258])Ornithorhynchus anatinus:0.1197[&&NHX:D=Y:B=100:T=9258],ENSOANP00000011015:0.1996[&&NHX:D=N:G=ENSOANG00000006913:T=9258])Ornithorhynchus anatinus:0.0795[&&NHX:D=Y:B=86:T=9258])Mammalia:0.0220[&&NHX:D=N:B=9:T=40674],ENSGALP00000040602:0.4841[&&NHX:D=N:G=ENSGALG00000024484:T=9031])Amniota:0.1205[&&NHX:D=N:B=8:T=32524])Amniota:0.0267[&&NHX:D=Y:B=1:T=32524],ENSXETP00000055532:0.3007[&&NHX:D=N:G=ENSXETG00000023390:T=8364])Tetrapoda:0.0000[&&NHX:D=N:B=0:T=32523],(((ENSORLP00000006429:0.1167[&&NHX:D=N:G=ENSORLG00000005096:T=8090],ENSGACP00000002251:0.1792[&&NHX:D=N:G=ENSGACG00000001729:T=69293])Smegmamorpha:0.0494[&&NHX:D=N:B=45:T=129949],(SINFRUP00000149334:0.1179[&&NHX:D=N:G=SINFRUG00000140726:T=31033],GSTENP00033049001:0.1257[&&NHX:D=N:G=GSTENG00033049001:T=99883])Tetraodontidae:0.0577[&&NHX:D=N:B=86:T=31031])Percomorpha:0.0696[&&NHX:D=N:B=44:T=32485],ENSDARP00000073828:0.2006[&&NHX:D=N:G=ENSDARG00000056824:T=7955])Clupeocephala:0.2225[&&NHX:D=N:B=62:T=186625])Euteleostomi:0.0558[&&NHX:D=N:B=2:T=117571])Euteleostomi:0.0537[&&NHX:D=Y:B=15:T=117571],((((((((((ENSMLUP00000004847:0.0512[&&NHX:D=N:G=ENSMLUG00000005312:T=59463],ENSBTAP00000039324:0.1456[&&NHX:D=N:G=ENSBTAG00000001725:T=9913])Laurasiatheria:0.0067[&&NHX:D=N:B=4:T=314145],(ENSFCAP00000008811:0.0644[&&NHX:D=N:G=ENSFCAG00000009503:T=9685],ENSSARP00000011198:0.1400[&&NHX:D=N:G=ENSSARG00000012408:T=42254])Laurasiatheria:0.0151[&&NHX:D=N:B=0:T=314145])Laurasiatheria:0.0000[&&NHX:D=N:B=0:T=314145],((((ENSP00000305651:0.0040[&&NHX:D=N:G=ENSG00000169245:T=9606],ENSPTRP00000027807:0.0052[&&NHX:D=N:G=ENSPTRG00000016182:T=9598])Homo/Pan/Gorilla group:0.0060[&&NHX:D=N:B=100:T=207598],ENSMMUP00000027500:0.0165[&&NHX:D=N:G=ENSMMUG00000020903:T=9544])Catarrhini:0.0242[&&NHX:D=N:B=100:T=9526],ENSTBEP00000006341:0.0748[&&NHX:D=N:G=ENSTBEG00000007354:T=37347])Euarchontoglires:0.0000[&&NHX:D=N:B=14:T=314146],(((ENSRNOP00000003649:0.0639[&&NHX:D=N:G=ENSRNOG00000022256:T=10116],ENSMUSP00000047646:0.0818[&&NHX:D=N:G=ENSMUSG00000034855:T=10090])Murinae:0.1483[&&NHX:D=N:B=100:T=39107],ENSCPOP00000001852:0.0641[&&NHX:D=N:G=ENSCPOG00000002046:T=10141])Rodentia:0.0078[&&NHX:D=N:B=1:T=9989],ENSOCUP00000013990:0.0579[&&NHX:D=N:G=ENSOCUG00000016280:T=9986])Glires:0.0000[&&NHX:D=N:B=0:T=314147])Euarchontoglires:0.0321[&&NHX:D=N:B=0:T=314146])Eutheria:0.0052[&&NHX:D=N:B=0:T=9347],ENSCAFP00000012597:0.0334[&&NHX:D=N:G=ENSCAFG00000008584:T=9615])Eutheria:0.0105[&&NHX:D=Y:B=0:T=9347],((ENSLAFP00000006646:0.0201[&&NHX:D=N:G=ENSLAFG00000007923:T=9785],ENSETEP00000012854:0.0841[&&NHX:D=N:G=ENSETEG00000015850:T=9371])Afrotheria:0.0314[&&NHX:D=N:B=74:T=311790],ENSDNOP00000006376:0.0542[&&NHX:D=N:G=ENSDNOG00000008246:T=9361])Eutheria:0.0224[&&NHX:D=N:B=47:T=9347])Eutheria:0.0789[&&NHX:D=N:B=0:T=9347],ENSMODP00000037542:0.2621[&&NHX:D=N:G=ENSMODG00000025268:T=13616])Theria:0.0995[&&NHX:D=N:B=84:T=32525],((((ENSFCAP00000007755:0.0420[&&NHX:D=N:G=ENSFCAG00000008365:T=9685],ENSMLUP00000006347:0.0515[&&NHX:D=N:G=ENSMLUG00000006947:T=59463])Laurasiatheria:0.0062[&&NHX:D=N:B=74:T=314145],(ENSEEUP00000004286:0.1085[&&NHX:D=N:G=ENSEEUG00000004728:T=9365],ENSSARP00000006561:0.2125[&&NHX:D=N:G=ENSSARG00000007248:T=42254])Insectivora:0.1023[&&NHX:D=N:B=52:T=9362])Laurasiatheria:0.0000[&&NHX:D=N:B=2:T=314145],((((((ENSPTRP00000027806:0.0000[&&NHX:D=N:G=ENSPTRG00000016181:T=9598],ENSP00000354901:0.0000[&&NHX:D=N:G=ENSG00000138755:T=9606])Homo/Pan/Gorilla group:0.0192[&&NHX:D=N:B=100:T=207598],ENSMMUP00000027499:0.0245[&&NHX:D=N:G=ENSMMUG00000020902:T=9544])Catarrhini:0.0380[&&NHX:D=N:B=96:T=9526],ENSOGAP00000010404:0.1497[&&NHX:D=N:G=ENSOGAG00000011627:T=30611])Primates:0.0000[&&NHX:D=N:B=6:T=9443],(((ENSRNOP00000003627:0.0310[&&NHX:D=N:G=ENSRNOG00000022242:T=10116],ENSMUSP00000031365:0.0337[&&NHX:D=N:G=ENSMUSG00000029417:T=10090])Murinae:0.1157[&&NHX:D=N:B=100:T=39107],ENSSTOP00000012296:0.1809[&&NHX:D=N:G=ENSSTOG00000013732:T=43179])Sciurognathi:0.0104[&&NHX:D=N:B=1:T=33553],ENSOCUP00000012609:0.1272[&&NHX:D=N:G=ENSOCUG00000014671:T=9986])Glires:0.0000[&&NHX:D=N:B=0:T=314147])Euarchontoglires:0.0114[&&NHX:D=N:B=0:T=314146],ENSTBEP00000006011:0.1088[&&NHX:D=N:G=ENSTBEG00000006974:T=37347])Euarchontoglires:0.0037[&&NHX:D=N:B=0:T=314146],ENSDNOP00000012810:0.0937[&&NHX:D=N:G=ENSDNOG00000016523:T=9361])Eutheria:0.0225[&&NHX:D=N:B=0:T=9347])Eutheria:0.1660[&&NHX:D=N:B=0:T=9347],ENSMODP00000024073:0.2760[&&NHX:D=N:G=ENSMODG00000019287:T=13616])Theria:0.1217[&&NHX:D=N:B=50:T=32525])Theria:0.0593[&&NHX:D=Y:B=47:T=32525],ENSOANP00000011034:0.4646[&&NHX:D=N:G=ENSOANG00000006926:T=9258])Mammalia:0.1399[&&NHX:D=N:B=7:T=40674],(((((ENSRNOP00000035380:0.0549[&&NHX:D=N:G=ENSRNOG00000022298:T=10116],ENSMUSP00000076992:0.0654[&&NHX:D=N:G=ENSMUSG00000060183:T=10090])Murinae:0.1286[&&NHX:D=N:B=98:T=39107],ENSSTOP00000008517:0.0489[&&NHX:D=N:G=ENSSTOG00000009504:T=43179])Sciurognathi:0.0229[&&NHX:D=N:B=2:T=33553],ENSTBEP00000006599:0.1232[&&NHX:D=N:G=ENSTBEG00000007644:T=37347])Euarchontoglires:0.0021[&&NHX:D=N:B=0:T=314146],(ENSMLUP00000004850:0.0394[&&NHX:D=N:G=ENSMLUG00000005316:T=59463],ENSBTAP00000006993:0.1035[&&NHX:D=N:G=ENSBTAG00000005603:T=9913])Laurasiatheria:0.0181[&&NHX:D=N:B=16:T=314145])Eutheria:0.0000[&&NHX:D=N:B=0:T=9347],((((ENSPTRP00000027808:0.0000[&&NHX:D=N:G=ENSPTRG00000016183:T=9598],ENSP00000306884:0.0000[&&NHX:D=N:G=ENSG00000169248:T=9606])Homo/Pan/Gorilla group:0.0076[&&NHX:D=N:B=100:T=207598],ENSMMUP00000027502:0.0141[&&NHX:D=N:G=ENSMMUG00000020904:T=9544])Catarrhini:0.0225[&&NHX:D=N:B=99:T=9526],ENSCPOP00000011196:0.0856[&&NHX:D=N:G=ENSCPOG00000012442:T=10141])Euarchontoglires:0.0031[&&NHX:D=N:B=17:T=314146],((ENSDNOP00000006372:0.0387[&&NHX:D=N:G=ENSDNOG00000008240:T=9361],ENSETEP00000002677:0.1001[&&NHX:D=N:G=ENSETEG00000003269:T=9371])Eutheria:0.0108[&&NHX:D=N:B=6:T=9347],ENSEEUP00000003875:0.1243[&&NHX:D=N:G=ENSEEUG00000004285:T=9365])Eutheria:0.0053[&&NHX:D=N:B=0:T=9347])Eutheria:0.0023[&&NHX:D=N:B=0:T=9347])Eutheria:0.5221[&&NHX:D=Y:B=0:T=9347])Mammalia:0.0000[&&NHX:D=Y:B=0:T=40674],(((((ENSDARP00000088355:0.0047[&&NHX:D=N:G=ENSDARG00000067756:T=7955],ENSDARP00000088357:0.0267[&&NHX:D=N:G=ENSDARG00000035283:T=7955])Danio rerio:0.0193[&&NHX:D=Y:B=76:T=7955],ENSDARP00000051098:0.0222[&&NHX:D=N:G=ENSDARG00000035267:T=7955])Danio rerio:0.1258[&&NHX:D=Y:B=76:T=7955],ENSDARP00000088622:0.2039[&&NHX:D=N:G=ENSDARG00000067905:T=7955])Danio rerio:0.0402[&&NHX:D=Y:B=9:T=7955],ENSDARP00000043819:0.1129[&&NHX:D=N:G=ENSDARG00000010064:T=7955])Danio rerio:0.1761[&&NHX:D=Y:B=9:T=7955],ENSDARP00000069427:0.2706[&&NHX:D=N:G=ENSDARG00000052987:T=7955])Danio rerio:0.1087[&&NHX:D=Y:B=83:T=7955])Euteleostomi:0.2327[&&NHX:D=N:B=3:T=117571])Euteleostomi:0.0";
		TreeIO.parseNewickString(new PhyloTree(), s);
		s = "(a,(b,c));";
		TreeIO.parseNewickString(new PhyloTree(), s);
	}

	public static String createNewickString(RootedTree tree,
			boolean includeStupidLabels)
	{
		StringBuffer sb = new StringBuffer();
		outputVertex(tree, sb, tree.getRoot(), includeStupidLabels);
		return sb.toString();
	}

	private static void outputVertex(RootedTree tree, StringBuffer sb,
			DefaultVertex v, boolean includeStupidLabels)
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

	private static String getNexusCompliantLabel(RootedTree t, DefaultVertex v,
			boolean includeStupidLabels)
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
