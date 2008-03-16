package org.phylowidget.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.menu.MenuIO;
import org.andrewberman.ui.unsorted.MethodAndFieldSetter;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class PhyloConfig
{
	PApplet p;

	/*
	 * URL API Configuration
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
	 * Set the starting tree.
	 */
	public String tree = "Homo Sapiens";
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
	public String foreground = "(0,0,0)";
	public String background = "(255,255,255)";
	
	/*
	 * The following parameters can be set using any numerical value, e.g. "textRotation = 0.25" 
	 */
	public float textRotation = 0f;			// Text rotation, in degrees.
	public float textSize = 1.0f;			// Text scaling, where a value of 1.0 is normal size.
	public float lineSize = 0.1f;			// Line width. Ranges between 0 and 0.5 work best.
	public float nodeSize = 0.15f;			// Node size. Again, ranges between 0 and 0.5 are sensible.
	public float renderThreshold = 150f;	// Maximum number of nodes to render per frame.
	public float minTextSize = 10;			// Minimum text size for leaf node labels.

	public boolean showBranchLengths = false;
	public boolean showCladeLabels = true;
	public boolean outputAllInnerNodes = false;		// Kind of a strange one: if set to true, PhyloWidget will *always* output the labels of non-leaf nodes. Sometimes these are just stupid-looking numbers.
	public boolean enforceUniqueLabels = false;
	public boolean stretchToFit = false;
	public boolean antialias = false;
	public boolean useBranchLengths = false;		// Should the renderer display the tree using the branch length information?

	/*
	 * END: URL API Configuration
	 * 
	 * The rest is all just code...
	 */

	public PhyloConfig(PApplet p)
	{
		this.p = p;
	}

	private Color backgroundC = Color.parseColor(background);

	public void setBackground(String s)
	{
		backgroundC = Color.parseColor(s);
		background = s;
	}

	public Color getBackground()
	{
		return backgroundC;
	}

	private Color foregroundC = Color.parseColor(foreground);

	public void setForeground(String s)
	{
		foregroundC = Color.parseColor(s);
		foreground = s;
	}

	public Color getForeground()
	{
		return foregroundC;
	}

	public void setTree(String s)
	{
		PhyloWidget.trees.setTree(s);
		tree = s;
	}

	public void setUseBranchLengths(boolean useEm)
	{
		//		System.out.println("Hey!");
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
}
