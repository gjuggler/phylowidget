package org.andrewberman.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.core.PFont;

public class FontLoader
{
	private PApplet p;
	
	public PFont vera;
	
	static public FontLoader instance;
	
	
	public static void lazyLoad(PApplet p2)
	{
		if (instance == null)
			instance = new FontLoader(p2);
	}

	public FontLoader(PApplet p)
	{
		this.p = p;
		
		vera = p.loadFont("BitstreamVeraSans-Roman-12.vlw");
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
