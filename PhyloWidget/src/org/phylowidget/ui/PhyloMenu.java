package org.phylowidget.ui;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.VerticalMenu;
import org.phylowidget.render.NodeRange;

import processing.core.PApplet;

public final class PhyloMenu extends VerticalMenu
{
	NodeRange curNode;
	Point nodePt = new Point(0,0);
	
	public PhyloMenu(PApplet p)
	{
		super(p);
	}

	public void draw()
	{
		if (curNode != null)
		{
			// Update our position based on the current menu node.
			curNode.render.getPosition(curNode.node,nodePt);
			setPosition(nodePt.x,nodePt.y);
		}
		super.draw();
	}

	public void show()
	{
		super.show();
//		PhyloWidget.ui.focus.setModalFocus(this);
	}
	
	public void hide()
	{
		super.hide();
//		PhyloWidget.ui.focus.removeFromFocus(this);
//		PhyloWidget.ui.hideMenu();
	}
	
	public void setNodeRange(NodeRange r)
	{
		curNode = r;
		curNode.render.getPosition(curNode.node,nodePt);
		this.setPosition(nodePt.x, nodePt.y);
	}
}
