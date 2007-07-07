package org.andrewberman.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;
import org.phylowidget.render.Point;
import org.phylowidget.ui.FontLoader;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class RadialPopupMenu extends PopupMenu implements TweenListener
{
	protected PFont font;
	
	public float x,y=0;
	public float thetaLo = 0;
	public float thetaHi = PConstants.TWO_PI;
	public float radLo = 20;
	public float radHi = 50;

	protected Ellipse2D.Float inner = new Ellipse2D.Float(0,0,0,0);
	protected Ellipse2D.Float outer = new Ellipse2D.Float(0,0,0,0);
	protected Ellipse2D.Float max = new Ellipse2D.Float(0,0,0,0);
	protected Point pt = new Point(0,0);
	
	protected Color baseColor;
	protected Color[] stateColors = new Color[4];
	protected Color strokeColor;
	
	public RadialPopupMenu()
	{
		font = p.loadFont("TimesNewRoman-64.vlw");
	}
	
	public void layoutSegments()
	{
		if (segments.size() == 0) return;
		
		float dTheta = thetaHi - thetaLo;
		float thetaStep = dTheta / segments.size();
		float start = - PConstants.HALF_PI;
		for (int i=0; i < segments.size(); i++)
		{
			RadialMenuSegment seg = (RadialMenuSegment)segments.get(i);
			float curTheta = start + i*thetaStep;
			seg.layout(radLo/radHi, 1, curTheta, curTheta+thetaStep);
		}
	}

	public void preDraw()
	{
		// TODO: Resize the buffer accordingly.
		
		buffTransform = AffineTransform.getTranslateInstance(0,0);
		mouseTransform = AffineTransform.getTranslateInstance(x, y);
		buffTransform.scale(radHi,radHi);
		mouseTransform.scale(radHi,radHi);
		
		inner.setFrameFromCenter(x, y, x - radLo*2, y - radLo*2);
		outer.setFrameFromCenter(x,y, x- 2*radHi, y + 2*radHi);
		max.setFrameFromCenter(x,y, x - 4*radHi, y + 4*radHi);
		
		setColors();
	}

	public void bufferToCanvas()
	{
		p.image(pg, x-pg.width/2, y-pg.height/2);
	}

	public void addMenuItem(String label, char hint, Object o, String function)
	{
		RadialMenuSegment seg = new RadialMenuSegment(label,hint,o,function);
		segments.add(seg);
		layoutSegments();
	}
	
	public void setColors()
	{
		alpha = constrain(alpha,0,255);
		baseColor = new Color(230,230,240,alpha);
		stateColors[0] = baseColor;
		stateColors[1] = lightenColor(baseColor,15);
		stateColors[2] = lightenColor(baseColor,-25);
		strokeColor = new Color(0,0,0,alpha);
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
	
	public void mouseEvent(MouseEvent e)
	{
		if (hidden) return;
		preDraw();
		pt.setLocation(e.getX(),e.getY());
		ProcessingUtils.screenToModel(pt);
//		if (e.getID() == MouseEvent.MOUSE_PRESSED)
//			System.out.println(pt);
		boolean in = inner.contains(pt.x,pt.y);
		boolean out = outer.contains(pt.x,pt.y);
		boolean inMax = max.contains(pt.x,pt.y);
		
		if (!inMax)
		{
			hide();
		} else if (!out) // if we're outside the visible boundary.
		{
//			System.out.println("Not in Out");
			// Fade out as we move further away.
			float diff = (float)max.getWidth()/2 - (float)pt.distance(x,y);
			float ratio = diff / ((float)max.getWidth()/2 - (float)outer.getWidth()/2);
			int intDst = (int)Math.min((ratio * 255),255);
			aTween.continueTo(intDst, 30);
			aTween.fforward();
			
			if (e.getID() == MouseEvent.MOUSE_PRESSED)
			{
				hide();
			}
		}
		super.mouseEvent(e);
	}
	
	public static StringBuffer sb = new StringBuffer();
	public static Ellipse2D.Float tempCircle = new Ellipse2D.Float(0,0,0,0);
	public static Arc2D.Float tempArc = new Arc2D.Float(Arc2D.PIE);
	public static Stroke myStroke = new BasicStroke(2);
	
	public AffineTransform buffTransform = AffineTransform.getTranslateInstance(0,0);
	public AffineTransform mouseTransform = AffineTransform.getTranslateInstance(0,0);
	
	final class RadialMenuSegment extends MenuSegment
	{
		float rLo,rHi,tLo,tHi = 0;
		
		float rectX,rectY,rectW,rectH = 0;
		float textX,textY = 0;
		float textWidth, textHeight, pad = 0;
		float hintX,hintY = 0;
		float fontSize,hintSize = 0;
		Area wedge,bufferWedge,mouseWedge;
		
		String label;
		char hint;
		
		public RadialMenuSegment(String label, char hint, Object object, String function)
		{
			super(object, function);
			this.label = label;
			this.hint = hint;
		}

		public void drawUnder()
		{
			float r = radHi;
			
			pg.fill(255,255,255,230f*(alpha/255f));
			pg.stroke(0,alpha);
			pg.strokeWeight(1.0f);
			pg.noSmooth();
			ProcessingUtils.roundedRect(pg,rectX*r,rectY*r,rectW*r,rectH*r,rectW*r/10);
			pg.smooth();
			
			super.drawUnder();
		}
		
		public void draw()
		{			
			float r = radHi;
			
			pg.fill(0,alpha);
			pg.textFont(font);
			pg.textSize(fontSize*r);
			pg.text(label,textX*r,textY*r);
		
			drawShape();
			
			pg.fill(0,alpha);
			pg.textSize(hintSize*r);
			pg.text(hint,hintX*r,hintY*r);
			
			super.draw();
		}
		
		public void drawShape()
		{
			bufferWedge = wedge.createTransformedArea(buffTransform);
			mouseWedge = wedge.createTransformedArea(mouseTransform);
			g2.setPaint(stateColors[state]);
			g2.fill(bufferWedge);
			g2.setStroke(myStroke);
			g2.setPaint(strokeColor);
			g2.draw(bufferWedge);
		}
		
		public void layout(float radLo, float radHi, float thLo, float thHi)
		{
			this.rLo=radLo;
			this.rHi=radHi;
			this.tLo=thLo;
			this.tHi=thHi;
			
			this.layoutText();
			this.createShapes();
			
			float dTheta = thHi - thLo;
			float thetaStep = dTheta / subSegments.size();
			for (int i=0; i < subSegments.size(); i++)
			{
				RadialMenuSegment seg = (RadialMenuSegment) subSegments.get(i);
				float theta = thLo + i*thetaStep;
				seg.layout(radHi,radHi+(radHi-radLo),theta,theta+thetaStep);
			}
			
			// We had to do our own recursion because of the new arguments
			// in the layout function as compared to MenuSegment's layou().
//			super.layout();
		}
		
		public float radToDeg(float rad)
		{
			return PApplet.degrees(rad);
		}
		
		public void createShapes()
		{
			tempCircle.setFrameFromCenter(0,0,rLo,-rLo);
			tempArc.setFrame(-rHi,-rHi,2*rHi,2*rHi);
			
			float degLo = radToDeg(-tLo);
			float degHi = radToDeg(-tHi);
			
			tempArc.setAngleStart(degLo);
			tempArc.setAngleExtent(degHi-degLo);
			wedge = new Area(tempArc);
			Area delete = new Area(tempCircle);
			wedge.subtract(delete);
		}
		
		public void layoutText()
		{
			/*
			 * Calculate the sine and cosine, which we'll need to use often.
			 */
			float theta = (tLo + tHi) / 2;
//			System.out.println(theta);
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);
			float outerX = cos*(rHi*1.5f);
			float outerY = sin*(rHi*1.5f);
			
			float unitTextHeight = font.ascent()+font.descent();
			fontSize = (rHi-rLo)/unitTextHeight * .9f;
			float descent = font.descent()*fontSize;
			float ascent = font.ascent()*fontSize;
			textHeight = fontSize*unitTextHeight;
			sb.replace(0, sb.length(), label);
			textWidth = 0;
			for (int i=0; i < sb.length(); i++)
			{
				textWidth += font.width(sb.charAt(i)) * fontSize;
			}
			// Calculate the necessary x and y offsets for the text.
			textX = Math.signum(cos) * textWidth/2;
			if (Math.abs(cos) < 0.25) textX = 0;
			textX += -textWidth / 2;
			textX += outerX;
			textY = sin * (textHeight)/2;
//			float textY = sin * (ascent + descent + 5)/2;
			textY += -descent + (ascent + descent)/2;
			textY += outerY;
			
			/*
			 * Set the background rectangle.
			 */
			float pad = rHi/5;
			rectX = textX-pad;
			rectY = textY-ascent-descent/2-pad;
			rectW = textWidth+2*pad;
			rectH = textHeight+2*pad;
			
			/*
			 * Now, let's handle the hint characters.
			 */
			float rMid = (rLo + rHi) / 2;
			float centerX = cos * rMid;
			float centerY = sin * rMid;
			
			hintSize = fontSize * 1.3f;
			float naturalSize = font.size;
			float multiplier = hintSize / naturalSize;
			int i = font.index(hint);
			float charHeight = font.height[i] * multiplier;
			float charWidth = font.width[i] * multiplier;
			float charDesc = (font.height[i] - font.topExtent[i])*multiplier;
			
			hintX = centerX - charWidth / 2.0f;
			hintY = centerY - charDesc + charHeight / 2.0f;		
		}
		
		public boolean containsPoint(Point p)
		{
			if (mouseWedge == null) return false;
			return mouseWedge.contains(segPt.x,segPt.y);
		}
		
		public void mouseEvent(MouseEvent e)
		{
			segPt.setLocation(e.getX(),e.getY());
			ProcessingUtils.screenToModel(segPt);
			super.mouseEvent(e);
			if (containsPoint(segPt))
			{
				changeCursor = true;
				withinButtons = true;
			}
		}
		
		public void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
		{
			if (bufferWedge == null) return;
			float r = radHi;
//			Rectangle2D.union(rect, bufferWedge.getBounds2D(), rect);
			buff.x = pg.width/2;
			buff.y = pg.height/2;
			buff.x += textX*r;
			buff.y += textY*r - textHeight*r;
			buff.width = textWidth*r;
			buff.height = textHeight*r;
			Rectangle2D.union(rect, buff, rect);
		}
		
	}
}
