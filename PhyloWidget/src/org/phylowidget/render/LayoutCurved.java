package org.phylowidget.render;

import org.andrewberman.ui.UIUtils;
import org.phylowidget.tree.PhyloNode;

import processing.core.PGraphics;

public class LayoutCurved extends LayoutCladogram
{

	@Override
	public void drawLine(PGraphics canvas, PhyloNode p, PhyloNode c)
	{
		if (UIUtils.isJava2D(canvas))
		{
		canvas.strokeCap(canvas.ROUND);
		canvas.strokeJoin(canvas.ROUND);
		}
		canvas.noFill();
		canvas.beginShape();
		canvas.vertex(p.getX(), p.getY());
		canvas.vertex(p.getX(), c.getY());
		canvas.vertex(c.getX(), c.getY());
		canvas.endShape();
		if (UIUtils.isJava2D(canvas))
		{
			canvas.strokeCap(canvas.ROUND);
		}
	}

	
}
