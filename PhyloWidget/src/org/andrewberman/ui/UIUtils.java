/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphics2D;
import processing.core.PGraphicsJava2D;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PMatrix3D;

/**
 * The <code>UIUtils</code> class is a disgusting mess of static utility
 * functions that were placed in a single place so as to reduce the amount of
 * duplicated code within the individual UI objects' classes.
 * <p>
 * Scroll to each method's documentation for further details.
 * 
 * @author Greg
 */
public final class UIUtils
{
	static Cursor baseCursor = Cursor.getDefaultCursor();
	private static PMatrix3D camera = new PMatrix3D();
	private static PMatrix3D cameraInv = new PMatrix3D();
	static Object cursorOwner;

	private static PMatrix3D modelview = new PMatrix3D();

	private static PMatrix3D modelviewInv = new PMatrix3D();
	private static double[] temp = new double[6];

	private static Point tPoint = new Point(0, 0);

	/**
	 * Copies the transformation data from an <code>AffineTransformation</code>
	 * into a <code>PMatrix3D</code>.
	 * 
	 * @param tr
	 *            the source <code>AffineTransformation</code>
	 * @param mat
	 *            the destination <code>PMatrix3D</code>
	 */
	public static void affineToPMatrix3D(AffineTransform tr, PMatrix3D mat)
	{
		tr.getMatrix(temp);
		mat.set((float) temp[0], (float) temp[2], 0, (float) temp[4], (float) temp[1], (float) temp[3], 0,
			(float) temp[5], 0, 0, 0, 0, 0, 0, 0, 0);
	}

