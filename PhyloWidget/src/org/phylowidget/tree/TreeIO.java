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

//	public static final String spnNwk = "(((((((((((((((((((((((((((((699:0.00001396,10388:0.00082626):0.00122026,IO11614:0.00046183):0.00020924,(2067-9:0.00042743,397-9:0.00083284):0.00022616):0.00003806,(436-24:0.00078357,(1522-2:0.00059423,(456-218:0.00064366,52-1:-0.00001361):0.00024609):0.00004507):0.00010503):0.00010178,(IO12371:0.00022897,(04-029:0.00045642,645-2:0.00038340):0.00019094):0.00000477):0.00006029,(32-0:0.00021532,(IO11062:0.00027185,((04-043:0.00148718,463-6:0.00040378):0.00020870,(3085:0.00078738,(721:0.00020704,3038:0.00168534):0.00089705):0.00005461):0.00018948):0.00024618):-0.00009321):0.00014741,(696:0.00085900,442-8:0.00040175):0.00046370):0.00007761,((518:0.00116770,492-3:0.00093414):0.00030014,(10001:0.00144617,(1305-9:0.00022599,170-6:0.00061424):0.00023654):0.00018296):0.00021704):0.00010923,((839:-0.00001178,521-7:0.00022173):0.00029105,(2117:0.00028513,IO12665:0.00034478):0.00012899):0.00011244):0.00003034,(SK0618:0.00187602,(3126:0.00071311,04-041:0.00244249):0.00128731):0.00035239):0.00006993,(452-316:0.00146409,(748-0:0.00108330,IO10916:0.00101955):0.00021881):0.00028704):0.00014261,(((IO11330:0.00070561,IO12273:0.00055466):0.00019282,705-14:0.00106879):0.00013499,(((10692:0.00020558,11698:0.00021427):0.00000565,6045-75:-0.00000566):0.00069799,((3027:0.00078875,1953-86:0.00068222):0.00012472,(1909:0.00080705,(3026:0.00116354,(3169:0.00063246,420-1215:0.00083935):0.00051872):0.00084458):0.00017445):0.00008841):0.00018283):0.00039522):0.00069864,11995:0.00202698):0.00377560,768-1:0.00590838):0.00850120,D76:0.01185736):0.00405722,SK0597:0.01753691):0.00126729,((((((((SK0626:0.00710854,SK0639:0.00860345):0.00231704,SK0636:0.00963248):0.00195635,SK0648:0.01248775):-0.00003327,(SK0635:-0.00001648,SK0640:0.00022643):0.01236243):0.00101609,(SK0649:0.01259310,SK0653:0.01484149):0.00180537):0.00208311,SK0675:0.01495158):0.00111593,((TOK3:0.01235745,TOK6:0.01260113):0.00347315,(VS04:0.00495169,VS44:0.00562171):0.00898705):0.00107133):-0.00000551,((SK0601:0.01643670,SK0630:0.01358381):0.00114222,((((SK0564:0.00887390,VS50:0.01002526):0.00313100,(SK0612:0.01294241,SK0677:0.01249987):0.00091743):0.00033593,SK0599:0.01631213):0.00160744,((((((SK0574:0.00021868,SK0575:-0.00000877):0.00859089,SK0602:0.00796078):0.00232676,SK0632:0.00843474):0.00241739,(SK0608:0.01128469,SK0609:0.01066190):0.00191461):0.00111016,(SK0598:0.01522339,TH1:0.01173248):0.00104379):0.00044993,((98-1:0.00018295,1362-8:0.00023706):0.01172359,((((0103:0.00372590,D63:0.00260102):0.00333413,(731-5:0.00143048,D449:0.00172537):0.00505455):0.00110746,809:0.00684760):0.00221815,(D450:0.01206869,((((0108:0.00087507,SK0674:0.00122684):0.00290825,484:0.00427590):0.00394193,3074:0.00634831):-0.00018074,(220-8:0.00817656,(IOPR171169:0.00901977,(2640:0.00403756,(869:0.00039112,(226-9:-0.00000045,(290:-0.00000056,((769:0.00000000,2716:0.00000000):0.00000000,(3458:0.00000000,5052:0.00000000):0.00000000):0.00021051):0.00000011):0.00023879):0.00336450):0.00334081):0.00114759):-0.00011661):0.00419768):-0.00232992):0.00616445):0.00064737):0.00137966):0.00093309):-0.00007143):0.00001937):0.00252834,(((((((SK0578:0.00830114,VS51:0.00977119):0.00023451,VS54:0.00932592):0.00477949,(VS42:0.01333227,(SK0646:0.00912651,VS27:0.01157120):0.00152078):0.00267744):0.00058581,((SK0661:0.01363547,VS28:0.01464367):0.00081199,(VS29:0.01192345,(VS16:0.01032132,VS20:0.01099322):0.00386885):0.00303033):0.00033579):0.00157553,(TG2:0.01795093,VS19:0.01425606):0.00257816):0.00222330,((SK0629:0.02282941,VS31:0.01855304):0.00125598,(SK0659:0.01453706,(SK0631:0.00857170,VS24:0.00842886):0.00584009):0.00494543):0.00058208):0.00078572,((((((VS32:0.00987991,VS36:0.01166430):0.00148932,VS33:0.01253996):0.00072252,(SK0024:0.00842890,SK0321:0.01031008):0.00251592):0.00334216,VS56:0.01772667):0.00146742,VS34:0.01723172):0.00038800,((((((SK0271:0.00713096,SK0322:0.00704996):0.00259272,TOK2:0.00859444):0.00125297,(SK0334:0.01134159,SK0572:0.00951827):0.00061405):0.00210514,(SK0568:0.01102703,VS05:0.01030561):0.00250218):0.00483624,(SK0596:0.01314580,SK1073:0.01364891):0.00303563):0.00113191,((SK0650:0.01728776,VS38:0.01804513):0.00147222,(SK0611:0.01526852,((SK0637:0.01121534,SK0641:0.01247993):0.00385782,(SK0138:0.01438308,(SK0135:0.01117793,(SK0634:0.01150333,(SK0137:0.00897304,SK0145:0.00928525):0.00133103):0.00337450):0.00088705):0.00142787):0.00133526):0.00107866):-0.00081895):0.00017120):0.00078585):0.00141581):0.00296677,(SK0627:0.01352965,SK0642:0.01477083):0.00976521):0.00013991,(((SK0607:0.01719707,SK0651:0.01682199):0.00112320,(SK0667:0.01238317,VS10:0.01391417):0.00822648):0.00594948,(((SK0569:0.01317135,SK0616:0.01532783):0.00033085,SK0579:0.01856122):0.00323365,(VS58:0.01683977,(SK0614:0.01392899,SK0615:0.01658676):0.00050997):0.00548890):0.00138864):0.00143048):0.00433464,VS25:0.02495115):0.00284365,(SK0643:0.03115957,(SK0624:0.00566213,SK0647:0.00554177):0.02444371):0.01273693):0.01022653,((((((((SK0152:0.00000000,SK0400:0.00000000):0.01081162,SK0565:0.01181917):0.00507732,(SK0034:0.01399828,SK0103:0.01632985):0.00151964):0.00197196,SK0096:0.01918686):0.00341905,VS57:0.01805835):-0.00126980,(SK0413:0.02245886,(SK0079:0.01750204,VS09:0.01806997):0.00160467):0.00058037):0.00316238,(SK0095:0.02378740,SK0429:0.02675488):0.00057769):0.00081272,((((((((2315:0.01710747,SK0255:0.01384970):0.00296393,(SK0664:0.01131946,VS07:0.01240440):0.00420567):0.00139466,SK1075:0.01506421):0.00387377,(VS01:0.00982980,VS03:0.00676745):0.00677760):-0.00009743,((SK0734:0.01833351,SK1084:0.01619838):0.00357576,(SK1074:0.01485641,(SK0313:0.00845689,(SK0305:0.00000000,SK0567:0.00000000):0.00449038):0.01435586):0.00031615):0.00060395):0.00355363,(SK0304:0.02306054,SK0555:0.01581960):0.00247930):0.00588492,SK0394:0.02305960):0.00433335,(((((VS21:0.00000000,VS26:0.00000000):0.00251092,SK0595:0.00191114):0.00406744,(VS12:0.00444557,VS55:0.00400607):0.00318876):0.00535858,SK0576:0.01201205):0.00403050,(((((((((2022:0.00668005,SK0328:0.00710998):0.00148721,SK0323:0.01060703):0.00144683,SK0010:0.01137637):0.00110422,VS48:0.01194113):0.00250017,VS39:0.01202597):0.00149884,(SK0326:0.01498731,(VS22:0.01427343,(SK0039:0.00912580,VS23:0.00659629):0.00411187):0.00235655):0.00023201):0.00020283,(SK0113:0.01043689,(SK0155:0.01078349,VS13:0.00970893):0.00295045):0.00295725):0.00191416,(VS08:0.00978074,VS43:0.01199149):0.00720217):-0.00023597,(((SK0342:0.00663392,TE-1:0.00503108):0.01148487,SK0570:0.01955765):0.00015677,((((SK0285:0.00332823,SK0286:0.00384993):0.01203751,SK0105:0.01468640):0.00171494,VS15:0.01554723):0.00058017,((((((((SK0327:0.00019162,SK0337:0.00001833):0.00391228,SK0100:0.00305043):0.00291520,(SK0562:0.00594452,16-080:0.00527842):0.00196678):0.00239976,(SK0580:0.00991198,VS30:0.00710195):0.00079201):0.00204871,2278:0.01123727):0.00397770,VS14:0.01467591):-0.00007828,(2279:0.01573715,SK0335:0.01258492):0.00045757):0.00042008,(((2186:0.00186238,SK0309:0.00256135):0.01208449,(SK0141:0.01367839,VS37:0.01272393):0.00213181):0.00161479,((SK0610:0.00722879,VS47:0.00849013):0.00357494,((SK0397:0.00878111,SK1077:0.01106176):0.00320026,((2394:0.01027111,SK0143:0.00999169):0.00270094,(VS06:0.00744976,(SK1083:0.00597882,VS02:0.00610334):0.00146581):0.00430855):0.00167674):0.00051618):0.00063739):0.00059225):0.00104521):0.00165201):0.00004964):0.00124904):0.00475783):0.00138144):0.01237565):0.00646414,((((((SK0645:0.01469957,SK0660:0.01470890):0.00112400,SK0969:0.01647526):0.00559251,SK0655:0.02281428):-0.00045065,(SK0970:0.02121965,(SK0959:0.01721351,TI-1:0.01547085):0.00402696):0.00080473):0.01397794,SK0958:0.05118965):-0.00405875,((SK0654:0.02163672,SK1076:0.02306124):0.00516778,((((SK0385:0.01661181,SK0390:0.01367835):0.00109145,SK0140:0.01336687):0.00231076,(SK0350:0.02394989,SK0656:0.01439858):0.00100642):0.00232643,(((SK0282:0.00227200,SK0283:0.00342274):0.00841919,SK0605:0.01215552):0.00337680,((SK0613:0.01163475,VS35:0.01098943):0.00352739,(SK0657:0.01329718,(SK0603:0.01294041,SK0644:0.01405932):0.00053935):0.00106099):0.00186107):0.00496675):0.00437837):0.00405804):0.01327547):0.01458329,(((((SK0306:0.01388375,SK0590:0.01442336):0.00964558,(SK0428:0.01508067,SK0557:0.01498246):0.00479738):0.00247496,SK0357:0.03015715):0.00022380,(SK0307:0.01914277,(SK0716:0.01553790,(SK0956:0.00000000,SK0957:0.00000000):0.01567348):0.00351793):0.00683917):0.02502149,(SK0264:0.02694383,((SK0968:0.01512871,SK1024:0.02160999):0.00356332,(SK0236:0.01520684,((SK0254:0.01382264,SK1097:0.01210158):0.00260393,(SK0154:0.01491311,(((SK0971:0.01013835,VS18:0.01032121):0.00212377,SK0974:0.00998935):0.00246492,(SK0972:0.01056846,(SK0973:0.01050149,SK1101:0.01213642):0.00065724):0.00068664):0.00185079):-0.00026210):0.00369048):0.00197081):0.00699297):0.02182301):0.01406499):0.01147879,(((((((SK0589:0.01044232,1058:0.01373835):0.00620714,1059:0.02261848):0.00056172,SK0678:0.01872825):0.00452945,SK0355:0.02308016):0.03774415,SK1072:0.05943355):0.02402668,VS59:0.05233267):-0.01040149,(SK0003:0.02124275,(SK0325:0.01732212,((((SK0006:0.00677569,SK0186:0.00615343):0.00414198,(SK0185:0.00942000,SK0391:0.00799944):0.00146651):0.00525619,(SK0392:0.01198527,SK0700:0.01175134):0.00322161):0.00123273,((((SK0007:0.00279445,SK0592:0.00290051):0.00431807,SK0183:0.00736184):0.00662790,SK0384:0.01502937):0.00400768,(((SK0008:-0.00001709,SK0009:0.00022700):0.01019100,(SK0402:0.00794800,SK1023:0.00905393):0.00265333):0.00295986,(SK0121:0.02268730,(SK0332:0.00000000,(SK0333:0.00000000,(SK0336:0.00000000,(SK1104:0.00000000,SK1105:0.00000000):0.00000000):0.00000000):0.00000000):0.01488884):-0.00153216):0.00331016):0.00229701):0.00862466):0.00373336):0.01986655):0.02511153):0.00631634,(SK0341:0.01606320,(SK0315:0.01345515,(SK0410:0.00931128,VS52:0.01246514):0.00214243):0.00167402):0.10152530):0.01096718,(SK0244:0.00249336,(SK0316:0.00486228,(SK0225:0.00306954,SK0240:0.00072313):0.00062592):0.00060503):0.09214876):0.00768890,P1/7:0.11417674,pyogenes:0.12400737);";
	
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

	public static RootedTree parseNewickString(RootedTree tree, String s)
	{
//		System.out.println(s);
//		System.out.println("Spn Nwk = "+s.equals(spnNwk));
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
					Object[] returnO = newNode(tree, curLabel);
					DefaultVertex curNode = (DefaultVertex) returnO[0];
					Double curLength = ((Double)returnO[1]).doubleValue();
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
//					curLength = 1.0;
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

	static Object[] newNode(RootedTree t, String s)
	{
		PhyloNode v = new PhyloNode();
		Double curLength = new Double(0.0);

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
				curLength = new Double(Double.parseDouble(length));
			} catch (Exception e)
			{
				curLength = new Double(1);
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
				return new Object[]{existingNode,curLength};
		}
		//		DefaultVertex o = t.createAndAddVertex();
		t.addVertex(v);
		t.setLabel(v, s);
		return new Object[]{v,curLength};
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
