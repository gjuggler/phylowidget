package org.phylowidget.ui;

import org.andrewberman.ui.tools.Tool;
import org.phylowidget.PWContext;
import org.phylowidget.PhyloWidget;

import processing.core.PApplet;

public class NodeSelector extends Tool
{
	PhyloWidget pw;
	PWContext pwc;
	
	public NodeSelector(PApplet p)
	{
		super(p);
		this.pw = (PhyloWidget)p;
		this.pwc = pw.pwc;
	}

	
	
}
