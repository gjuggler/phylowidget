package org.phylowidget.ui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Label;
import org.andrewberman.ui.LayoutUtils;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIEvent;
import org.andrewberman.ui.ifaces.UIListener;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.menu.TextBox;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;

import processing.core.PApplet;

public class SearchBox extends TextBox implements UIListener
{
	PWContext context;
	Label label;

	float myWidth;

	public SearchBox(PApplet p)
	{
		super(p);
		context = PWPlatform.getInstance().getThisAppContext();
		label = new Label(p);
		tf.addListener(this);
	}

	public void setLabel(String l)
	{
		label.setLabel(l);
		layout();
	}
	
	@Override
	public void setOptions()
	{
		super.setOptions();
		
		
	}

	@Override
	public synchronized void draw()
	{
		label.alpha = parent.getNearestMenu().alpha;
		super.draw();
	}
	
	@Override
	public void setName(String name)
	{
		super.setName(name);
		setLabel(name);
	}

	@Override
	public synchronized void dispose()
	{
		super.dispose();
		label.dispose();
	}

	@Override
	public void layout()
	{
		//		tf.setWidth(width-getStyle().padX);
		//		tf.setHeight(height-getStyle().padY);
		LayoutUtils.centerVertical(tf, y, y + height);
		LayoutUtils.centerHorizontal(tf, x, x + width);

		tf.setTextSize(getFontSize());
		label.setFontSize(getFontSize() * .9f);
		LayoutUtils.centerVertical(label, y, y + height);
		label.setX(x + getPadX());

		tf.setX(label.getX() + label.getWidth() + getPadX());
	}

	@Override
	protected void calcPreferredSize()
	{
		super.calcPreferredSize();
		width = label.getWidth() + tf.getWidth() + getPadX() * 3;
		height = tf.getHeight();
	}

	@Override
	protected void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);
	}
	
	@Override
	protected void visibleMouseEvent(MouseEvent e, Point tempPt)
	{
		super.visibleMouseEvent(e, tempPt);
		
		if (mouseInside)
		{
			parent.getNearestMenu().setState(this, MenuItem.OVER);
		} else
		{
			parent.getNearestMenu().setState(this, MenuItem.UP);
		}
	}
	
	@Override
	public void setWidth(float newWidth)
	{
		super.setWidth(newWidth);
		tf.setWidth(newWidth - label.getWidth());
		menu.layout();
	}

	@Override
	protected boolean containsPoint(Point p)
	{
		buffRect.setRect(x, y, width, height);
		if (tf.containsPoint(p))
		{
			return false;
		}
		return buffRect.contains(p);
//		return super.containsPoint(p);
	}
	
	public String getText()
	{
		return tf.getText();
	}

	public void setText(String s)
	{
		tf.replaceText(s);
	}

	public void uiEvent(UIEvent e)
	{
		if (e.getID() == UIEvent.TEXT_VALUE)
		{
			context.config().search = getText();
			PhyloTree t = (PhyloTree) context.trees().getTree();
			if (t != null)
			{
				t.searchAndMarkFound(getText());
			}
		}
	}

}
