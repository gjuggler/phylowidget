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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;

import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeManager;
import org.phylowidget.ui.PhyloNode;
import org.phylowidget.ui.PhyloTree;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphicsJava2D;
import processing.core.PImage;
import processing.pdf.PGraphicsPDF;

public class RenderOutput
{

	public static boolean isOutputting;

	public static synchronized void savePDF(PApplet p, RootedTree t,
			TreeRenderer r)
	{
		/*
		 * Change the rendering threshold to the size of the tree.
		 */
		float numNodes = t.vertexSet().size();
		float oldThreshold = PhyloWidget.ui.renderThreshold;
		PhyloWidget.ui.renderThreshold = numNodes;
		try
		{
			PhyloWidget.setMessage("Outputting PDF...");
			preprocess(t);
			File f = p.outputFile("Save PDF as...");
			p.noLoop();
			isOutputting = true;
			PGraphicsPDF canvas = (PGraphicsPDF) p.createGraphics(p.width,
					p.height, PConstants.PDF, f.getAbsolutePath());
			canvas.beginDraw();
			Rectangle2D.Float rect = TreeManager.cameraRect;
			r.render(canvas, rect.x, rect.y, rect.width, rect.height, true);
			canvas.endDraw();
			canvas.dispose();
			// canvas.save(f.getAbsolutePath());
			PhyloWidget.setMessage("Output complete.");
		} catch (Exception e)
		{
			e.printStackTrace();
			PhyloWidget.setMessage("PDF output failed!");
		} finally
		{
			PhyloWidget.ui.renderThreshold = oldThreshold;
			isOutputting = false;
			p.loop();
		}
	}

	public static synchronized void save(PApplet p, RootedTree t,
			TreeRenderer r, int w, int h)
	{
		try
		{
			PhyloWidget.setMessage("Outputting image...");
			preprocess(t);
			File f = p.outputFile("Save image as...");
			p.noLoop();
			int width = Math.min(1600, p.width * 4);
			int height = Math.min(1200, p.height * 4);
			PGraphicsJava2D canvas = (PGraphicsJava2D) p.createGraphics(width,
					height, PConstants.JAVA2D);
			canvas.beginDraw();
			prettyHints(canvas);
			canvas.background(255);
			Rectangle2D.Float rect = TreeManager.cameraRect;
			r.render(canvas, rect.x, rect.y, rect.width, rect.height, true);
			canvas.endDraw();
			canvas.loadPixels();
			PImage img = canvas.get();
			canvas.dispose();
			img.loadPixels();
			img.save(f.getAbsolutePath());
			PhyloWidget.setMessage("Output complete.");
		} catch (RuntimeException e)
		{
			PhyloWidget.setMessage("Output failed!");
		} finally
		{
			p.loop();
		}
	}

	private static void prettyHints(PGraphicsJava2D g)
	{
		Graphics2D g2 = g.g2;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
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
		PhyloTree pt = (PhyloTree) PhyloWidget.trees.getTree();
		pt.hoveredNode = null;
	}
}
