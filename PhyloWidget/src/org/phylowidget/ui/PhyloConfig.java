package org.phylowidget.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.unsorted.MethodAndFieldSetter;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class PhyloConfig
{

	/*
	 * URL API CONFIGURATION
	 * 
	 * If you are running a database that works with phylogenetic information, the easiest way to "integrate"
	 * PhyloWidget with your service is to create "View in PhyloWidget" links using PhyloWidget's simple URL-based
	 * API. This allows you to customize the look, feel, and functionality of PhyloWidget without having to host
	 * the applet on your own site.
	 * 
	 * The API is simple: just choose from one of the configuration parameters listed below, and call the 
	 * PhyloWidget URL with the desired value appended to the end, as you would any normal GET parameters.
	 * The applet will then read in the configuration parameters and modify its settings accordingly. 
	 * 
	 * Some examples:
	 * - Set the tree that PhyloWidget shows upon startup.
	 *		http://www.phylowidget.org/lite/index.html?tree="(a,(b,c));"
	 * - Set the foreground color, using (R,G,B) format
	 *		http://www.phylowidget.org/lite/index.html?foreground="(255,0,0);"
	 * - Start up PhyloWidget with a preset tree and search string.
	 *		http://www.phylowidget.org/lite/index.html?tree="(a,(b,c));"&search="a"
	 * 
	 * The configurable parameters are displayed below, generally in order from most to least useful. Enjoy!
	 */

	/*
	 * If you specify a valid URL pointing to a properties file, then PhyloWidget will attempt to load the properties remotely.
	 * The format for the properties file is the standard .properties style, with a property name followed by an "=", followed
	 * by the property value. 
	 * 
	 * ### Example.properties ###
	 * tree="Homo sapiens"
	 * search="sapiens"
	 * renderer="Rectangular"
	 * nodeSize=5.0
	 */
	public String remoteConfig = "";

	/*
	 * Set the starting tree.
	 */
	public String tree = "Homo sapiens";
	/*
	 * Set the starting search string.
	 */
	public String search = "";
	/*
	 * Set the starting renderer type.
	 * 
	 * Possible values: Rectangular, Diagonal, and Circular
	 */
	public String renderer = "Rectangular";

	/*
	 * Choose the preset XML menu file which PhyloWidget will load.
	 * 
	 * Useful presets:
	 *   - "full.xml" (default) a full set of editing, viewing, and output menus.
	 *   - "dock-only.xml", a UI that only shows the pan and zoom tools.
	 *   - "view.xml", a dock and toolbar with only viewing (no editing) controls.
	 *   - "none.xml", Shows no menus at all.
	 */
	public String menuFile = "full.xml";

	/*
	 * Colors: You can modify the foreground and background colors which PhyloWidget uses.
	 * 
	 * The new value should be formatted as below; a triplet of integer RGB values enclosed in parentheses.
	 */
	public String backgroundColor = "(255,255,255)";
	public String textColor = "(0,0,0)";
	public String nodeColor = "(0,0,0)";
	public String branchColor = "(0,0,0)";

	/*
	 * Shapes -- usable values are:
	 *   - "square"
	 *   - "circle"
	 *   - "triangle"
	 */
	public String nodeShape = "circle";

	/*
	 * Should we color the branches based on the NHX bootstrap values (if they exist?)
	 * If true, then branches with low bootstrap will be faded.
	 */
	public boolean colorBranchesWithBootstrap = true;
	/*
	 * Should we color duplicated nodes based on the NHX annotations (if they exist)? If true, then duplicated nodes will
	 * show up red.
	 */
	public boolean colorNodesWithDuplications = true;

	public boolean colorBySpecies = true;

	/*
	 * The following parameters can be set using any numerical value, e.g. "textRotation = 0.25" 
	 */
	public float textRotation = 0f; // Text rotation, in degrees.
	public float textSize = 1.0f; // Text scaling, where a value of 1.0 is normal size.
	public float lineSize = 1f; // Line width. 0 is minimum, 1 is a pretty normal size. 10 is as high as you'll want to go.
	public float nodeSize = 2f; // Node size. Same range as line width: 0 to 10 is reasonable.
	public float renderThreshold = 150f; // Maximum number of nodes to render per frame.
	public float minTextSize = 10; // Minimum text size for leaf node labels.
	public float branchLengthScaling = 1f; // How much to scale the branch lengths?

	public boolean showBootstrapValues = false; // Should we show bootstrap values if they exist?
	public boolean showCladeLabels = false; // Should we show labels of non-leaf nodes?

	public boolean outputAllInnerNodes = false; // Kind of a strange one: if set to true, PhyloWidget will *always* output the labels of non-leaf nodes. Sometimes these are just stupid-looking numbers.
	public boolean enforceUniqueLabels = false; // Enforce uniqueness of node labels.
	public boolean stretchToFit = false;
	public boolean useBranchLengths = false; // Should the renderer display the tree using the branch length information?

	/*
	 * END: URL API Configuration
	 * 
	 * The rest is all just code...
	 */

	public PhyloConfig()
	{
		super();
		//		setRemoteConfig("http://www.phylowidget.org/nhx_test/ensembl.properties");
	}

	/*
	 * COLOR CRAP
	 */
	private Color backgroundC = Color.parseColor(backgroundColor);

	public void setBackgroundColor(String s)
	{
		backgroundC = Color.parseColor(s);
		backgroundColor = s;
	}

	public Color getBackgroundColor()
	{
		return Color.parseColor(backgroundColor);
	}

	private Color textC = Color.parseColor(textColor);

	public void setTextColor(String s)
	{
		textC = Color.parseColor(s);
		textColor = s;
	}

	public Color getTextColor()
	{
		if (textC != null)
			return textC;
		else
			return Color.parseColor(textColor);
	}

	private Color nodeC = Color.parseColor(nodeColor);

	public void setNodeColor(String s)
	{
		nodeC = Color.parseColor(s);
		nodeColor = s;
	}

	public Color getNodeColor()
	{
		if (nodeC != null)
			return nodeC;
		else
			return Color.parseColor(nodeColor);
	}

	private Color branchC = Color.parseColor(branchColor);

	public void setBranchColor(String s)
	{
		branchC = Color.parseColor(s);
		branchColor = s;
	}

	public Color getBranchColor()
	{
		if (branchC != null)
			return branchC;
		else
			return Color.parseColor(branchColor);
	}

	/*
	 * END COLOR CRAP
	 */

	public void setTree(String s)
	{
		PhyloWidget.trees.setTree(s);
		tree = s;
	}

	public void setUseBranchLengths(boolean useEm)
	{
		useBranchLengths = useEm;
		PhyloWidget.ui.layout();
	}

	public void setStretchToFit(boolean fitMe)
	{
		stretchToFit = fitMe;
	}

	public void setSearch(String s)
	{
		search = s;
		PhyloWidget.ui.search();
	}

	public void setEnforceUniqueLabels(boolean b)
	{
		enforceUniqueLabels = b;
		RootedTree t = PhyloWidget.trees.getTree();
		if (t != null)
			t.setEnforceUniqueLabels(b);
	}

	public void setRenderer(String s)
	{
		s = s.toLowerCase();
		if (s.equals("diagonal"))
		{
			PhyloWidget.trees.rectangleRender();
		} else if (s.equals("circular"))
		{
			PhyloWidget.trees.circleRender();
		} else
		{
			PhyloWidget.trees.rectangleRender();
		}
	}

	public void setRemoteConfig(String s)
	{
		try
		{
			HashMap<String, String> map = new HashMap<String, String>();
			URL url = new URL(s);
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));
			String line = null;
			while ((line = in.readLine()) != null)
			{
				if (line.startsWith("#"))
					continue;
				String[] split = line.split("=");
				if (split.length >= 2)
				{
					map.put(split[0], split[1]);
				}
			}
			MethodAndFieldSetter.setMethodsAndFields(this, map);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
