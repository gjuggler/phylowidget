package org.phylowidget.ui;

import java.awt.event.MouseEvent;

import org.andrewberman.ui.RadialMenu;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.Point;

public final class PhyloMenu extends RadialMenu
{
	PhyloWidget p = PhyloWidget.p;
	
	NodeRange curNode;
	Point pt = new Point(0,0);
	
	public PhyloMenu()
	{
		super(PhyloWidget.p);
	}

	public synchronized void draw()
	{
		if (curNode != null)
		{
			// Update our position based on the current menu node.
			curNode.render.getPosition(curNode.node,pt);
			this.setPosition(pt.x, pt.y);
		}
		super.draw();
	}

	public void show()
	{
		super.show();
		PhyloWidget.ui.focus.setModalFocus(this);
	}
	
	public void hide()
	{
		super.hide();
		PhyloWidget.ui.focus.removeFromFocus(this);
		PhyloWidget.ui.hideMenu();
	}
	
	protected void drawApproachingCircle()
	{
		// HoverHalo takes care of this.
		return;
	}
	
	public synchronized void setNodeRange(NodeRange r)
	{
		curNode = r;
	}
	
	public void mouseEvent(MouseEvent e)
	{
		if (this.isHidden())return;
		super.mouseEvent(e);
	}
	
}
