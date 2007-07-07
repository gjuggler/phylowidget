package org.andrewberman.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;
import org.phylowidget.ui.FontLoader;
import org.phylowidget.ui.UIObject;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphicsJava2D;

public class Menu implements TweenListener, UIObject, MouseListener, MouseMotionListener
{
	private PApplet p;
	private PGraphicsJava2D pg;
	
// Model.
	public MenuSegment[] segments = new MenuSegment[0];
	public RadialMenuSegmentBackup[] shapes = new RadialMenuSegmentBackup[0];
	public ArrayList menuItems = new ArrayList(10);
	
// View.
	protected Graphics2D g2;
	protected float x;
	protected float y;
	private float cx; // x position of the center relative to the offscreen buffer.
	private float cy; // y position  "" .
	private float r; // radius.
	protected static final float INNER_MULTIPLIER = 0.35f;
	protected static Color baseColor;
	protected static Color[] stateColors = new Color[3];
	protected static Color strokeColor;
	protected int textBackgroundColor = 255;
	// font stuff...
	protected StringBuffer sb = new StringBuffer();
	protected PFont font;
	protected int fontSize;
	protected float descent;
	protected float ascent;
	// Transform stuff.
	AffineTransform transform = AffineTransform.getTranslateInstance(0, 0);
	// Fading stuff.
	protected int alpha = 0;
	protected Tween tween;
	
// Controller.
	public static final int UP = 0;
	public static final int OVER = 1;
	public static final int DOWN = 2;
	Point2D.Float pt = new Point2D.Float(0,0); // Contains the current mouse coordinates.
	protected Ellipse2D.Float outer = new Ellipse2D.Float(0,0,0,0);
	protected Ellipse2D.Float inner = new Ellipse2D.Float(0,0,0,0);
	protected Ellipse2D.Float centerHi = new Ellipse2D.Float(0,0,0,0);
	protected Ellipse2D.Float centerLo = new Ellipse2D.Float(0,0,0,0);
	protected boolean hidden = true;
	protected boolean approaching = false;
	
// Statics & Constants.
	private static final float PI = PApplet.PI;
	private static final float TWO_PI = PApplet.TWO_PI;
	private static final float HALF_PI = PApplet.HALF_PI;
	
	protected boolean in;
	protected boolean out;
	protected boolean ctrHi;
	protected boolean ctrLo;
	
	public Menu(PApplet p)
	{
		this(p,20,20,50);
	}
	
	public Menu(PApplet p, float x, float y, float r)
	{
		this.p = p;
		pg = (PGraphicsJava2D) p.createGraphics((int)r*2,(int)r*2, PApplet.JAVA2D);
		resizeBuffer((int)r*2,(int)r*2);

		this.font = FontLoader.f64;
		this.fontSize = (int)Math.max(r / 2,10);
		ascent = font.ascent() * fontSize;
		descent = font.descent() * fontSize;
	
		this.setAll(x, y, r);
		
		tween = new Tween(this, new TweenQuad(), Tween.INOUT, 0, 255, 30);
		tween.stop();
	}

	public void addListeners()
	{
		p.addMouseListener(this);
		p.addMouseMotionListener(this);		
	}
	
	public static final Color lightenColor(Color c, int lighten)
	{
		int red = constrain(c.getRed() + lighten,0,255);
		int green = constrain(c.getGreen() + lighten,0,255);
		int blue = constrain(c.getBlue() + lighten,0,255);
		int alpha = c.getAlpha();
		return new Color(red,green,blue,alpha);
	}
	
	public static final int constrain(int val, int lo, int hi)
	{
		if (val < lo) val = lo;
		else if (val > hi) val = hi;
		return val;
	}
	
	public void draw()
	{
		tween.update();
		setColors();
		
		drawApproachingCircle();
		pg.beginDraw();
		pg.background(255,0);
		for(int i=0; i < shapes.length; i++)
		{
			shapes[i].tween.update();
			drawText(shapes[i]);
			drawShape(shapes[i]);
			drawHintText(shapes[i]);
		}
		pg.endDraw();
		pg.modified = true;
		if (tween.position > 0)
		{
			p.image(pg, x-pg.width/2, y-pg.height/2);
		}
	}

	// (re)sets the colors, with correct alpha values.
	public void setColors()
	{
		alpha = constrain(alpha,0,255);
		baseColor = new Color(200,200,200,alpha);
		stateColors[0] = baseColor;
		stateColors[1] = lightenColor(baseColor,20);
		stateColors[2] = lightenColor(baseColor,-20);
		strokeColor = new Color(0,0,0,alpha);
	}
	
