package org.andrewberman.ui.menu;

import processing.core.PApplet;


public class RadialMenu extends Menu
{
//	protected PFont font;
//	
//	public float thetaLo = 0;
//	public float thetaHi = PConstants.TWO_PI;
//	public float innerRadius = 20;
//	public float radius = 50;
//
//	protected Ellipse2D.Float inner = new Ellipse2D.Float(0,0,0,0);
//	protected Ellipse2D.Float outer = new Ellipse2D.Float(0,0,0,0);
//	protected Ellipse2D.Float max = new Ellipse2D.Float(0,0,0,0);
//	protected Point pt = new Point(0,0);
//	
//
//	public RadialMenu()
//	{
//		super();
//		font = p.loadFont("TimesNewRoman-64.vlw");
//	}
//	
//	public void setLocation(float x, float y)
//	{
//		this.x = x;
//		this.y = y;
//	}
//	
//	public void setRadius(float r)
//	{
//		this.radius = r;
//	}
//	
//	public void setRadii(float inner, float outer)
//	{
//		this.innerRadius = inner;
//		this.radius = outer;
//		layout();
//	}
	
	public RadialMenu(PApplet app)
	{
		super(app);
		// TODO Auto-generated constructor stub
	}

	public MenuItem create(String s)
	{
		return null;
	}
	
//	public void setArc(float thetaLo, float thetaHi)
//	{
//		this.thetaLo = thetaLo;
//		this.thetaHi = thetaHi;
//		layout();
//	}
//	
//	public void layout()
//	{
//		if (items.size() == 0) return;
//		
//		float dTheta = thetaHi - thetaLo;
//		float thetaStep = dTheta / items.size();
//		float start = thetaLo; // -PConstants.HALF_PI;
//		for (int i=0; i < items.size(); i++)
//		{
//			RadialMenuItem seg = (RadialMenuItem)items.get(i);
//			float curTheta = start + i*thetaStep;
//			seg.layout(innerRadius/radius, 1, curTheta, curTheta+thetaStep);
//		}
//	}
//
//	public void preDraw()
//	{	
//		buffTransform = AffineTransform.getTranslateInstance(0,0);
//		mouseTransform = AffineTransform.getTranslateInstance(x, y);
//		buffTransform.scale(radius,radius);
//		mouseTransform.scale(radius,radius);
//		
//		inner.setFrameFromCenter(x, y, x - innerRadius*2, y - innerRadius*2);
//		outer.setFrameFromCenter(x,y, x- 2*radius, y + 2*radius);
//		max.setFrameFromCenter(x,y, x - 4*radius, y + 4*radius);
//	}
//
//	public void bufferToCanvas()
//	{
//		p.image(pg,x-pg.width/2,y-pg.height/2,pg.width,pg.height);
//	}
//
//	
////	public MenuItem createMenuItem(String label, char hint, Object o, String function)
////	{
//////		return new RadialMenuItem(label,hint,o,function);
////	}
//	
//	public void mouseEvent(MouseEvent e)
//	{
//		if (hidden) return;
//		preDraw();
//		pt.setLocation(e.getX(),e.getY());
//		ProcessingUtils.screenToModel(pt);
////		if (e.getID() == MouseEvent.MOUSE_PRESSED)
////			System.out.println(pt);
//		boolean in = inner.contains(pt.x,pt.y);
//		boolean out = outer.contains(pt.x,pt.y);
//		boolean inMax = max.contains(pt.x,pt.y);
//		
//		if (!inMax)
//		{
//			hide();
//		} else if (!out) // if we're outside the visible boundary.
//		{
////			System.out.println("Not in Out");
//			// Fade out as we move further away.
//			float diff = (float)max.getWidth()/2 - (float)pt.distance(x,y);
//			float ratio = diff / ((float)max.getWidth()/2 - (float)outer.getWidth()/2);
//			int intDst = (int)Math.min((ratio * 255),255);
//			aTween.continueTo(intDst, 30);
//			aTween.fforward();
//			
//			if (e.getID() == MouseEvent.MOUSE_PRESSED)
//			{
//				hide();
//			}
//		}
//		super.mouseEvent(e);
//	}
}
