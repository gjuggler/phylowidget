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
package org.andrewberman.ui.menu;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;

import processing.core.PApplet;
import processing.core.PFont;

public class NumberScroller extends MenuItem
{
	static RoundRectangle2D.Float buffRoundRect = new RoundRectangle2D.Float(0, 0, 0, 0, 0, 0);
	private DecimalFormat df;
	boolean customFormat = false;
	private Field field;
	private Method method;
	private Object fieldObj;

	public boolean allowPrecision = true;

	static float NaN = Float.NaN;

	protected float min = -Float.MAX_VALUE, max = Float.MAX_VALUE;
	boolean scrolling;
	float startY, startVal;

	// private int minDigits, maxDigits;
	private String stringValue;
	private float tWidth, nWidth, nOffset;
	private boolean useReflection;
	private boolean useMethod;

	private float value, defaultValue, increment, scrollSpeed;

	public NumberScroller()
	{
		super();

		df = new DecimalFormat("#######0.0#");
		df.setDecimalSeparatorAlwaysShown(false);

		value = 0;
		defaultValue = NaN;
		increment = 1;
		scrollSpeed = 1;

		setIncrement(increment);
		setValue(value);
		setScrollSpeed(scrollSpeed);

		stringValue = new String();
	}

	public void setFormat(String f)
	{
		df = new DecimalFormat(f);
		df.setDecimalSeparatorAlwaysShown(false);

		int ind = f.indexOf(".");
		if (ind > -1)
		{
			int digCount = f.length() - ind - 1;
			df.setMinimumFractionDigits(digCount);
			df.setMaximumFractionDigits(digCount);
		}

		customFormat = true;
		updateString();
	}

	protected void calcPreferredSize()
	{
		super.calcPreferredSize();

		PFont font = menu.getStyle().getFont("font");
		float fs = menu.getStyle().getF("f.fontSize");
		float px = menu.getStyle().getF("f.padX");
		float py = menu.getStyle().getF("f.padY");

		/*
		 * For the height, let's use the height of some capital letters.
		 */
		float tHeight = UIUtils.getTextHeight(menu.buff, font, fs, "XYZ", true);
		/*
		 * Calculate the text rectangle size.
		 */
		if (getName().length() > 0)
		{
			tWidth = UIUtils.getTextWidth(menu.buff, font, fs, getName() + ":", true);
			tWidth += px;
		}

		String s = stringValue;

		/*
		 * Store the beginning point for the number area.
		 */
		nWidth = 0;
		nWidth += UIUtils.getTextWidth(menu.buff, font, fs, s, true);
		nWidth += 2 * px;

		nOffset = getWidth() - px - nWidth;

		setWidth(px + tWidth + nWidth + px);
		setHeight(tHeight + 2 * py);
	}

	protected boolean containsPoint(Point p)
	{
		if (scrolling)
			return true;
		float ro = menu.getStyle().getF("f.roundOff");
		buffRoundRect.setRoundRect(x, y, width, height, ro, ro);
		// buffRoundRect.setRoundRect(x + nOffset, y, nWidth, height,
		// menu.getStyle().roundOff, menu.getStyle().roundOff);
		return buffRoundRect.contains(p);
	}

	protected void drawMyself()
	{
		super.drawMyself();
		getValue();
		if (scrolling)
		{
			/*
			 * Cause the menu to re-layout in case we've changed preferred size.
			 */
			menu.layout();
		}

		float px = menu.getStyle().getF("f.padX");
		float py = menu.getStyle().getF("f.padY");

		float curX = x + px;
		MenuUtils.drawLeftText(this, getName() + ":", curX);
		curX += tWidth;

		curX = getX() + getWidth() - px - nWidth;
		if (shouldPerformFill())
			MenuUtils.drawSingleGradientRect(this, curX, y, nWidth, height);
		/*
		 * update the "value" object using Reflection.
		 */
		MenuUtils.drawText(this, stringValue, true, true, curX, y, nWidth, height);
	}

	protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	{
		buff.setFrame(x, y, width, height);
		Rectangle2D.union(rect, buff, rect);
		super.getRect(rect, buff);
	}

	public float getValue()
	{
		float oldValue = value;
		try
		{
			if (useReflection)
			{
				try
				{
					value = field.getFloat(fieldObj);
				} catch (Exception e)
				{
					try
					{
						value = parseValueFromString(field.get(fieldObj));
					} catch (Exception e2)
					{
						e2.printStackTrace();
						System.err.println(e.getMessage());
					}
				}
			}
		} catch (Exception e)
		{
			useReflection = false;
			e.printStackTrace();
		}
		if (value != oldValue)
			updateString();
		return value;
	}

	protected float parseValueFromString(Object s)
	{
		if (s == null || s.equals("")) return 0;
		return Float.parseFloat(s.toString());
	}

