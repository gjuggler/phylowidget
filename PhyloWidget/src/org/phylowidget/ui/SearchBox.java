package org.phylowidget.ui;

import java.awt.geom.Rectangle2D.Float;

import org.andrewberman.ui.Label;
import org.andrewberman.ui.LayoutUtils;
import org.andrewberman.ui.UIEvent;
import org.andrewberman.ui.ifaces.UIListener;
import org.andrewberman.ui.menu.TextBox;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;

import processing.core.PApplet;

public class SearchBox extends TextBox implements UIListener
{
	
	Label label;
	
	float myWidth;
	
	public SearchBox(PApplet p)
	{
		super(p);
		label = new Label(p);
		tf.addListener(this);
	}

	public void setLabel(String l)
	{
		label.setLabel(l);
		layout();
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
		LayoutUtils.centerVertical(tf, y, y+height);
		LayoutUtils.centerHorizontal(tf, x,x+width);

		tf.setTextSize(getFontSize());
		label.setFontSize(getFontSize() * .9f);
		LayoutUtils.centerVertical(label, y, y+height);
		label.setX(x+getPadX());
		
		tf.setX(label.getX()+label.getWidth()+getPadX());
	}
	
	@Override
	protected void calcPreferredSize()
	{
		super.calcPreferredSize();
		width = label.getWidth()+tf.getWidth()+getPadX()*3;
		height = tf.getHeight();
	}
	
	@Override
	public void setWidth(float newWidth)
	{
		super.setWidth(newWidth);
		tf.setWidth(newWidth - label.getWidth());
		menu.layout();
	}
	
	@Override
	protected void getRect(Float rect, Float buff)
	{
		super.getRect(rect, buff);
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
			PhyloWidget.cfg.search = getText();
			PhyloTree t = (PhyloTree) PhyloWidget.trees.getTree();
			t.search(getText());
		}
	}
	
}
