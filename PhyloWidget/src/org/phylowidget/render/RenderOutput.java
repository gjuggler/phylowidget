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
package org.phylowidget.render;

import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.andrewberman.ui.UIUtils;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.TreeManager;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

import com.lowagie.text.Document;

public class RenderOutput
{
	public static boolean isOutputting = false;

	public static Document doc;
	
	
	
	public static synchronized void savePDF(PApplet p, BasicTreeRenderer r, boolean zoomToFull, boolean showAllLabels)
	{
		PWContext context = PWPlatform.getInstance().getThisAppContext();
		isOutputting = true;
		RootedTree t = r.getTree();
		float oldThreshold = context.config().renderThreshold;
		context.config().renderThreshold = Integer.MAX_VALUE;
		boolean oldDoubleBuff = context.config().useDoubleBuffering;
		context.config().useDoubleBuffering = false;
		float oldTextSize = context.config().minTextSize;
		if (showAllLabels)
			context.config().minTextSize = 0;
		try
		{
			context.getPW().setMessage("Outputting PDF...");
			preprocess(t);
			//			File f = p.outputFile("Save PDF as...");
			//			String s = p.selectOutput("Save PDF as...");
			String fileType = "PDF";
			FileDialog fd =
					new FileDialog(context.ui().getFrame(), "Choose your desination " + fileType + " file.",
							FileDialog.SAVE);
			fd.pack();
			fd.setVisible(true);
			String directory = fd.getDirectory();
			String filename = fd.getFile();
			if (filename == null)
			{
				context.getPW().setMessage("Output cancelled.");
				return;
			}
			// Fix a non-PDF extension.
			if (!filename.toLowerCase().endsWith((".pdf")))
			{
				filename += ".pdf";
			}
			File f = new File(directory,filename);
			p.noLoop();
			PGraphics canvas = p.createGraphics(p.width, p.height, PConstants.PDF);
			canvas.setPath(f.getAbsolutePath());
			canvas.setSize(p.width, p.height);
			canvas.beginDraw();
			if (context.config().debug)
				System.out.println("BEGIN DRAW");

			/*
			 * Create the render rectangle.
			 */
			Rectangle2D.Float rect = TreeManager.cameraRect;
			Rectangle2D.Float oldRect = rect;
			if (zoomToFull)
			{
				TreeManager.camera.fillScreen(0.5f);
				TreeManager.camera.fforward();
				context.trees().update();
				rect = TreeManager.cameraRect;
			}
			/*
			 * Do the rendering!
			 */
			r.render(canvas, rect.x, rect.y, rect.width, rect.height, true);

			if (context.config().debug)
				System.out.println("END DRAW");
			canvas.endDraw();
			canvas.dispose();
			context.getPW().setMessage("Output complete.");
		} catch (Exception e)
		{
			e.printStackTrace();
			context.getPW().setMessage("PDF output failed: " + e.getMessage());
		} finally
		{
			context.config().renderThreshold = oldThreshold;
			context.config().minTextSize = oldTextSize;
			context.config().useDoubleBuffering = oldDoubleBuff;
			isOutputting = false;
			p.loop();
		}
	}

	public static synchronized void save(PApplet p, BasicTreeRenderer r, boolean zoomToFull, boolean showAllLabels,
			String fileType, int w, int h)
	{
		PWContext context = PWPlatform.getInstance().getThisAppContext();
		isOutputting = true;
		float oldThreshold = context.config().renderThreshold;
		context.config().renderThreshold = Integer.MAX_VALUE;
		boolean oldDoubleBuff = context.config().useDoubleBuffering;
		context.config().useDoubleBuffering = false;
		float oldTextSize = context.config().minTextSize;
		if (showAllLabels)
			context.config().minTextSize = 0;
		try
		{
			context.getPW().setMessage("Outputting image...");
			RootedTree t = r.getTree();
			preprocess(t);

			FileDialog fd =
					new FileDialog(context.ui().getFrame(), "Choose your desination " + fileType + " file.",
							FileDialog.SAVE);
			fd.pack();
			fd.setVisible(true);
			String directory = fd.getDirectory();
			String filename = fd.getFile();
			if (filename == null)
			{
				context.getPW().setMessage("Output cancelled.");
				return;
			}
			if (!filename.toLowerCase().endsWith(fileType.toLowerCase()))
			{
				filename += "." + fileType.toLowerCase();
			}
			File f = new File(directory, filename);

			p.noLoop();
			PGraphicsJava2D canvas = (PGraphicsJava2D) p.g;
			Image oldImage = canvas.image;
			Graphics2D oldG2 = canvas.g2;
			int oldW = canvas.width;
			int oldH = canvas.height;
			canvas.width = w;
			canvas.height = h;
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			Graphics2D g2 = img.createGraphics();
			UIUtils.setRenderingHints(g2);
			canvas.image = img;
			canvas.g2 = g2;
			canvas.beginDraw();
			prettyHints(canvas);
			canvas.background(255);

			/*
			 * Create the render rectangle.
			 */
			Rectangle2D.Float rect = TreeManager.cameraRect;
			float wFactor = w / oldW;
			float hFactor = h / oldH;
			if (zoomToFull)
				rect.setRect(0, 0, oldW, oldH);
			//			System.out.println(rect);
			r.render(canvas, rect.x * wFactor, rect.y * hFactor, rect.width * wFactor, rect.height * hFactor, true);

			canvas.endDraw();
			canvas.loadPixels();
			canvas.save(f.getAbsolutePath());
			g2.dispose();
			canvas.image = oldImage;
			canvas.g2 = oldG2;
			canvas.width = oldW;
			canvas.height = oldH;
			context.getPW().setMessage("Output complete.");
		} catch (Exception e)
		{
			e.printStackTrace();
			context.getPW().setMessage("Output failed: " + e.getMessage());
			System.gc();
			return;
		} finally
		{
			context.config().renderThreshold = oldThreshold;
			context.config().minTextSize = oldTextSize;
			context.config().useDoubleBuffering = oldDoubleBuff;
			isOutputting = false;
			p.loop();
		}
	}

	private static void prettyHints(PGraphicsJava2D g)
	{
		Graphics2D g2 = g.g2;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
	}

	private static void preprocess(RootedTree t)
	{
		/*
		 * Go through all the nodes and remove any cut/copy/paste states.
		 */
		ArrayList nodes = new ArrayList();
		t.getAll(t.getRoot(), null, nodes);
		for (int i = 0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
			n.setState(PhyloNode.NONE);
		}
		
		PWContext context = PWPlatform.getInstance().getThisAppContext();
		PhyloTree pt = (PhyloTree) context.trees().getTree();
		pt.hoveredNode = null;
	}
}
