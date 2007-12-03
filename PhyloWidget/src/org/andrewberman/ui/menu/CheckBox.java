package org.andrewberman.ui.menu;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Field;
import java.text.DecimalFormat;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;

import processing.core.PApplet;

public class CheckBox extends MenuItem
{
	public final static float CHECKBOX_SIZE = .9f;
	
	private boolean value;

	private Field field;
	private Object fieldObj;
	private boolean useReflection;

	private float tWidth, nWidth, nOffsetX, nHeight, nOffsetY;

	public void setProperty(Object obj, String prop)
	{
		try
		{
			field = obj.getClass().getField(prop);
			fieldObj = obj;
			useReflection = true;
		} catch (Exception e)
		{
			e.printStackTrace();
			field = null;
			return;
		}
		// setVal(defaultValue);
	}

	public void setValue(String s)
	{
		setVal(Boolean.parseBoolean(s));
	}

	public void setVal(boolean value)
	{
		this.value = value;
		if (useReflection)
		{
			try
			{
				field.setBoolean(fieldObj, value);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void drawMyself()
	{
		super.drawMyself();

		float curX = x + menu.style.padX;
		MenuUtils.drawLeftText(this, getName() + ":", curX);
//		curX += tWidth;
		curX = getX() + getWidth() - menu.style.padX - nWidth;
		
		if (getState() == MenuItem.UP)
		{
			menu.buff.strokeWeight(0.5f);
			menu.buff.stroke(100);
			menu.buff.noFill();
			menu.buff.rect(curX, y+nOffsetY, nWidth, nHeight);
//			MenuUtils.drawBlankRect(this, curX, y+nOffsetY, nWidth, nHeight);
		} else
		{
			MenuUtils.drawSingleGradientRect(this, curX, y+nOffsetY, nWidth, nHeight,0);
		}
		/*
		 * update the "value" object using Reflection.
		 */
		try
		{
			if (useReflection)
				value = field.getBoolean(fieldObj);
		} catch (Exception e)
		{
			useReflection = false;
			e.printStackTrace();
		} finally
		{
//			MenuUtils.drawText(this, Boolean.toString(value), true, true, curX, y, nWidth,
//					height);
		}
		/*
		 * Draw the check mark, if necessary.
		 */
		if (value)
		{
			drawCheckMark();
		}
	}

	private void drawCheckMark()
	{
		float w = nWidth * .75f;
		float h = nHeight * .75f;
		float x0 = x + nOffsetX + (nWidth - w)/2f;
		float y0 = y + nOffsetY + (nHeight - h)/2f;
		menu.canvas.strokeWeight(nHeight/8f);
		menu.canvas.stroke(0);
		menu.canvas.line(x0+w*.2f, y0+h*.6f,
				x0+w*.5f, y0+h*.9f);
		menu.canvas.line(x0+w*.5f, y0+h*.9f,
				x0+w*.8f,y0+h*.2f);
	}
	
	protected void calcPreferredSize()
	{
		super.calcPreferredSize();
		/*
		 * For the height, let's use the height of some capital letters.
		 */
		float tHeight = UIUtils.getTextHeight(menu.buff, menu.style.font,
				menu.style.fontSize, "XYZ", true);
		/*
		 * Calculate the text rectangle size.
		 */
		if (getName().length() > 0)
		{
			tWidth = UIUtils.getTextWidth(menu.buff, menu.style.font,
					menu.style.fontSize, getName() + ":", true);
			tWidth += menu.style.padX;
		}

		setHeight(tHeight + 2 * menu.style.padY);
		
		nOffsetX = getWidth() - menu.style.padX - nWidth;
		nHeight = tHeight * CHECKBOX_SIZE;
		nWidth = nHeight;
		nOffsetY = (getHeight() - nHeight)/2f;

		setWidth(menu.style.padX + tWidth + nWidth + menu.style.padX);
		
	}

	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		buff.setFrame(x, y, width, height);
		Rectangle2D.union(rect, buff, rect);
		super.getRect(rect, buff);
	}

	protected void performAction()
	{
		// super.performAction();
	}

	protected void visibleMouseEvent(MouseEvent e, Point tempPt)
	{
		super.visibleMouseEvent(e, tempPt);

		if (mouseInside)
		{
			menu.setCursor(Cursor.HAND_CURSOR);
		}
		switch (e.getID())
		{
			case (MouseEvent.MOUSE_PRESSED):
				if (mouseInside)
				{
					setVal(!value);
				}
				break;
			case (MouseEvent.MOUSE_DRAGGED):

				break;
			case (MouseEvent.MOUSE_RELEASED):
				break;
		}
	}

	static RoundRectangle2D.Float buffRoundRect = new RoundRectangle2D.Float(0,
			0, 0, 0, 0, 0);

	protected boolean containsPoint(Point p)
	{
		buffRoundRect.setRoundRect(x, y, width, height, menu.style.roundOff,
				menu.style.roundOff);
//		buffRoundRect.setRoundRect(x + nOffsetX, y + nOffsetY, nWidth, nHeight,
//				menu.style.roundOff, menu.style.roundOff);
		return buffRoundRect.contains(p);
	}

}