	protected static Stroke myStroke = new BasicStroke(2);
	protected void drawShape(RadialMenuSegmentBackup seg)
	{
		g2.setPaint(stateColors[seg.state]);
		g2.fill(seg.bufferArea);
		g2.setStroke(myStroke);
		g2.setPaint(strokeColor);
		g2.draw(seg.bufferArea);
	}
	
	protected void drawApproachingCircle()
	{
		if (approaching)
		{
			p.stroke(0,255,0);
			p.strokeWeight(3);
			p.noFill();
			p.ellipse(x, y, inner.width, inner.height);
		}
	}
	
	private static float degToRad(float degrees)
	{
		degrees = degrees % 360;
		float theta = degrees / 360 * TWO_PI;
		return theta;
	}
	
	private void drawText(RadialMenuSegmentBackup seg)
	{
		float newAlpha = (float)(alpha * seg.alpha) / (float)(255.0);
		// Semi-transparent background behind the text.
		pg.noStroke();
		pg.fill(textBackgroundColor,(int)((newAlpha-1)*.95));
		pg.rectMode(PConstants.CORNER);
		
		float x = cx+seg.textX-r/4;
		float y = cy+seg.textY-ascent-descent - (r/4);
		float w = seg.textWidth+(r/4)*2;
		float h = descent*2+ascent + (r/4)*2;
		pg.noSmooth();
		ProcessingUtils.roundedRect(pg,x,y,w,h,w/5);
		pg.smooth();
//		pg.rect(cx+seg.textX-r/4, cy+seg.textY - ascent - descent - (r/4), seg.textWidth+(r/4)*2, descent*2+ascent + (r/4)*2);
		
		// Now, draw the text.
		pg.fill(0,newAlpha);
		pg.textAlign(PConstants.LEFT);
		pg.textFont(font);
		pg.textSize(fontSize);
		pg.text(seg.name,cx+seg.textX,cy+seg.textY);
	}
	
	public void drawHintText(RadialMenuSegmentBackup seg)
	{
		float myFontSize = fontSize*1.5f;
		pg.textFont(font);
		pg.textSize(myFontSize);
		
		pg.fill(0,alpha);
		pg.textAlign(PConstants.LEFT);
		pg.text(seg.shortcut,cx + seg.hintX,cy + seg.hintY);
		
		pg.textSize(fontSize);
	}
	
	private void createShapes()
	{
		shapes = new RadialMenuSegmentBackup[menuItems.size()];
		float dtheta = (float)360 / menuItems.size();
		System.out.println(dtheta);
		float theta = -90;
		
		Ellipse2D.Float circle = new Ellipse2D.Float(-INNER_MULTIPLIER,-INNER_MULTIPLIER,INNER_MULTIPLIER*2,INNER_MULTIPLIER*2);	
		Area del = new Area(circle);

		Arc2D.Float arc;
		Area wedge;
		for(int i=0; i < shapes.length; i++)
		{
			arc = new Arc2D.Float(Arc2D.PIE);
			arc.setFrame(-1, -1, 2, 2);
			arc.setAngleStart(-theta);
			arc.setAngleExtent(-dtheta);
			wedge = new Area(arc);
			wedge.subtract(del);
			
			RadialMenuSegmentBackup seg = (RadialMenuSegmentBackup) menuItems.get(i);
			seg.area = wedge;
			seg.thetaLo = (float)theta;
			seg.thetaHi = (float)theta + dtheta;
			seg.thetaMid = theta + dtheta/2;
//			System.out.println(seg.thetaLo + " " +seg.thetaMid + " " + seg.thetaHi);
			
			shapes[i] = seg;
			theta += dtheta;
			
			layoutShapeText(seg);
		}
		updateTransformedAreas();
	}
	