	// This is bad programming! Do as I say, not as I do!!
	// This is here so subclasses can directly get the current value, without
	// going through the reflection nonsense. Maybe I could get rid of it?
	protected float getValueDirectly()
	{
		return value;
	}

	public void performAction()
	{
		// super.performAction();
	}

	public void setDefault(float def)
	{
		defaultValue = def;
		setValue(defaultValue);
	}

	public void setIncrement(float inc)
	{
		increment = inc;
		/*
		 * Try and auto-detect number of decimal places from the increment.
		 */
		int numDecimals = (int) Math.ceil((float) -Math.log10(increment));
		// System.out.println(Math.log10(increment)+ " "+getName() + " " +
		// increment + " " + numDecimals);
		if (!customFormat)
		{
			df.setMinimumFractionDigits(numDecimals);
			df.setMaximumFractionDigits(numDecimals);
		}

		setValue(getValue());
	}

	public void setMax(float max)
	{
		this.max = max;
	}

	public void setMin(float min)
	{
		this.min = min;
	}

	public void setProperty(Object obj, String prop)
	{
		try
		{
			String setProp = "set" + MenuIO.upperFirst(prop);
			method = obj.getClass().getMethod(setProp, Float.TYPE);
			useMethod = true;
		} catch (Exception e)
		{
			useMethod = false;
		}
		try
		{
			field = obj.getClass().getField(prop);
			fieldObj = obj;
			useReflection = true;
			//			System.out.println(field.getFloat(fieldObj));
		} catch (Exception e)
		{
			//			e.printStackTrace();
			field = null;
			fieldObj = null;
			useReflection = false;
			throw new RuntimeException();
		}
		if (Float.isNaN(defaultValue))
		{
			setDefault(getValue());
		}
		if (useReflection)
		{
			try
			{
				field.setFloat(fieldObj, getValue());
			} catch (Exception e)
			{
				// Try setting the String value.
				String s = getStringValueForNumber(value);
				try
				{
					field.set(fieldObj, s);
				} catch (Exception e2)
				{
					e.printStackTrace();
					e2.printStackTrace();
				}
			}
		} else
		{
			setValue(defaultValue);
		}
	}

	public void setScrollSpeed(float changePerPixel)
	{
		scrollSpeed = changePerPixel;
	}

	public void setValue(float val)
	{
		float oldValue = value;
		value = PApplet.constrain(val, min, max);
		if (useReflection)
		{
			if (useMethod)
			{
				try
				{
					method.invoke(fieldObj, value);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			} else
			{
				try
				{
					field.setFloat(fieldObj, value);
				} catch (Exception e)
				{
					// Try setting the String value.
					String s = getStringValueForNumber(value);
					try
					{
						field.set(fieldObj, s);
					} catch (Exception e2)
					{
						e.printStackTrace();
						e2.printStackTrace();
					}

				}
			}
		}
		updateString();
	}

	void updateString()
	{
		stringValue = getStringValueForNumber(value);
	}

	protected String getStringValueForNumber(float value)
	{
		return df.format(value);
	}

	private boolean controlDown = false;

	protected void visibleMouseEvent(MouseEvent e, Point tempPt)
	{
		super.visibleMouseEvent(e, tempPt);
		if (!isEnabled())
			return;

		float curInterval = increment;

		if (e.isControlDown() != controlDown)
		{
			controlDown = e.isControlDown();
			startY = tempPt.y;
			startVal = getValue();
		}
		if (e.isControlDown())
		{
			curInterval /= 5f;
		}

		if (mouseInside)
		{
			menu.setCursor(Cursor.N_RESIZE_CURSOR);
		}
		switch (e.getID())
		{
			case (MouseEvent.MOUSE_PRESSED):
				if (mouseInside)
				{
					if (e.getClickCount() > 1)
					{
						setValue(defaultValue);
					}
					startY = tempPt.y;
					startVal = getValue();
					scrolling = true;
					nearestMenu.context.focus().setModalFocus(this.menu);
				}
				break;
			case (MouseEvent.MOUSE_DRAGGED):
				if (scrolling)
				{
					float dy = startY - tempPt.y;
					float dVal = dy * curInterval * scrollSpeed;
					value = startVal + dVal;
					setValue(value);
					e.consume();
				}
				break;
			case (MouseEvent.MOUSE_RELEASED):
				if (scrolling)
				{
					e.consume();
					scrolling = false;
					nearestMenu.context.focus().removeFromFocus(this.menu);
				}
				break;
		}
	}

	public boolean isAllowPrecision()
	{
		return allowPrecision;
	}

	public void setAllowPrecision(String allowPrecision)
	{
		if (allowPrecision.equalsIgnoreCase("true") || allowPrecision.toLowerCase().startsWith("y"))
			this.allowPrecision = true;
		else
			this.allowPrecision = false;
	}

}
