/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;
import processing.core.PFont;

public class FontLoader
{
	private PApplet p;
	
	private PFont pfont;
	private Font font;
	
	public FontLoader(PApplet p)
	{
		this.p = p;
		pfont = p.loadFont("BitstreamVeraSans-Roman-36.vlw");
		try
		{
			InputStream in = p.createInput("vera.ttf");
			font = Font.createFont(Font.TRUETYPE_FONT, in);
			in.close();
			pfont.setFont(font);
		} catch (FontFormatException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setFont(String fontName)
	{
		// Create a HashMap of TextAttributes which we'll use to create the font.
		 Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
		 attributes.put(TextAttribute.FAMILY, fontName);
		 attributes.put(TextAttribute.SIZE, (float)1);
		 
		 this.font = Font.getFont(attributes);
		 pfont.setFont(font);
	}
	
	public PFont getPFont()
	{
		return pfont;
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public String getFontName()
	{
		return font.getFamily();
	}
}