	public void layoutShapeText(RadialMenuSegmentBackup seg)
	{
		/*
		 * Calculate the sine and cosine, which we'll need to use often.
		 */
		float theta = degToRad(seg.thetaMid);
		float cos = (float) Math.cos(theta);
		float sin = (float) Math.sin(theta);

		/*
		 * First, let's handle the text labels.
		 */
		float outerX = cos*(r+8);
		float outerY = sin*(r+8);
		
		// Simple text alignment isn't enough, so let's "hug" the circle ourselves.
		sb.replace(0, sb.length(), seg.name);
		float textWidth = 0;
		// Get the width of our text.
		for (int i=0; i < sb.length(); i++)
		{
			textWidth += font.width(sb.charAt(i)) * fontSize;
		}
		// Calculate the necessary x and y offsets for the text.
		seg.textX = Math.signum(cos) * textWidth/2;
		if (Math.abs(cos) < 0.25) seg.textX = 0;
		seg.textY = sin * (ascent + descent + 5)/2;
		seg.textX += -textWidth / 2;
		seg.textY += -descent + (ascent + descent)/2;
		seg.textX += outerX;
		seg.textY += outerY;
		seg.textWidth = textWidth;
		/*
		 *  Test if the text will fall outside the boundary of the buffered area. If so,
		 *  resize the buffer to make it fit.
		 */
		if (cx + seg.textX - 2 < 0 ||
			cx + seg.textX + textWidth + 2 > pg.width)
		{
			resizeBuffer(pg.width*2,pg.height);
		}
		if( cy + seg.textY - ascent - descent < 0 ||
			cy + seg.textY - ascent - descent + descent*2 > pg.height)
		{
			resizeBuffer(pg.width,pg.height*2);
		}
		
		/*
		 * Now, let's handle the hint characters.
		 */
		float centerRad = (r + r*INNER_MULTIPLIER)/2;
		float centerX = cos * centerRad;
		float centerY = sin * centerRad;
		
		float myFontSize = (int) (fontSize*1.5);
		
		int i = font.index(seg.shortcut);
		float cDesc = font.height[i] - font.topExtent[i];
		float cAsc = font.topExtent[i];
		float cHeight = font.height[i];
		float cWidth = font.setWidth[i];
		
		cDesc = cDesc / (float)font.size * (float)myFontSize;
		cAsc = cAsc / (float)font.size * (float)myFontSize;
		cWidth = cWidth / (float)font.size * (float)myFontSize;
		cHeight = cHeight / (float)font.size * (float)myFontSize;
		
		seg.hintX = centerX - cWidth / 2.0f;
		seg.hintY = centerY - cDesc + cHeight / 2.0f;
	}
	
	public void resizeBuffer(int width, int height)
	{
		pg.resize(width, height);
		cx = (float)pg.width / 2.0f;
		cy = (float)pg.height / 2.0f;
		g2 = pg.g2;
		pg.hint(PConstants.ENABLE_NATIVE_FONTS); // Native fonts are nice!
		pg.smooth();
	}

	public void updateTransformedAreas()
	{
		AffineTransform tr = AffineTransform.getTranslateInstance(x,y);
		AffineTransform sc = AffineTransform.getTranslateInstance(cx,cy);
		tr.scale(r,r);
		sc.scale(r,r);
		for (int i=0; i < shapes.length; i++)
		{
			shapes[i].modelArea = shapes[i].area.createTransformedArea(tr);
			shapes[i].bufferArea = shapes[i].area.createTransformedArea(sc);
		}
		inner.setFrameFromCenter(x, y, x - r * INNER_MULTIPLIER/2, y - r * INNER_MULTIPLIER/2);
		outer.setFrameFromCenter(x,y, x- 2 * r, y + 2 * r);
		centerHi.setFrameFromCenter(x,y,x-r,y-r);
		centerLo.setFrameFromCenter(x,y,x-(r*INNER_MULTIPLIER),y-(r*INNER_MULTIPLIER));
	}
	
	public void addMenuItem(String label, char shortcut, Object functionTarget, String function)
	{
		RadialMenuSegmentBackup seg = new RadialMenuSegmentBackup(label,shortcut,functionTarget,function);
		menuItems.add(seg);
		createShapes();
	}
	
	public void setPosition(float x, float y)
	{
		setAll(x,y,this.r);
	}
	
	public void setAll(float x, float y, float r)
	{
		this.x = x;
		this.y = y;
		this.r = r;
		updateTransformedAreas();
	}

	public void show()
	{
		if (!hidden)return;
		tween.continueTo(255, 10);
		hidden = false;
		System.out.println("SHOW!!!");
	}
	
	public void hide()
	{
		if (hidden)return;
		tween.continueTo(0, 10);
		hidden = true;
		System.out.println("HIDE!");
	}
	
	public boolean isHidden()
	{
		return hidden;
	}
	
	public synchronized void mouseEvent(MouseEvent e)
	{
		pt.setLocation(e.getX(),e.getY());
		ProcessingUtils.screenToModel(pt);
		if (e.getID() == MouseEvent.MOUSE_PRESSED)
			System.out.println(pt);
		in = inner.contains(pt);
		out = outer.contains(pt);
		ctrHi = centerHi.contains(pt);
		ctrLo = centerLo.contains(pt);
		if (hidden)
			hiddenMouseEvent(e);
		else
			visibleMouseEvent(e);
		
		if (!hidden)
			segmentMouseEvent(e);
	}
	
