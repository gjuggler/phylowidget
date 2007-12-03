package org.andrewberman.ui.menu;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.UIUtils;

import processing.core.PGraphicsJava2D;

public class MenuUtils
{

	static RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float();

	public static synchronized void drawWhiteTextRect(MenuItem item, float x, float y, float w, float h)
	{
		Graphics2D g2 = item.menu.buff.g2;

		roundRect.setRoundRect(x, y, w, h, h / 3,
				h / 3);
		if (!item.isEnabled())
			g2.setPaint(item.menu.style.stateColors[MenuItem.DISABLED]);
		else
			g2.setPaint(Color.white);
		g2.fill(roundRect);
		g2.setPaint(Color.black);
		g2.setStroke(item.menu.style.stroke);
		g2.draw(roundRect);
	}

	public static synchronized void drawBackgroundRoundRect(MenuItem item,
			float x, float y, float width, float height)
	{
		PGraphicsJava2D buff = item.menu.buff;
		StyleSet style = item.menu.style;
		Menu menu = item.menu;

		roundRect.setRoundRect(x, y, width, height, style.roundOff,
				style.roundOff);

		buff.g2.setPaint(style.menuBackground);
		buff.g2.fill(roundRect);

		buff.g2.setStroke(style.stroke);
		buff.g2.setPaint(style.strokeColor);
		buff.g2.draw(roundRect);
	}

	public static synchronized void drawDoubleGradientRect(MenuItem item,
			float x, float y, float width, float height)
	{
		PGraphicsJava2D buff = item.menu.buff;
		StyleSet style = item.menu.style;
		Menu menu = item.menu;

		roundRect.setRoundRect(x, y, width, height, style.roundOff,
				style.roundOff);
		/*
		 * Draw the first gradient: a full-height gradient.
		 */
		buff.g2.setPaint(style.getGradient(y, y + height));
		buff.g2.fill(roundRect);
		/*
		 * Draw a translucent gradient on top of the first, starting halfway and
		 * going to the bottom.
		 */
		Composite oldC = buff.g2.getComposite();
		AlphaComposite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f * menu.alpha);
		buff.g2.setComposite(c);
		buff.g2.setPaint(style.getGradient(y + height, y + height / 3));
		buff.g2.fillRect((int) x, (int) (y + height / 2), (int) width,
				(int) height / 2);
		buff.g2.setComposite(oldC);
		/*
		 * Finally, draw the stroke on top of everything.
		 */
		RenderingHints rh = menu.buff.g2.getRenderingHints();
		buff.g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		buff.g2.setPaint(style.strokeColor);
		buff.g2.setStroke(style.stroke);
		buff.g2.draw(roundRect);
		buff.g2.setRenderingHints(rh);

	}

	public static synchronized void drawBlankRect(MenuItem item, float x, float y, float w, float h)
	{
		Menu menu = item.menu;
		StyleSet style = menu.style;

		roundRect.setRoundRect(x, y, w, h, 0,0);
		Graphics2D g2 = menu.buff.g2;
		
		g2.setPaint(menu.style.strokeColor);
		g2.setStroke(menu.style.stroke);
		g2.draw(roundRect);
	}
	
	public static synchronized void drawSingleGradientRect(MenuItem item,
			float x, float y, float width, float height)
	{
		drawSingleGradientRect(item, x, y, width, height, item.menu.style.roundOff);
	}
	
	public static synchronized void drawSingleGradientRect(MenuItem item,
			float x, float y, float width, float height, float roundOff)
	{
		Menu menu = item.menu;
		StyleSet style = menu.style;

		roundRect.setRoundRect(x, y, width, height, roundOff,roundOff);
		Graphics2D g2 = menu.buff.g2;
		/*
		 * Set the correct fill gradient
		 */
		if (item.isOpen())
		{
			g2.setPaint(menu.style.getGradient(MenuItem.DOWN, y, y + height));
		} else
		{
			g2.setPaint(menu.style.getGradient(MenuItem.OVER, y, y + height));
		}
		/*
		 * Only perform the fill if the mood is right.
		 */
		if (item.getState() != MenuItem.UP || item.isOpen())
		{
			g2.fill(roundRect);
		}
		/*
		 * Draw the rounded rectangle outline.
		 */
		if (item.getState() != MenuItem.UP || item.isOpen())
		{
			RenderingHints rh = menu.buff.g2.getRenderingHints();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setPaint(menu.style.strokeColor);
			g2.setStroke(menu.style.stroke);
			g2.draw(roundRect);
			g2.setRenderingHints(rh);
		}
	}

	public static void drawCenteredText(MenuItem item)
	{
		drawText(item, item.getName(), true, true, item.x, item.y, item.width,
				item.height);
	}

	public static void drawLeftText(MenuItem item, String s, float x)
	{
		drawText(item, s, false, true, x, item.y, item.width, item.height);
	}
	
	public static void drawText(MenuItem item, String s, boolean centerH,
			boolean centerV, float x, float y, float width, float height)
	{

		Graphics2D g2 = item.menu.buff.g2;

		float descent = getTextDescent(item);

		float xOffset = 0, yOffset = 0;
		if (centerH)
		{
			float tWidth = getTextWidth(item, s);
			xOffset = (width - tWidth) / 2f;
		}
		if (centerV)
		{
			float tHeight = getTextHeight(item, s);
			yOffset = (height - tHeight) / 2f + descent;
		} else
			yOffset = height - descent;

		g2.setFont(item.menu.style.font.font);
		g2.setPaint(item.menu.style.strokeColor);
		g2.setStroke(item.menu.style.stroke);
		g2.drawString(s, x + xOffset, y + yOffset);
	}

	public static float getTextWidth(MenuItem item, String s)
	{
		StyleSet style = item.menu.style;
		return UIUtils.getTextWidth(item.menu.buff, style.font, style.fontSize,
				s, true);
	}

	public static float getTextHeight(MenuItem item, String s)
	{
		StyleSet style = item.menu.style;
		return UIUtils.getTextHeight(item.menu.buff, style.font,
				style.fontSize, s, true);
	}

	public static float getTextAscent(MenuItem item)
	{
		StyleSet style = item.menu.style;
		return UIUtils.getTextAscent(item.menu.buff, style.font,
				style.fontSize, true);
	}

	public static float getTextDescent(MenuItem item)
	{
		StyleSet style = item.menu.style;
		return UIUtils.getTextAscent(item.menu.buff, style.font,
				style.fontSize, true);
	}
}
