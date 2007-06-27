package org.andrewberman.ui;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import processing.core.PApplet;
import processing.core.PGraphicsJava2D;
import processing.core.PMatrix;

public final class ProcessingUtils
{

	private static PMatrix temp = new PMatrix();
	
	/**
	 * This should be called at the end of every draw() run.
	 * @param mat the modelview matrix.
	 */
	public static void setMatrix(PApplet p)
	{
		temp.set(p.g.modelviewInv);
//		temp.invert();
	}
	
	/**
	 * 
	 * @param p The PApplet from which to base the transformation.
	 * @param pt The point to transform in place. Should currently contain the mouse
	 * coordinates.
	 */
	public static void mouseToModel(PApplet p, Point2D.Float pt)
	{
		if (p.g.getClass() == PGraphicsJava2D.class)
		{
			// If we're in Java2D, we need to get the AffineTransform and do an inverse.
			PGraphicsJava2D g = (PGraphicsJava2D) p.g;
//			g.loadMatrix();
			AffineTransform a = g.g2.getTransform();
			AffineTransform inv;
			try
			{
				inv = a.createInverse();
				inv.transform(pt, pt);
			} catch (NoninvertibleTransformException e1)
			{
				inv = a;
				System.err.println("Error transforming coordinates!");
			}
		} else
		{
			// We're using P3D or OpenGL.
			
			// Re-center the x and y coordinates back to zero-centered.
			pt.x -= p.width/2;
			pt.y -= p.height/2;
			
			// Transform the mouse coordinates into model space.
		    float modelX =
		        temp.m00*pt.x + temp.m01*pt.y + temp.m03;
			float modelY =
				temp.m10*pt.x + temp.m11*pt.y + temp.m13;
			pt.setLocation(modelX,modelY);
		}
	}
}