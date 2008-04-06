package org.phylowidget.render;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.andrewberman.ui.UIUtils;

import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PImage;

public abstract class DoubleBuffer implements Runnable
{
	private PGraphics offscreen;
	private PGraphics onscreen;

	Rectangle2D.Float onscreenRect;

	boolean shouldRepaint;
	static Thread repaintThread;

	public DoubleBuffer()
	{
		repaintThread = new Thread(this, "DoubleBuffer");
		repaintThread.start();
	}

	synchronized void triggerRepaint()
	{
		synchronized (this)
		{
			shouldRepaint = true;
			notifyAll();
		}
	}

	private void allocateBuffers(PGraphics canvas)
	{
		offscreen = new PGraphicsJava2D(canvas.width, canvas.height, null)
		{
			@Override
			public String toString()
			{
				return "offscreen";
			}
		};
		onscreen = new PGraphicsJava2D(canvas.width, canvas.height, null)
		{
			@Override
			public String toString()
			{
				return "onscreen";
			}
		};
		UIUtils.setRenderingHints(offscreen);
		UIUtils.setRenderingHints(onscreen);
		onscreen.loadPixels();

		//		offscreen = canvas.parent.createGraphics(canvas.width, canvas.height, PGraphics.JAVA2D);
		//		onscreen = canvas.parent.createGraphics(canvas.width, canvas.height, PGraphics.JAVA2D);
	}

	public void drawToCanvas(PGraphics canvas)
	{
		if (offscreen == null || offscreen.width != canvas.width
				|| offscreen.height != canvas.height)
		{
			/*
			 * We have to re-allocate the offscreen image.
			 */
			allocateBuffers(canvas);
		}

		/*
		 * Trigger a repaint in the rendering thread.
		 */
		triggerRepaint();

		synchronized (onscreen)
		{
			/*
			 *  Blit the onscreen buffer onto the current canvas.
			 */
			canvas.image(onscreen, 0, 0);
		}
	}

	public void drawToBuffer(PGraphics g)
	{
		
	}
	
	public void run()
	{
		while (true)
		{
			if (stopRunning)
				break;

			if (shouldRepaint)
			{
				offscreen.beginDraw();
				drawToBuffer(offscreen);
				offscreen.endDraw();
				synchronized (onscreen)
				{
					/*
					 * Switch the offscreen and onscreen buffers.
					 */
					PGraphics temp = offscreen;
					offscreen = onscreen;
					onscreen = temp;
				}
			}
//			System.out.println("Finished rendering. Waiting for signal...");
			try
			{
				synchronized (this)
				{
					wait();
				}
			} catch (InterruptedException e)
			{
			}
		}
	}

	private boolean stopRunning = false;

	public void dispose()
	{
		stopRunning = true;
		this.notify();
	}

}
