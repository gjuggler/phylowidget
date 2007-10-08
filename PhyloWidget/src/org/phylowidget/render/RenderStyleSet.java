package org.phylowidget.render;

import java.lang.reflect.Field;
import java.util.Properties;

import org.andrewberman.ui.Color;
import org.phylowidget.PhyloWidget;

public class RenderStyleSet
{

	public Color backgroundColor;
	public Color foregroundColor;
	
	public float regStroke;

	public Color hoverColor;
	public float hoverStroke;

	public Color dimColor;
	public float dimStroke;

	public Color copyColor;
	public float copyStroke;

	public float nodeSizeMultiplier;
	public float lineThicknessMultiplier;

	/*
	 * Text rotation in degrees.
	 */
	public float textRotation;
	
	private static RenderStyleSet defaultSet;

	public static RenderStyleSet defaultStyle()
	{
		if (defaultSet == null)
		{
			defaultSet = new RenderStyleSet();
			defaultSet.loadDefaults();
			defaultSet.loadFromProperties();
		}
		return defaultSet;
	}

	private void loadDefaults()
	{

		foregroundColor = new Color(Color.black);
		regStroke = 1f;

		dimColor = foregroundColor.brighter(200);
		dimStroke = 4f;

		hoverColor = new Color(100, 150, 255);
		hoverStroke = 3f;

		copyColor = new Color(255, 0, 0);
		copyStroke = 4f;

		nodeSizeMultiplier = 0.5f;
		lineThicknessMultiplier = 0.1f;
	}

	private void loadFromProperties()
	{
		Properties p = PhyloWidget.props;
		Class c = this.getClass();
		Field[] fields = c.getFields();
		for (int i = 0; i < fields.length; i++)
		{
			Field f = fields[i];
			String s = f.getName();
			if (p.containsKey(s))
			{
				try
				{
					String value = (String) p.get(s);
					Class fieldType = f.getType();
					if (fieldType == Float.TYPE)
					{
						f.setFloat(this, Float.parseFloat(value));
					} else if (fieldType == Color.class)
					{
						// parse the color triplet.
						String[] rgb = value.split(",");
						Color color = new Color(Integer.parseInt(rgb[0]),
								Integer.parseInt(rgb[1]), Integer
										.parseInt(rgb[2]));
						f.set(this, color);
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}
			}
		}
	}

}
