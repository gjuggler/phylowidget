package org.andrewberman.ui;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;


import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PMatrix;

public class ProcessingUtils
{
	private static PMatrix camera = new PMatrix();
	private static PMatrix cameraInv = new PMatrix();
	private static PMatrix modelview = new PMatrix();
	private static PMatrix modelviewInv = new PMatrix();

	private static Point tPoint = new Point(0,0);
	
	static HashMap cache = new HashMap(50);
	
	public static void releaseCursor(PApplet p, Cursor c)
	{
		if (p.getCursor() != c)
			return;
		p.setCursor(Cursor.getDefaultCursor());
	}
	
	public static int colorToInt(PGraphics g, Color c)
	{
		return g.color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	}
	
	public static float getTextDescent(PGraphics g, PFont font, float size, boolean useNativeFonts)
	{
		if (g.getClass() == PGraphicsJava2D.class && useNativeFonts)
		{
			PGraphicsJava2D pgj = (PGraphicsJava2D) g;
			Graphics2D g2 = pgj.g2;
			Font f = font.font.deriveFont(size);
			FontMetrics fm = g2.getFontMetrics(f);
			return fm.getDescent();
		}
		return font.descent()*size;
	}
	
	public static float getTextAscent(PGraphics g, PFont font, float size, boolean useNativeFonts)
	{
		if (g.getClass() == PGraphicsJava2D.class && useNativeFonts)
		{
			PGraphicsJava2D pgj = (PGraphicsJava2D) g;
			Graphics2D g2 = pgj.g2;
			Font f = font.font.deriveFont(size);
			FontMetrics fm = g2.getFontMetrics(f);
			return fm.getAscent();
		}
		return font.ascent()*size;
	}
	
	public static float getTextWidth(PGraphics g, PFont font, float size, String text, boolean useNativeFonts)
	{
		if (useNativeFonts)
		{	
			PGraphicsJava2D pgj = (PGraphicsJava2D) g;
			Graphics2D g2 = pgj.g2;
			Font f = font.font.deriveFont(size);
			FontMetrics fm = g2.getFontMetrics(f);
			return fm.stringWidth(text);
		}
		char[] chars = text.toCharArray();
		float width = 0;
		for (int j = 0; j < chars.length; j++)
		{
			width += font.width(chars[j])*size;
		}
		return width;
	}
	
	public static void roundedRect(PGraphics g, float x, float y, float w, float h, float r)
	{
		int fill = g.fillColor;
		int strokeC = g.strokeColor;
		boolean stroke = g.stroke;
		float strokeW = g.strokeWeight;
		
		float cx = x + w/2;
		float cy = y + h/2;
		
//		p.rect(x,y,w,h);
		
		float dX = cx - (x + r);
		float dY = cy - (y + r);
		float dTheta = PConstants.QUARTER_PI;
		for (int i=0; i < 4; i++)
		{
			float theta = dTheta + (i*PConstants.HALF_PI);
			int sX = (PApplet.cos(theta) > 0 ? 1 : -1);
			int sY = (PApplet.sin(theta) > 0 ? 1 : -1);
			g.arc(cx+dX*sX,cy+dY*sY,2*r,2*r,theta-PConstants.QUARTER_PI,theta+PConstants.QUARTER_PI);
		}
		
		g.noStroke();
		float left = (float) Math.floor(x+r);
		float right = (float) Math.ceil(x+w-r);
		float top = (float) Math.floor(y+r);
		float bottom = (float) Math.floor(y+h-r);
		float width = right - left;
		float height = bottom - top;
		
		g.rect(x, top, w, height);
		g.rect(left,y,width,r);
		g.rect(left,bottom,width,r);
		
		g.stroke = stroke;
		g.strokeColor = strokeC;
		g.line(x+r, y, x+w-r, y); // top border.
		g.line(x+r, y+h, x+w-r, y+h);
		g.line(x, y+r, x, y+h-r);
		g.line(x+w, y+r, x+w, y+h-r);
		
		// upper-left.
//		p.arc(x+r, y+r, r, r, PConstants.PI, 3/2*PConstants.PI);
		
	}
	
	/**
	 * This should be called at the end of every draw() run.
	 * @param mat the modelview matrix.
	 */
	public static void setMatrix(PApplet p)
	{
		cache.clear();
		
		if (p.g.getClass() == PGraphicsJava2D.class)
		{
			PGraphicsJava2D g = (PGraphicsJava2D) p.g;
			AffineTransform tr = g.g2.getTransform();
			try
			{
				affineToPMatrix(tr, modelview);
				tr.invert();
				affineToPMatrix(tr, modelviewInv);
				camera.reset();
				cameraInv.reset();
			} catch (NoninvertibleTransformException e)
			{
				return;
			}
		} else
		{
			camera.set(p.g.camera);
			cameraInv.set(p.g.cameraInv);
			modelview.set(p.g.modelview);
			modelviewInv.set(p.g.modelviewInv);
			
		}
	}

	private static double[] temp = new double[6];
	public static void affineToPMatrix(AffineTransform tr, PMatrix mat)
	{
		tr.getMatrix(temp);
		mat.set((float) temp[0], (float) temp[2], 0, (float) temp[4],
				(float) temp[1], (float) temp[3], 0, (float) temp[5],
				0, 0, 0, 0,
				0, 0, 0, 0);
	}

	public static void transform(PMatrix mat, Point2D.Float pt)
	{
		float x = pt.x;
		float y = pt.y;
		float z = 0;
		
		pt.x = mat.m00*x + mat.m01*y + mat.m02*z + mat.m03;
		pt.y = mat.m10*x + mat.m11*y + mat.m12*z + mat.m13;
	}
	
	public static void screenToModel(Rectangle2D.Float rect)
	{
		tPoint.x = rect.x;
		tPoint.y = rect.y;
		transform(camera,tPoint);
		transform(modelviewInv,tPoint);
		float x = tPoint.x;
		float y = tPoint.y;
		
		tPoint.x = rect.x+rect.width;
		tPoint.y = rect.y+rect.height;
		transform(camera,tPoint);
		transform(modelviewInv,tPoint);
		
		rect.setFrameFromDiagonal(x, y, tPoint.x, tPoint.y);
	}
	
	/**
	 * 
	 * @param pt The point to transform in place. Should currently contain the mouse
	 * coordinates.
	 */

	public static void screenToModel(Point2D.Float pt)
	{
		transform(camera,pt);
		transform(modelviewInv,pt);
	}

	public static void modelToScreen(Point2D.Float pt)
	{
		transform(modelview,pt);
		transform(cameraInv,pt);
	}
}