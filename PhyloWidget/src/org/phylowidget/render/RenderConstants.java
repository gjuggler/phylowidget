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
package org.phylowidget.render;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.lang.reflect.Field;
import java.util.Properties;

import org.andrewberman.ui.Color;
import org.phylowidget.PhyloWidget;

public class RenderConstants
{
//	public Color textColor;
//	public Color nodeColor;
//	public Color lineColor;
	
	public static float regStroke = 1f;

	public static Color hoverColor = new Color(100,150,255);
	public static float hoverStroke = 1.5f;

	public static Color dimColor = PhyloWidget.cfg.getTextColor().brighter(200);
	public static float dimStroke = 1f;

	public static Color copyColor = new Color(255,0,0);
	public static float copyStroke = 1f;
	
	public static Color foundColor = new Color(255,155,0);
	public static float foundStroke = 3f;
	public static Color foundBackground = new Color(255,255,255);
	public static Color foundForeground = foundColor;
	
	public static float labelSpacing = 2f;

	public static Stroke stroke(float weight)
	{
		return new BasicStroke(weight);
	}
}
