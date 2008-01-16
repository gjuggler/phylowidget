package org.phylowidget.ui;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.RadialMenu;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;

import processing.core.PApplet;

public final class PhyloContextMenu extends RadialMenu
{
	HoverHalo hover;
	
	NodeRange curNodeRange;
	Point nodePt = new Point(0,0);
	
	public PhyloContextMenu(PApplet p)
	{
		super(p);
	}

	public void setOptions()
	{
		super.setOptions();
		this.setRadii(10, 28);
	}
	
	public void draw()
	{
		if (curNodeRange != null)
		{
			// Update our position based on the current menu node.
			setPosition(curNodeRange.node.getRealX(),curNodeRange.node.getRealY());
		}
		super.draw();
	}

	public void open(NodeRange r)
	{
		menu.open(this);
		setNodeRange(r);
		aTween.continueTo(1f);
		aTween.fforward();
	}
	
	public void close()
	{
		super.close();
		PhyloWidget.ui.traverser.getCurRange();
	}
	
	private void setNodeRange(NodeRange r)
	{
		curNodeRange = r;
	}
}
