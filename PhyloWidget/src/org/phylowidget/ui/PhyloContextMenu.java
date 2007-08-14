package org.phylowidget.ui;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.RadialMenu;
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
		
		hover = new HoverHalo(p);
		hover.show();
	}

	protected void setOptions()
	{
		super.setOptions();
		this.focusOnShow = true;
	}
	
	public void draw()
	{
		if (!isVisible()) return;
		if (curNodeRange != null)
		{
			// Update our position based on the current menu node.
//			curNodeRange.render.getNodePosition(curNodeRange.node,nodePt);
			setPosition(curNodeRange.node.x,curNodeRange.node.y);
		}
		super.draw();
	}

	public void show(NodeRange r)
	{
		super.show();
		setNodeRange(r);
		hover.hide();
	}
	
	public void hide()
	{
		super.hide();
		hover.show();
		curNodeRange.node.hovered = false;
	}
	
	public void setNodeRange(NodeRange r)
	{
		curNodeRange = r;
		curNodeRange.node.hovered = true;
	}
}
