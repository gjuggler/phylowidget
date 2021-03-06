package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.andrewberman.ui.UIUtils;
import org.phylowidget.PWPlatform;

import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

public class DoubleBuffer implements Runnable
{
	private PGraphicsJava2D dummyGraphics;
	private BufferedImage offscreen;
	private BufferedImage onscreen;
	private Graphics2D offscreenG;
	private Graphics2D onscreenG;
	//	private PGraphics offscreen;
	//	private PGraphics onscreen;

	Rectangle2D.Float onscreenRect;

	boolean shouldRepaint;
	protected boolean shouldTriggerRepaint = false;
	Thread repaintThread;

	public DoubleBuffer()
	{
		dummyGraphics = new PGraphicsJava2D();
		dummyGraphics.setParent(PWPlatform.getInstance().getThisAppContext().getPW());
		repaintThread = new Thread(this, "DoubleBuffer");
		//		repaintThread.setPriority(Thread.MIN_PRIORITY);
		repaintThread.start();
		
		shouldTriggerRepaint = true;

		//		dummyGraphics.g2.dispose();
		//		dummyGraphics.smooth();
	}

	synchronized void triggerRepaint()
	{
		synchronized (this)
		{
			shouldRepaint = true;
			notifyAll();
		}
	}

	private synchronized void allocateBuffers(PGraphics canvas)
	{
		if (offscreen != null)
			offscreen.flush();
		if (onscreen != null)
			onscreen.flush();
		count++;
		offscreen = new BufferedImage(canvas.width, canvas.height,
				BufferedImage.TYPE_INT_ARGB);
		onscreen = new BufferedImage(canvas.width, canvas.height,
				BufferedImage.TYPE_INT_ARGB);
		offscreenG = offscreen.createGraphics();
		onscreenG = onscreen.createGraphics();
		UIUtils.setRenderingHints(offscreenG);
		UIUtils.setRenderingHints(onscreenG);

		dummyGraphics.width = canvas.width;
		dummyGraphics.height = canvas.height;
		//		dummyGraphics.smooth();

		//		UIUtils.setRenderingHints(offscreen);
		//		UIUtils.setRenderingHints(onscreen);
		//		onscreen.loadPixels();

		//		offscreen = canvas.parent.createGraphics(canvas.width, canvas.height, PGraphics.JAVA2D);
		//		onscreen = canvas.parent.createGraphics(canvas.width, canvas.height, PGraphics.JAVA2D);
	}

	public void drawDoubleBuffered(PGraphics canvas)
	{
		if (offscreen == null || offscreen.getWidth() != canvas.width
				|| offscreen.getHeight() != canvas.height)
		{
			/*
			 * We have to re-allocate the offscreen image.
			 */
			allocateBuffers(canvas);
		}

		/*
		 * Trigger a repaint in the double-buffering thread.
		 */
		if (shouldTriggerRepaint)
		{
			triggerRepaint();
		}

		synchronized (onscreen)
		{
			/*
			 *  Blit the onscreen buffer onto the current canvas.
			 */
			canvas.loadPixels();
//			Graphics2D g2 = (Graphics2D) canvas.getImage().getGraphics();
			BufferedImage bi = (BufferedImage) canvas.image;
//			BufferedImage bi = (BufferedImage) canvas.getImage();
			Graphics2D g2 = bi.createGraphics();
			g2.drawImage(onscreen, 0, 0, null);
			g2.dispose();
		}
	}

	public void drawToBuffer(PGraphics g)
	{
//		System.out.println("Draw" + System.currentTimeMillis());
	}

	public void run()
	{
		while (true)
		{
			if (stopRunning)
				break;
			if (shouldRepaint)
			{
				synchronized (this)
				{
					dummyGraphics.image = offscreen;
					dummyGraphics.g2 = offscreenG;

					try
					{
						drawToBuffer(dummyGraphics);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					/*
					 * These lines are important for garbage collection!!!
					 */
					dummyGraphics.image = null;
					dummyGraphics.g2 = null;
				}

				synchronized (onscreen)
				{
					/*
					 * Switch the offscreen and onscreen buffers.
					 */
					BufferedImage temp = offscreen;
					Graphics2D tempG = offscreenG;
					offscreen = onscreen;
					offscreenG = onscreenG;
					onscreen = temp;
					onscreenG = tempG;
				}
			}
			if (stopRunning)
				break;
			try
			{
				synchronized (this)
				{
					wait();
				}
			} catch (InterruptedException e)
			{
				e.printStackTrace();
				continue;
			}
		}
		dummyGraphics = null;
		offscreen = null;
		onscreen = null;
		offscreenG = null;
		onscreenG = null;
		onscreenRect = null;
		repaintThread = null;
	}

	static int count = 0;

	private boolean stopRunning = false;

	public synchronized void dispose()
	{
		stopRunning = true;
		this.notifyAll();
	}

}