	/**
	 * Turns a <code>Color</code> object into an int value, using the current
	 * <code>PGraphics</code> object's <code>color</code> method. Yeah, I'm just
	 * lazy enough not to do it myself in the <code>Color</code> class.
	 * 
	 * @param g
	 *            the current <code>PGraphics</code>
	 * @param c
	 *            the <code>Color</code> object to convert
	 * @return the integer representation of the given <code>Color</code>
	 *         object.
	 */
	public static int colorToInt(PGraphics g, Color c)
	{
		return g.color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	public static Cursor createBlankCursor(PApplet p)
	{
		Image image = p.createImage(new MemoryImageSource(0, 0, new int[0], 0, 0));
		//		Image image = UIUtils.PImageToImage(p,img);
		return Toolkit.getDefaultToolkit().createCustomCursor(image, new java.awt.Point(0, 0), "asdf");
	}

	public static Cursor createCursor(PApplet p, String filename, int offsetX, int offsetY)
	{
		InputStream in = p.createInput(filename);
		BufferedImage img = null;
		try
		{
			img = ImageIO.read(in);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(img.getWidth(), img.getHeight());
		BufferedImage img2 = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img2.createGraphics();
		g2.drawImage(img, 0, 0, null);
		g2.dispose();

		//		Image image = resized.getImage();
		return Toolkit.getDefaultToolkit().createCustomCursor(img2, new java.awt.Point(offsetX, offsetY), "asdf");
	}

	public static Object getCursorOwner()
	{
		return cursorOwner;
	}

	/**
	 * Retrieves the "meta" mask for the current system.
	 * <p>
	 * In Windows, the meta mask should be equivalent to
	 * <code>CTRL_DOWN_MASK</code>.
	 * <p>
	 * In Mac OSX, the meta mask usually ends up as <code>META_DOWN_MASK</code>.
	 * <p>
	 * Either way, if a UI object wishes to implement standard shortcuts like
	 * <code>Ctrl-X</code>, then it should make sure to turn all references to
	 * "control" into the current system's "meta" mask. See the
	 * <code>Shortcut</code> class for an attempt at achieving this
	 * platform-compatibility.
	 * 
	 * @return
	 * @see org.andrewberman.ui.Shortcut
	 */
	public static int getMetaMask()
	{
		return KeyEvent.CTRL_DOWN_MASK;
		//		int shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		//		if (shortcutMask == KeyEvent.CTRL_MASK)
		//			shortcutMask = KeyEvent.CTRL_DOWN_MASK;
		//		else if (shortcutMask == KeyEvent.ALT_MASK)
		//			shortcutMask = KeyEvent.ALT_DOWN_MASK;
		//		else if (shortcutMask == KeyEvent.META_MASK)
		//			shortcutMask = KeyEvent.META_DOWN_MASK;
		//		return shortcutMask;
	}

	/**
	 * Returns the <code>FontMetrics</code> object corresponding to the given
	 * <code>PGraphicsJava2D</code> object.
	 * 
	 * @param pg
	 *            a <code>PGraphicsJava2D</code> instance
	 * @param font
	 *            a <code>java.awt.Font</code> instance
	 * @param size
	 *            the desired font size
	 * @return the corresponding <code>FontMetrics</code> object.
	 */
	public static FontMetrics getMetrics(PGraphics pg, Font font, float size)
	{
		//		Graphics2D g2 = (Graphics2D)UIGlobals.g.getP().getGraphics();
		if (pg == null)
			return null;
		if (pg instanceof PGraphicsJava2D)
		{
			Graphics2D g2 = ((PGraphicsJava2D) pg).g2;
			if (g2 == null)
				return null;
			if (font == null)
				return null;
			Font f = font.deriveFont(size);
			FontMetrics fm = g2.getFontMetrics(f);
			return fm;
		} else {
			return pg.parent.getGraphics().getFontMetrics();
		}
	}

	/**
	 * Retrieves the floating-point text ascent for the given font, size, and
	 * PGraphics context.
	 * 
	 * @param g
	 *            a PGraphics object
	 * @param font
	 *            a <code>PFont</code> object
	 * @param size
	 *            the font size
	 * @param useNativeFonts
	 *            true if the caller wishes to use Java's built-in font
	 *            functionality
	 * @return the font ascent
	 */
	public static float getTextAscent(PGraphics g, PFont font, float size, boolean useNativeFonts)
	{
		if (isJava2D(g) && useNativeFonts)
		{
			FontMetrics fm = getMetrics(g, font.getFont(), size);
			if (fm != null)
				return fm.getAscent();
		}
		return font.ascent() * size;
	}

	/**
	 * Retrieves the floating-point text descent for the given font, size, and
	 * PGraphics context.
	 * 
	 * @param g
	 *            a PGraphics object
	 * @param font
	 *            a <code>PFont</code> object
	 * @param size
	 *            the font size
	 * @param useNativeFonts
	 *            true if the caller wishes to use Java's built-in font
	 *            functionality
	 * @return the font descent
	 */
	public static float getTextDescent(PGraphics g, PFont font, float size, boolean useNativeFonts)
	{
		if (isJava2D(g) && useNativeFonts)
		{
			FontMetrics fm = getMetrics(g, font.getFont(), size);
			if (fm != null)
				return fm.getDescent();
		}
		return font.descent() * size;
	}

	/**
	 * Retrieves the floating-point text height for the given font, size, and
	 * PGraphics context.
	 * 
	 * @param g
	 *            a PGraphics object
	 * @param font
	 *            a <code>PFont</code> object
	 * @param size
	 *            the font size
	 * @param useNativeFonts
	 *            true if the caller wishes to use Java's built-in font
	 *            functionality
	 * @return the font height
	 */
	public static float getTextHeight(PGraphics g, PFont font, float size, String text, boolean useNativeFonts)
	{
		if (isJava2D(g) && useNativeFonts)
		{
			FontMetrics fm = getMetrics(g, font.getFont(), size);
			if (fm != null)
				return fm.getAscent() + fm.getDescent();
		}
		return font.ascent() * size + font.descent() * size;
	}

	public static Rectangle2D getTextRect(PGraphics g, PFont font, float size, String text, boolean useNativeFonts)
	{
		if (isJava2D(g) && useNativeFonts)
		{
			FontMetrics fm = getMetrics(g, font.getFont(), size);
			if (fm != null)
			{
				Graphics2D g2 = ((PGraphicsJava2D) g).g2;
				return fm.getStringBounds(text, g2);
			}
		}
		Rectangle2D.Float rect = new Rectangle2D.Float();
		rect.width = getTextWidth(g, font, size, text, useNativeFonts);
		rect.height = getTextHeight(g, font, size, text, useNativeFonts);
		return rect;
	}

	/**
	 * Retrieves the floating-point text width for the given string, font, size,
	 * and PGraphics context.
	 * 
	 * @param g
	 *            a PGraphics object
	 * @param font
	 *            a <code>PFont</code> object
	 * @param size
	 *            the font size
	 * @param useNativeFonts
	 *            true if the caller wishes to use Java's built-in font
	 *            functionality
	 * @return the width of the indicated text
	 */
	public static float getTextWidth(PGraphics g, PFont font, float size, String text, boolean useNativeFonts)
	{
		if (isJava2D(g) && useNativeFonts)
		{
			FontMetrics fm = null;
			while (fm == null)
			{
				try
				{
					fm = getMetrics(g, font.getFont(), size);
				} catch (Exception e)
				{
					try
					{
						Thread.sleep(200);
					} catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
			}

			// return fm.stringWidth(text);
		}
		char[] chars = text.toCharArray();
		float width = 0;
		for (int j = 0; j < chars.length; j++)
		{
			width += font.width(chars[j]) * size;
		}
		return width;
	}

	public static float getTextWidth(Graphics2D g2, Font font, float size, String text)
	{
		FontMetrics fm = g2.getFontMetrics(font.deriveFont(size));
		return (float) fm.getStringBounds(text, g2).getWidth();
	}

	public static float getTextHeight(Graphics2D g2, Font font, float size, String text)
	{
		FontMetrics fm = g2.getFontMetrics(font.deriveFont(size));
		return (float) fm.getStringBounds(text, g2).getHeight();
	}

	/**
	 * Convenience function for <code>isJava2D(PGraphics pg)</code>
	 * 
	 * @param p
	 *            the current <code>PApplet</code>
	 * @return true if the current <code>PApplet's</code> associated
	 *         <code>PGraphics</code> object is an instance of
	 *         <code>PGraphicsJava2D</code>
	 */
	public static boolean isJava2D(PApplet p)
	{
		return isJava2D(p.g);
	}

	/**
	 * Determines whether the indicated <code>PGraphics</code> object is an
	 * instance of <code>PGraphicsJava2D</code>
	 * 
	 * @param canvas
	 *            the <code>PGraphics</code> object to test
	 * @return true if the <code>PGraphics</code> object is an instance of
	 *         <code>PGraphicsJava2D</code>
	 */
	public static boolean isJava2D(PGraphics pg)
	{
		boolean is = pg.getClass() == PGraphicsJava2D.class;
		return is;
	}

	/**
	 * Calls the <code>lazyLoad()</code> method on all of the relevant "singlet"
	 * classes that are required for the correct functioning of all UI objects
	 * in this package.
	 * <p>
	 * This should be called in the constructor of every UIObject, so that the
	 * user doesn't need to call it him or herself.
	 * 
	 * @param p
	 *            The PApplet with which to associate the UI singlets.
	 * @see org.andrewberman.ui.EventManager
	 * @see org.andrewberman.ui.FocusManager
	 * @see org.andrewberman.ui.ShortcutManager
	 */
	//	public static void loadUISinglets(PApplet app)
	//	{
	//		if (p != app)
	//		{
	//			p = app;
	//			FontLoader.lazyLoad(p);
	//			FocusManager.lazyLoad(p);
	//			EventManager.lazyLoad(p);
	//			ShortcutManager.lazyLoad(p);
	//			setRenderingHints(p);
	//		}
	//	}
	/**
	 * Transforms a <code>Point</code> from model to scree ncoordinates in
	 * place.
	 * 
	 * @param pt
	 *            The point to transform in place. Should currently contain
	 *            model coordinates.
	 */
	public static void modelToScreen(Point2D.Float pt)
	{
		transform(modelview, pt);
		transform(cameraInv, pt);
	}

	public static Image PImageToImage(PApplet p, PImage image)
	{
		Image newImage = image.getImage();
		//		Image newImage = p.createImage(new MemoryImageSource(image.width,
		//				image.height, image.pixels, 0, image.width));
		return newImage;
	}

	/**
	 * Releases the specified cursor (i.e. returns to the default cursor) only
	 * if the specified object and cursor are currently set. This helps avoid
	 * the "flickering" effect of a bunch of UI objects trying to set and unset
	 * the cursor, because the cursor is only set back to default if the object
	 * which most recently set the cursor is calling the
	 * <code>releaseCursor</code> method.
	 * <p>
	 * Does that make any sense?
	 * <p>
	 * TODO: We could make a new class, CursorManager, to deal with this in a
	 * more organized way... but is it really worth it?
	 * 
	 * @param o
	 *            The object that is requesting this cursor release.
	 * @param p
	 *            The current PApplet
	 */
	public static void releaseCursor(Object o, PApplet p)
	{
		if (cursorOwner != o)
			return;
		//		System.out.println("Released.");
		cursorOwner = null;
		p.setCursor(baseCursor);
	}

	/**
	 * Convenience method for <code>resetMatrix(PGraphics pg)</code>.
	 * 
	 * @param p
	 *            a PApplet instance
	 */
	public static void resetMatrix(PApplet p)
	{
		resetMatrix(p.g);
	}

	/**
	 * Calls the correct method to reset the matrix of a PGraphics instance. The
	 * main reason for this is that PGraphicsJava2D requires
	 * <code>resetMatrix()</code>, while P3D and OpenGL require a
	 * <code>camera()</code> method, which Java2D unfortunately doesn't
	 * implement. Should we really have to do the same thing in two different
	 * ways? Probably not, But this seems to work well enough...
	 * 
	 * @param pg
	 */
	public static void resetMatrix(PGraphics pg)
	{
		if (isJava2D(pg) || pg.getClass() == PGraphics2D.class)
			pg.resetMatrix();
		else
		{
			pg.camera();
		}
	}

	/**
	 * Transforms a <code>Point</code> from screen to model coordinates in
	 * place.
	 * 
	 * @param pt
	 *            The point to transform in place. Should currently contain the
	 *            mouse coordinates.
	 */
	public static void screenToModel(Point2D.Float pt)
	{
		transform(camera, pt);
		transform(modelviewInv, pt);
	}

	/**
	 * Converts a <code>Rectangle2D</code> from "screen" to "model" coordinates.
	 * <p>
	 * Note that this package's definition of "model" coordintes is different
	 * from the standard Processing definition. It's all quite confusing, but
	 * this strategy seems to work. Maybe there's a better way?
	 * 
	 * @param rect
	 *            the <code>Redtangle2D</code> to convert (in place) from screen
	 *            to model coordinates.
	 */
	public static void screenToModel(Rectangle2D.Float rect)
	{
		/*
		 * Strategy: Go through all points in the rectangle, transforming each
		 * point into model coordinates. Then, find the smallest completely
		 * bounding rectangle in model space. Not simple, but it should work.
		 */
		tPoint.x = rect.x;
		tPoint.y = rect.y;
		transform(camera, tPoint);
		transform(modelviewInv, tPoint);
		float x1 = tPoint.x;
		float y1 = tPoint.y;

		tPoint.x = rect.x + rect.width;
		tPoint.y = rect.y;
		transform(camera, tPoint);
		transform(modelviewInv, tPoint);
		float x2 = tPoint.x;
		float y2 = tPoint.y;

		tPoint.x = rect.x + rect.width;
		tPoint.y = rect.y + rect.height;
		transform(camera, tPoint);
		transform(modelviewInv, tPoint);
		float x3 = tPoint.x;
		float y3 = tPoint.y;

		tPoint.x = rect.x;
		tPoint.y = rect.y + rect.height;
		transform(camera, tPoint);
		transform(modelviewInv, tPoint);
		float x4 = tPoint.x;
		float y4 = tPoint.y;

		float loX = PApplet.min(new float[] { x1, x2, x3, x4 });
		float loY = PApplet.min(new float[] { y1, y2, y3, y4 });
		float hiX = PApplet.max(new float[] { x1, x2, x3, x4 });
		float hiY = PApplet.max(new float[] { y1, y2, y3, y4 });

		rect.setFrameFromDiagonal(loX, loY, hiX, hiY);
	}

	/**
	 * Sets the base cursor. Generally called by ToolManager.
	 * 
	 * @param c
	 */
	public static void setBaseCursor(PApplet p, Cursor c)
	{
		baseCursor = c;
		if (cursorOwner == null)
			p.setCursor(baseCursor);
	}

	/**
	 * Sets the displayed cursor. This method effectively registers the Object
	 * <code>o</code> as the "owner" of the cursor from this point on. This
	 * means that for the cursor to be reset back to normal, the same object (
	 * <code>o</code>) must call the <code>releaseCursor</code> method. If any
	 * other object calls <code>releaseCursor</code> before <code>o</code>,
	 * nothing will happen.
	 * <p>
	 * Note, however, that any other object can, at any point, call
	 * <code>setCursor</code> and override <code>o's</code> "ownership" of the cursor.
	 * This causes some flickering when two objects are simultaneously trying to
	 * <code>set</code> and <code>release</code> the cursor, in particular when
	 * two objects are on top of each other.
	 * <p>
	 * TODO: Figure out a way to deal with objects on top of each other, without
	 * resorting to something extremely annoying like z-values.
	 * 
	 * @param o
	 *            The object which is requesting the cursor change.
	 * @param p
	 *            The current PApplet
	 * @param cursor
	 *            the cursor to change to (i.e. Cursor.HAND_CURSOR).
	 */
	public static void setCursor(Object o, PApplet p, int cursor)
	{
		cursorOwner = o;
		p.setCursor(Cursor.getPredefinedCursor(cursor));
	}

	/**
	 * Copies the relevant transformation matrices from the indicated
	 * <code>PApplet</code> into <code>UIUtils'</code> internal static cache.
	 * <p>
	 * This should be called at every iteration of the draw() cycle,
	 * <em><b>after</b></em> all relevant matrix transformations have been
	 * performed (i.e. camera, rotation, etc.). The matrix cache created here is
	 * then used when calling the <code>UIUtils.screenToModel</code> or
	 * associated methods.
	 * 
	 * @param p
	 *            the PApplet from which to glean the matrix information.
	 * @see org.andrewberman.ui.UIUtils.screenToModel
	 */
	public static void setMatrix(PApplet p)
	{
		if (isJava2D(p))
		{
			PGraphicsJava2D g = (PGraphicsJava2D) p.g;
			AffineTransform tr = g.g2.getTransform();
			try
			{
				affineToPMatrix3D(tr, modelview);
				// tr.invert();
				affineToPMatrix3D(tr.createInverse(), modelviewInv);
				camera.reset();
				cameraInv.reset();
			} catch (NoninvertibleTransformException e)
			{
				return;
			}
		} else
		{
			p.g.getMatrix();
			camera.set(p.g.getMatrix());
			PMatrix pm = p.g.getMatrix();
			pm.invert();
			cameraInv.set(pm);
			//			p.g.get
			//			modelview.set(p.g.modelview);
			//			modelviewInv.set(p.g.modelviewInv);

		}
	}

	public static void setRenderingHints(Graphics2D g2)
	{
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	public static void setRenderingHints(PGraphics g)
	{
		if (g instanceof PGraphicsJava2D)
		{
			PGraphicsJava2D pg = (PGraphicsJava2D) g;
			g.smooth();
			Graphics2D g2 = pg.g2;
			setRenderingHints(g2);
		}
	}

	/**
	 * Performs an in-place matrix transformation of a <code>Point</code>.
	 * 
	 * @param mat
	 *            the transformation <code>PMatrix3D</code>
	 * @param pt
	 *            the <code>Point</code> to be transformed in place
	 */
	public static void transform(PMatrix3D mat, Point2D.Float pt)
	{
		float x = pt.x;
		float y = pt.y;
		float z = 0;

		pt.x = mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03;
		pt.y = mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13;
	}
}