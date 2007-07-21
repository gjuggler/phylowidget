package org.phylowidget.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.core.PFont;

public class FontLoader
{
	private static PApplet p = PhyloWidget.p;

	public static PFont vera = p.loadFont("BitstreamVeraSans-Roman-48.vlw");
	
	static {
		InputStream in = p.openStream("vera.ttf");
		try
		{
			vera.font = Font.createFont(Font.TRUETYPE_FONT, in);
			in.close();
		} catch (FontFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
