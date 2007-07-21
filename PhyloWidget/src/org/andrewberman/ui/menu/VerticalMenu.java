package org.andrewberman.ui.menu;

/**
 * A <code>VerticalMenu</code> is a menu that lays out and displays its
 * sub-items in a vertical box. This menu type is especially well-suited for the
 * drop-down menus that users expect from a context menu or toolbar menu.
 */
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.Sizable;

import processing.core.PApplet;
import processing.core.PFont;

public class VerticalMenu extends Menu
{
	public VerticalMenu(PApplet app)
	{
		super(app);
	}

	public MenuItem create(String label)
	{
		return new VerticalMenuItem(label);
	}

	protected void drawAfter()
	{
		VerticalMenuItem.drawChildrenRect(this);
	}

	public void layout()
	{
		/*
		 * Calculate the height of each row in this menu.
		 */
		PFont font = style.font;
		float fontSize = style.fontSize;
		float descent = UIUtils.getTextDescent(menu.buff, font, fontSize, true);
		float ascent = UIUtils.getTextAscent(menu.buff, font, fontSize, true);
		float textHeight = descent + ascent;
		float itemHeight = textHeight + 2 * style.padY;
		/*
		 * Calculate the width of the "submenu" triangle shape.
		 */
		float innerHeight = textHeight;
		AffineTransform at = AffineTransform.getScaleInstance(innerHeight / 2,
				innerHeight / 2);
		Area a = style.subTriangle.createTransformedArea(at);
		VerticalMenuItem.tri = a;
		VerticalMenuItem.triWidth = (float) a.getBounds2D().getWidth();
		/*
		 * Calculate the max width of text in the menu items.
		 */
		float itemWidth = getMaxWidth();
		/*
		 * Set the width, height and position for the top-level items.
		 */
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			if (item instanceof Sizable)
			{
				Sizable size = (Sizable) item;
				size.setSize(itemWidth, itemHeight);
			}
			if (item instanceof Positionable)
			{
				Positionable pos = (Positionable) item;
				if (menu == this)
					pos.setPosition(x, y + i * itemHeight);
				else
					pos.setPosition(x, y + i * itemHeight);
			}
		}
		// Trigger the recursive layout for the rest of the menu.
		super.layout();
	}
}
