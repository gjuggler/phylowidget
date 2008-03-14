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
	 * The following parameters may be set using PhyloWidget's URL-based API.
	 * 
	 * In order to set a property, just append the desired parameter to the original PhyloWidget URL.
	 * 
	 * <b>Some examples:</b>
	 *  
	 * http://phylowidget.org/lite/index.html?tree="(a,(b,c));"
	 * 
	 */
	public String menuFile = "full.xml";
	public String foreground = "(0,0,0)";
	public String background = "(255,255,255)";
	public String tree = "Homo Sapiens";
	public String search = "";

	public float textRotation = 0f;
	public float textSize = 1.0f;
	public float lineSize = 0.1f;
	public float nodeSize = 0.15f;
	public float renderThreshold = 150f;
	public float minTextSize = 10;

	public boolean showBranchLengths = false;
	public boolean showCladeLabels = true;
	public boolean outputAllInnerNodes = false;
	public boolean enforceUniqueLabels = false;
	public boolean stretchToFit = false;
	public boolean antialias = false;
	public boolean useBranchLengths = false;
	
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
}
