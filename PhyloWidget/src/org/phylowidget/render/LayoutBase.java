package org.phylowidget.render;

import java.awt.geom.Rectangle2D;

import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloConfig;

import processing.core.PApplet;
import processing.core.PGraphics;

public abstract class LayoutBase
{
	protected RootedTree tree;
	protected PhyloNode[] leaves;
	protected PhyloNode[] nodes;

	protected PWContext context = PWPlatform.getInstance().getThisAppContext();
	
	protected double loX;
	protected double loY;
	protected double hiX;
	protected double hiY;

	public static final float TWOPI = (float) (Math.PI*2);
	public static final float PI = (float) Math.PI;
	
	protected int angleHandling;
	public static final int ANGLE_NONE = 0;
	public static final int ANGLE_QUANTIZE = 1;
	public static final int ANGLE_LEVEL = 2;

	float scaleX;
	float scaleY;
	float dX;
	float dY;
	
	float drawScaleX;
	float drawScaleY;
	
	public void layout(RootedTree tree, PhyloNode[] leaves, PhyloNode[] nodes)
	{
		this.tree = tree;
		this.leaves = leaves;
		this.nodes = nodes;

		loX = loY = Double.MAX_VALUE;
		hiX = hiY = Double.MIN_VALUE;

		PhyloConfig cfg = PWPlatform.getInstance().getThisAppContext().config();
		if (cfg.angleHandling.toLowerCase().equals("quantize"))
			angleHandling = ANGLE_QUANTIZE;
		else if (cfg.angleHandling.toLowerCase().equals("level"))
			angleHandling = ANGLE_LEVEL;
		else
			angleHandling = ANGLE_NONE;

		layoutImpl();
		
		// Now, take the arbitrarily-scaled layout and transform it to fit into the unit square (0,1).
		rect.setFrame(loX, loY, hiX - loX, hiY - loY);
		dX = -rect.x;
		dY = -rect.y;
		scaleX = 1f / rect.width;
		scaleY = 1f / rect.height;
		
		float scale = PApplet.min(scaleX,scaleY);
		if (Float.isInfinite(scale))
			scaleX = scaleY = scale = 0;
		else
			scaleX = scaleY = scale;
		
		// Center the layout properly if it's not square.
		float offsetX = 0;
		float offsetY = 0;
		if (scale * rect.width < 1 && scale != 0)
		{
			offsetX = (1 - scale * rect.width) / 2;
		}
		if (scale * rect.height < 0.9 && scale != 0)
		{
			offsetY = (1 - scale * rect.height) / 2;
		}
		
		for (PhyloNode n : nodes)
		{
			n.setPosition((n.getLayoutX() + dX) * scaleX + offsetX, (n.getLayoutY() + dY) * scaleY + offsetY);
		}
		
		// Keep the node centered and on the left if it's the only one.
		if (leaves.length < 10 && (this instanceof LayoutCladogram || this instanceof LayoutDiagonal))
		{
			float offsetAmount = (10f-leaves.length) / 10f;
			float maxOffset = -1f;
			float dX = offsetAmount * maxOffset;
//			System.out.println(dX);
			for (PhyloNode n : nodes)
			{
				n.setPosition(n.getLayoutX()+dX,n.getLayoutY());
			}
		}
	}

	protected void setPosition(PhyloNode n, float newX, float newY)
	{
		n.setPosition(newX, newY);

		if (newX < loX) // If this node extends the bounds of the rectangle, update them.
			loX = newX;
		if (newX > hiX)
			hiX = newX;
		if (newY < loY)
			loY = newY;
		if (newY > hiY)
			hiY = newY;
	}

	public abstract void drawLine(PGraphics canvas, PhyloNode p, PhyloNode c);

	protected abstract void layoutImpl();

	private Rectangle2D.Float rect = new Rectangle2D.Float();

	static final boolean between(float a, float lo, float hi)
	{
		if (a <= hi && a >= lo)
			return true;
		else
			return false;
	}

	protected final void setAngle(PhyloNode n, float theta)
	{
		theta += (float)TWOPI;
		theta %= (float)TWOPI;
		n.setAngle(theta);
		String s = n.getAnnotation("collapse");
		if (s != null && PhyloNode.parseTruth(s))
		{
			return;
		}
		switch (angleHandling)
		{
			case (ANGLE_NONE):
				noneAngles(n);
				break;
			case (ANGLE_QUANTIZE):
				quantizeAngles(n);
				break;
			case (ANGLE_LEVEL):
				levelAngles(n);
				break;
		}
	}

	protected float getLayoutMult(PhyloNode n)
	{
		float lm = 1;
		String layoutSize = n.getAnnotation("layout_size");
		if (layoutSize != null)
		{
			lm = Float.parseFloat(layoutSize);
		}
		return lm;
	}

	private static final void noneAngles(PhyloNode n)
	{
		float theta = n.getAngle();
		float degrees = theta / (float) (Math.PI * 2) * 360f;
		boolean alignRight = false;
		if (between(degrees,0,90) || between(degrees,270,360))
		{
			;
		} else
		{
			alignRight = true;
			degrees += 180;
		}
		
		setDegreesAndAlignment(n, degrees, alignRight);
	}

	private static final void setDegreesAndAlignment(PhyloNode n, float degrees, boolean alignRight)
	{
		if (alignRight)
			n.setTextAlign(PhyloNode.ALIGN_RIGHT);
		else
			n.setTextAlign(PhyloNode.ALIGN_LEFT);
		n.setAngle(degrees / 360f * 2 * (float) Math.PI);
	}
	
	private static final void levelAngles(PhyloNode n)
	{
		float theta = n.getAngle();
		float degrees = theta / (float) (Math.PI * 2) * 360f;
		degrees = degrees % 360;
		
		boolean alignRight = false;
		if (between(degrees, 0, 90) || between(degrees, 270, 360))
		{
			degrees = 0;
			alignRight = false;
		} else
		{
			degrees = 0;
			alignRight = true;
		}

		setDegreesAndAlignment(n, degrees, alignRight);
	}

	private static final void quantizeAngles(PhyloNode n)
	{
		float theta = n.getAngle();
		float degrees = theta / (float) (Math.PI * 2) * 360f;
		degrees += 720;
		degrees %= 360;
		float oldDegrees = degrees;
		boolean alignRight = false;
		if (between(degrees, 0, 45))
		{
			degrees = 0;
			alignRight = false;
		} else if (between(degrees, 45, 90))
		{
			degrees = 45;
			alignRight = false;
		} else if (between(degrees, 90, 135))
		{
			degrees = -45;
			alignRight = true;
		} else if (between(degrees, 135, 225))
		{
			degrees = 0;
			alignRight = true;
		} else if (between(degrees, 225, 270))
		{
			degrees = 45;
			alignRight = true;
		} else if (between(degrees, 270, 315))
		{
			degrees = -45;
			alignRight = false;
		} else if (between(degrees, 315, 360))
		{
			degrees = 0;
			alignRight = false;
		}
		setDegreesAndAlignment(n, degrees, alignRight);
	}

}
