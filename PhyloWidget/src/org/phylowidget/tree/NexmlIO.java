package org.phylowidget.tree;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.phylowidget.PhyloTree;
import org.phylowidget.tree.TreeIO.TreeOutputConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class NexmlIO
{

	static void addOtuToMap(HashMap<String, String> map, Element otu)
	{
		map.put(otu.getAttribute("id"), otu.getAttribute("label"));
	}

	static HashMap<String, String> createOtusMap(Element otus)
	{
		HashMap<String, String> otuMap = new HashMap<String, String>();
		NodeList otuEls = otus.getElementsByTagName("otu");
		for (int i = 0; i < otuEls.getLength(); i++)
		{
			Element el = (Element) otuEls.item(i);
			addOtuToMap(otuMap, el);
		}
		return otuMap;
	}

	public static void main(String[] args)
	{
		try
		{
			URL url = new URL("http://www.nexml.org/nexml/examples/tolweb.xml");
			InputStream in = url.openStream();
			NexmlIO io = new NexmlIO(PhyloTree.class);
			RootedTree tree = io.parseStream(in);
			System.out.println(tree.getNewick());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	HashMap<String, Class> classFactory;

	public NexmlIO(Class treeClass)
	{
		classFactory = new HashMap<String, Class>();
		classFactory.put("tree", treeClass);
	}
	
	private void addOtusToMap(HashMap<String, HashMap<String, String>> map, Element otus)
	{
		HashMap<String, String> otuMap = createOtusMap(otus);
		map.put(otus.getAttribute("id"), otuMap);
	}

	private void createEdge(Element el, HashMap<String, Object> nodeMap, RootedTree tree)
	{
		Object source = nodeMap.get(el.getAttribute("source"));
		Object target = nodeMap.get(el.getAttribute("target"));

		if (source == null || target == null)
		{
			System.err.println("Source or target is null! " + el.toString());
			System.exit(0);
		}

		Object edge = tree.addEdge(source, target);
		String length = el.getAttribute("length");
		if (length.length() > 0)
		{
			Double dblLength = Double.parseDouble(length);
			tree.setEdgeWeight(edge, dblLength);
		}
	}

	private Object createNode(Element el, RootedTree tree, HashMap<String, String> otusMap)
	{
		//		Object newNode = objFromElement(el);
		Object newNode = tree.createVertex();
		DefaultVertex node = (DefaultVertex) newNode;

		String nodeLabel = el.getAttribute("label");
		if (nodeLabel.length() > 0)
		{
			nodeLabel = nodeLabel.replaceAll("&amp;","&");
			node.setLabel(nodeLabel);
		}

		String label = otusMap.get(el.getAttribute("otu"));
		if (label != null && label.length() > 0)
		{
			label = label.replaceAll("&amp;","&");
			node.setLabel(label);
		}
		
		// Load annotations from any contained dict elements.
		List<Element> dicts = getSubElementsByName(el,"dict");
		for (Element dict : dicts)
		{
			List<Element> keyvals = getSubElementsByName(dict,"*");
			for (Element keyval : keyvals)
			{
				if (node instanceof PhyloNode)
				{
					PhyloNode pn = (PhyloNode) node;
					pn.setAnnotation(keyval.getNodeName(), keyval.getTextContent());
				}
			}
		}
		return node;
	}

	private List<Element> getSubElementsByName(Element el,String name)
	{
		NodeList subNodes = el.getElementsByTagName(name);
		ArrayList<Element> els = new ArrayList<Element>(subNodes.getLength());
		for (int i=0; i < subNodes.getLength(); i++)
		{
			els.add((Element) subNodes.item(i));
		}
		return els;
	}
		
	private Object objFromElement(Element elt)
	{
		String tagName = elt.getTagName();
		Object obj = null;
		try
		{
			obj = classFactory.get(tagName).newInstance();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// TODO: Automatically store the labels, IDs, etc.
		return obj;
	}

	public RootedTree parseDocument(Document xmlDoc)
	{
		HashMap<String, HashMap<String, String>> otuMaps = processOtus(xmlDoc.getElementsByTagName("otus"));
		HashMap<String, Object> trees = processTrees(xmlDoc.getElementsByTagName("trees"), otuMaps);

		// Todo: deal with multiple trees somehow.
		Iterator<String> it = trees.keySet().iterator();
		if (it.hasNext())
		{
			String key = it.next();
			return (RootedTree) trees.get(key);
		} else
		{
			return null;
		}
	}

	public RootedTree parseReader(Reader in) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return parseDocument(db.parse(new InputSource(in)));
	}

	public RootedTree parseStream(InputStream in) throws Exception
	{
		return parseReader(new InputStreamReader(in));
	}

	public RootedTree parseString(String s) throws Exception
	{
		return parseReader(new StringReader(s));
	}

	private HashMap<String, HashMap<String, String>> processOtus(NodeList otus)
	{
		HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
		for (int i = 0; i < otus.getLength(); i++)
		{
			Element el = (Element) otus.item(i);
			addOtusToMap(map, el);
		}
		return map;
	}

	private Object processTree(Element treeEl, HashMap<String, String> otuMap)
	{
		Object newTree = objFromElement(treeEl);
		RootedTree tree = (RootedTree) newTree;

		// Go through the nodes and add.
		HashMap<String, Object> nodeMap = new HashMap<String, Object>(); // Node ID to node object map.
		NodeList nl = treeEl.getElementsByTagName("node");
		for (int i = 0; i < nl.getLength(); i++)
		{
			Element el = (Element) nl.item(i);
			Object node = createNode(el, tree, otuMap);
			nodeMap.put(el.getAttribute("id"), node);
			tree.addVertex(node);
			if (el.getAttribute("root").length() > 0 && el.getAttribute("root").equals("true"))
			{
				tree.setRoot((DefaultVertex) node);
			}
		}

		// Go through edges and create.
		nl = treeEl.getElementsByTagName("edge");
		for (int i = 0; i < nl.getLength(); i++)
		{
			Element el = (Element) nl.item(i);
			createEdge(el, nodeMap, tree);
		}

		// Fix up the sorting.
		tree.fixSortingByAnnotation("first");
		
		return tree;
	}

	private HashMap<String, Object> processTrees(NodeList treesEls, HashMap<String, HashMap<String, String>> otusMap)
	{
		HashMap<String, Object> treesMap = new HashMap<String, Object>();
		for (int i = 0; i < treesEls.getLength(); i++)
		{
			Element el = (Element) treesEls.item(i);
			HashMap<String, String> otuMap = otusMap.get(el.getAttribute("otus"));
			NodeList trees = el.getElementsByTagName("tree");
			for (int j = 0; j < trees.getLength(); j++)
			{
				Element treeEl = (Element) trees.item(j);
				treesMap.put(treeEl.getAttribute("id"), processTree(treeEl, otuMap));
			}
		}
		return treesMap;
	}

	public static String createNeXMLString(RootedTree tree)
	{
		TreeOutputConfig config = new TreeOutputConfig();
		config.outputNHX = true;
		return createNeXMLString(tree, config);
	}

	private static String createNeXMLString(RootedTree tree, TreeOutputConfig config)
	{
		int nodeId = 0;
		int edgeId = 0;
		int otuId = 0;
		int otusId = 0;
		int treeId = 0;
		int treesId = 0;
		int dictId = 0;
		int globalDummyId = 0;
		StringBuffer sb = new StringBuffer();

		String boilerplate = "<nex:nexml version=\"0.8\" " +
				"xmlns=\"http://www.nexml.org/1.0\" " +
				"xmlns:nex=\"http://www.nexml.org/1.0\" " +
				"xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" "+
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "+
				"xsi:schemaLocation=\"http://www.nexml.org/1.0 http://www.nexml.org/1.0/nexml.xsd\" " +
				" >";
		
//		addLine(sb, "<?xml version='1.0' encoding='ISO-8859-1'?>", 0);
		addLine(
			sb,
			boilerplate,
			0);

		addLine(sb, "<otus id=\"otus1\" label=\"onlyOtus\">", 1);
		List leaves = tree.getAllLeaves();
		HashMap<Object, String> leafToOtuId = new HashMap<Object, String>();
		for (Object leaf : leaves)
		{
			int myId = otuId++;
			String otuIdS = "otu" + myId;
			String label = tree.getLabel((DefaultVertex) leaf);
			label = escapeXml(label);
			addLine(sb, "<otu id=" + qw(otuIdS) + " label=" + qw(label) + "/>", 2);
			leafToOtuId.put(leaf, otuIdS);
		}
		addLine(sb, "</otus>", 1);

		addLine(sb, "<trees id=\"trees1\" otus=\"otus1\" label=\"onlyTrees\">", 1);
		addLine(sb, "<tree id=\"tree1\" label=\"onlyTree\" xsi:type=\"nex:FloatTree\" >", 2);

		List<DefaultVertex> nodes = tree.getAllNodes();
		HashMap<Object,String> nodeToId = new HashMap<Object,String>();
		for (DefaultVertex o : nodes)
		{
			nodeId++;
			String nodeIdString = "node"+nodeId;
			String labelAndRoot = "";
			if (tree.getLabel(o).length() > 0)
				labelAndRoot += " label="+qw(tree.getLabel(o))+" ";
			if (tree.isRoot(o))
				labelAndRoot += " root="+qw("true")+" ";
			if (leafToOtuId.containsKey(o))
			{
				labelAndRoot += " otu="+qw(leafToOtuId.get(o))+" ";
			}
			labelAndRoot = escapeXml(labelAndRoot);

			HashMap<String,String> anns = null;
			if (o instanceof PhyloNode)
			{
				PhyloNode pn = (PhyloNode) o;
				PhyloNode parent = (PhyloNode) tree.getParentOf(pn);
				if (parent != null)
				{
					if (pn == tree.getFirstChild(parent))
					{
						pn.setAnnotation("first", "y");
					} else
					{
						pn.clearAnnotation("first");
					}
				}
				anns = pn.getAnnotations();
				if (anns != null && anns.keySet().size() == 0)
				{
					anns = null;
				}
			}
			if (anns != null)
			{
				addLine(sb, "<node id="+qw(nodeIdString)+" "+labelAndRoot+">",3);
				
				dictId++;
				String dictIdString = "dict"+dictId;
				addLine(sb,"<dict id="+qw(dictIdString)+">",4);
				for (String s : anns.keySet())
				{
					globalDummyId++;
					String dummyString = qw("dummy"+globalDummyId);
					addLine(sb,"<"+s+" id="+dummyString+">"+escapeXml(anns.get(s))+"</"+s+">",5);
				}
				addLine(sb,"</dict>",4);
				addLine(sb,"</node>",3);
			} else
			{
				addLine(sb, "<node id="+qw(nodeIdString)+" "+labelAndRoot+"/>",3);
			}
			
			
			nodeToId.put(o, nodeIdString);
		}
			
		// Go through and add edges.
		for (DefaultVertex o : nodes)
		{
			if (tree.getParentOf(o) != null)
			{
				edgeId++;
				String edgeIdString = "edge"+edgeId;
				String sourceAndTarget = "";
				String targetId = nodeToId.get(o);
				String sourceId = nodeToId.get(tree.getParentOf(o));
				sourceAndTarget = " source="+qw(sourceId)+" target="+qw(targetId)+" ";
				
				String length = " length="+qw(tree.getBranchLength(o)+"")+" ";
				addLine(sb, "<edge id="+qw(edgeIdString)+" "+sourceAndTarget+length+"/>",3);
			}
		}

		addLine(sb, "</tree>", 2);
		addLine(sb, "</trees>", 1);
		addLine(sb, "</nex:nexml>", 0);

		return sb.toString();
	}

	private static String escapeXml(String s)
	{
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		s = s.replaceAll("&", "&amp;");
		return s;
	}
	
	private static String qw(String s)
	{
		return "\"" + s + "\"";
	}

	private static void addLine(StringBuffer sb, String line, int depth)
	{
		String tab = "";
		for (int i = 0; i < depth; i++)
		{
			tab += "  ";
		}
		sb.append(tab + line + "\n");
	}

}
