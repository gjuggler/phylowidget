package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.andrewberman.ui.FocusManager;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.Point;

public final class UIManager implements MouseMotionListener, MouseListener, MouseWheelListener
{
	PhyloWidget p = PhyloWidget.p;
	
	public static FocusManager focus = new FocusManager();
	public static EventDispatcher dispatch = new EventDispatcher();
	
	public UIManager()
	{
	}
	
	public void update()
	{
		NodeRange r = NearestNodeFinder.nearestNode(p.mouseX, p.mouseY);
		if (r != null)
		{
			Point pt = PhyloWidget.trees.getPosition(r);
			System.out.println(pt);
			p.ellipse(pt.x, pt.y, 10, 10);
		}
	}
	
	public void mouseEvent(MouseEvent e)
	{

	}
	
	public void mouseDragged(MouseEvent e){mouseEvent(e);}
	public void mouseMoved(MouseEvent e){mouseEvent(e);}
	public void mouseClicked(MouseEvent e){mouseEvent(e);}
	public void mouseEntered(MouseEvent e){mouseEvent(e);}
	public void mouseExited(MouseEvent e){mouseEvent(e);}
	public void mousePressed(MouseEvent e){mouseEvent(e);}
	public void mouseReleased(MouseEvent e){mouseEvent(e);}

	public void mouseWheelMoved(MouseWheelEvent e){mouseEvent(e);}
}
