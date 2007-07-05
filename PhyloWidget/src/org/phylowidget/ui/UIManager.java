package org.phylowidget.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.andrewberman.sortedlist.XYRange;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.HoverHalo;
import org.andrewberman.ui.TextInput;
import org.andrewberman.ui.ProcessingUtils;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.TreeNode;

import processing.core.PApplet;

public final class UIManager implements MouseMotionListener, MouseListener
{
	PhyloWidget p = PhyloWidget.p;
	
	public static FocusManager focus;

	public UIManager()
	{
		focus = new FocusManager(p);	
	}
	
	public void draw()
	{
		updateHover();
	}

	Point2D.Float mpt = new Point2D.Float();
	float minX,minY,minDist=0;
	NodeRange minRange;
	ArrayList hits = new ArrayList();
	
	public void updateHover()
	{
		if (minRange == null) return;
		float cx = (minRange.loX + minRange.hiX) / 2;
		float cy = (minRange.loY + minRange.hiY) / 2;
//		h.setRect(cx, cy, minRange.hiX-minRange.loX, minRange.hiY-minRange.loY);
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
}
