package org.phylowidget.ui;

import org.andrewberman.ui.RadialPopupMenu;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.Point;

public final class PhyloMenu extends RadialPopupMenu
{
	
	NodeRange curNode;
	Point nodePt = new Point(0,0);
	
	public PhyloMenu()
	{
		super();
	}

	public void draw()
	{
		if (curNode != null)
		{
			// Update our position based on the current menu node.
			curNode.render.getPosition(curNode.node,nodePt);
			this.x = nodePt.x;
			this.y = nodePt.y;
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
	
	public void setNodeRange(NodeRange r)
	{
		curNode = r;
		curNode.render.getPosition(curNode.node,nodePt);
		this.x = nodePt.x;
		this.y = nodePt.y;
	}
}