	public void hiddenMouseEvent(MouseEvent e)
	{
		if (hidden)
		{
			if (in) // if we're inside the innermost "trigger" boundary.
			{
				if (hidden)
				{
					show();
					approaching = false;
				}
			}
			
			if (out) // if we're within the outermost boundary.
			{
				if (hidden)
				{
					approaching = true;
				}
					
			} else // if we're outside the outermost boundary.
			{
				approaching = false;
			}			
		}
	}	
	public void visibleMouseEvent(MouseEvent e)
	{
			if (ctrHi && !ctrLo)
			{
				p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else
			{
				p.setCursor(Cursor.getDefaultCursor());
			}
			if (!out)
			{
				hide();
			} else if (!ctrHi) // if we're outside the visible boundary.
			{
				// Fade out as we move further away.
				float diff = (float)outer.getWidth()/2 - (float)pt.distance(x,y);
				float ratio = diff / ((float)outer.getWidth()/2 - (float)centerHi.getWidth()/2);
				int intDst = (int)(ratio * 255);
				tween.continueTo(intDst, 30);
				tween.fforward();
			} else
			{
				// We're inside the boundary. Make it full alpha.
				tween.continueTo(255, 30);
				tween.fforward();
			}
			if (!ctrHi || (ctrLo && !in))
			{
				if (e.getID() == MouseEvent.MOUSE_PRESSED)
				{
					System.out.println("Helloooo doggy");
					hide();
				}
			}
			if (e.getID() == MouseEvent.MOUSE_PRESSED)
			{
				System.out.println(outer);
				System.out.println("CtrHi:"+ctrHi);
				System.out.println("CtrLo:"+ctrLo);
				System.out.println("outer:"+out);
				System.out.println("in:"+in);
			}
		}
	
	public void segmentMouseEvent(MouseEvent e)
	{
	
		if (hidden) return;
		
		// The following loop is ONLY for switching individual segment
		// states. For general mouse goodness, use the logic ABOVE this point.
		for (int i=0; i < shapes.length; i++)
		{
			RadialMenuSegmentBackup seg = shapes[i];
			boolean containsPoint = shapes[i].modelArea.contains(pt.x,pt.y);
			switch (e.getID())
			{
				case MouseEvent.MOUSE_MOVED:
					if (containsPoint)
					{
						setState(seg,OVER);
						// This stuff makes the non-selected segments fade out...
						// it looks kinda silly, so I'm commenting it out.
//						seg.show();
//						for (int j=0; j < shapes.length; j++)
//						{
//							if (shapes[j] != seg)
//								shapes[j].fadeOut();
//						}
					} else
					{
						setState(seg,UP);
					}
					break;
				case MouseEvent.MOUSE_PRESSED:
					if (containsPoint) seg.clickedInside = true;
					else seg.clickedInside = false;
				case MouseEvent.MOUSE_DRAGGED:
					if (seg.clickedInside && containsPoint) setState(seg,DOWN);
					else if (containsPoint) setState(seg,OVER);
					else setState(seg,UP);
					break;
				case MouseEvent.MOUSE_RELEASED:
					if (containsPoint)
					{
						seg.performAction();
						setState(seg,OVER);
					} else setState(seg,UP);
				default:
					break;
			}
		}
	}

	public void setState(RadialMenuSegmentBackup seg, int state)
	{
		seg.state = state;
	}

	public void tweenEvent(Tween source, int eventType)
	{
		alpha = Math.round(source.position);
	}

	public void mouseClicked(MouseEvent e){mouseEvent(e);}

	public void mouseEntered(MouseEvent e){mouseEvent(e);}
	public void mouseExited(MouseEvent e){mouseEvent(e);}
	public void mousePressed(MouseEvent e){mouseEvent(e);}
	public void mouseReleased(MouseEvent e){mouseEvent(e);}
	public void mouseDragged(MouseEvent e){mouseEvent(e);}
	public void mouseMoved(MouseEvent e){mouseEvent(e);}

	public void keyEvent(KeyEvent e){
		char c = e.getKeyChar();
		for (int i=0; i < menuItems.size(); i++)
		{
			RadialMenuSegmentBackup seg = (RadialMenuSegmentBackup)menuItems.get(i);
			if (seg.shortcut == c)
			{
				seg.performAction();
			}
		}
	}
}
