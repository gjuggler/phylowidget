package org.phylowidget.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.phylowidget.tree.RootedTree;
import org.phylowidget.ui.PhyloNode;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PImage;
import processing.pdf.PGraphicsPDF;

public class RenderOutput
{

	public static void savePDF(PApplet p,RootedTree t, TreeRenderer r)
	{
		preprocess(t);
		File f = p.outputFile("Save PDF as...");
		p.noLoop();
		PGraphicsPDF canvas = (PGraphicsPDF) p.createGraphics(p.width,p.height,PConstants.PDF,f.getAbsolutePath());
		canvas.beginDraw();
		r.render(canvas, 0, 0, canvas.width,canvas.height);
		canvas.endDraw();
		canvas.dispose();
//		canvas.save(f.getAbsolutePath());
		p.loop();
	}
	
	public static void save(PApplet p,RootedTree t, TreeRenderer r,int w,int h)
	{
		preprocess(t);
		File f = p.outputFile("Save image as...");
//		try {
//			System.getSecurityManager().checkWrite(f.getAbsolutePath());
//		} catch (SecurityException e)
//		{
//			return;
//		}
		
		p.noLoop();
		int width = Math.min(1600, p.width*4);
		int height = Math.min(1200, p.height*4);
		PGraphicsJava2D canvas = (PGraphicsJava2D) p.createGraphics(width,height, PConstants.JAVA2D);
		canvas.beginDraw();
		prettyHints(canvas);
		canvas.background(255);
		r.render(canvas, 0, 0, canvas.width,canvas.height);
		canvas.endDraw();
		canvas.loadPixels();
		PImage img = canvas.get();
		canvas.dispose();
		img.loadPixels();
		img.save(f.getAbsolutePath());
		p.loop();
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
		for (int i=0; i < nodes.size(); i++)
		{
			PhyloNode n = (PhyloNode) nodes.get(i);
			n.setState(PhyloNode.NONE);
			n.hovered = false;
		}
	}
}
