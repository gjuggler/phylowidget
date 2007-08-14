package org.andrewberman.ui.menu;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIUtils;
import org.phylowidget.ui.FontLoader;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * The <code>Dock</code> class is a close approximation of Apple's infamous
 * Dock menubar.
 * <p>
 * It is built on top of the <code>Menu</code> superclass, except unlike most
 * other <code>Menu</code> derivatives, the <code>Dock</code> does not rely
 * on Java2D rendering (although it makes use of it if available). As such, it
 * can be drawn directly to the canvas, without requiring the processor and
 * memory overhead of creating and drawing to an off-screen buffer. Thus, it
 * should be snappy under P3D and OpenGL renderers.
 * <p>
 * By default, the <code>Dock</code> "docks" and centers itself along one side
 * of the screen. You can alter this behavior by
 * 
 * @author Greg
 */
public class Dock extends Menu
{
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	DockRotationHandler rotation;

	RoundRectangle2D.Float mouseRect, drawRect;
	Point mousePt;
	float origWidth, inset, offset, maxPossibleWidth;
	float curWidth, curHeight, curLow;
	boolean isActivated;

	/**
	 * The amount by which the icons "bulge" when approached.
	 */
	public float bulgeAmount = .75f;
	/**
	 * The "rolloff" factor for the icons' bulge. Play around with it to find a
	 * value that you like.
	 */
	public float bulgeWidth = 50.0f;

	/**
	 * If set to true, then this Dock will automatically center itself to the
	 * side against which it's docked. Offset will offset it in the positive x
	 * or y direction away from the center. If false, then the offset will cause
	 * the dock to be offset from the corner by that amount.
	 * <p>
	 * Note that in the <code>false</code> case, the offset calculated will be
	 * based on the <em>resting</em> size of the <code>Dock</code> When
	 * items become bulged, the dock will likely extend a bit out in both
	 * directions, so you'd best give it a little extra <code>offset</code>
	 * just to be safe!
	 */
	public boolean autoCenter = true;

	/**
	 * If set to true, then a triangle will be drawn on the last clicked item.
	 * If false, no triangles. Simple!
	 */
	public boolean triangleOnSelected = true;

	public Dock(PApplet app)
	{
		super(app);
		origWidth = 40;
		inset = style.margin;
		maxPossibleWidth = origWidth * (1 + bulgeAmount);

		drawRect = new RoundRectangle2D.Float(0, 0, 0, 0, 0, 0);
		mouseRect = new RoundRectangle2D.Float(0, 0, 0, 0, 0, 0);
		mousePt = new Point(0, 0);
		rotation = new DockRotationHandler();
		rotation.setRotation(LEFT);

		layout();
		show();
	}

	protected void setOptions()
	{
		useCameraCoordinates = false;
		clickAwayBehavior = CLICKAWAY_COLLAPSES;
		hoverNavigable = false;
		clickToggles = true;
		useHandCursor = true;
		actionOnMouseDown = true;
		hideOnAction = true;
		usesJava2D = false;
		autoDim = true;
	}

	public void layout()
	{
		mousePt.setLocation(canvas.mouseX,canvas.mouseY);
		if (useCameraCoordinates)
			UIUtils.screenToModel(mousePt);
		float mousePos = mousePos();
		float origHeight = origWidth * items.size();
		float origCenter = rotation.getCenter();
		float origLow = origCenter - origHeight / 2;
		float mouseOffset = mousePos - origLow;

		float newOffset = 0;
		float pos = origLow;
		for (int i = 0; i < items.size(); i++)
		{
			float mid = pos + origWidth / 2;
			float scale = bulge(mid - mousePos);
			MenuItem item = (MenuItem) items.get(i);
			newOffset += item.getHeight();
			if (isActivated)
			{
				item.setSize(origWidth * scale, origWidth * scale);
			} else
				item.setSize(origWidth, origWidth);
			pos += origWidth;
		}
		curLow = origLow - (newOffset - origHeight)
				* (mouseOffset / origHeight);

		pos = curLow;
		float maxWidth = 0;
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = (MenuItem) items.get(i);
			// item.setPosition(inset, pos);
			rotation.positionItem(item, pos);
			pos += item.getHeight();
			if (item.getWidth() > maxWidth)
				maxWidth = item.getWidth();
		}
		curHeight = pos - curLow;
		curWidth = maxWidth;
	}

	public void hide()
	{
		// Docks never hide.
	}
	
	float mousePos()
	{
		return rotation.getMousePos(mousePt);
	}

	float bulge(float b)
	{
		return 1.0f + bulgeAmount
				* PApplet.exp(-b * b / (bulgeWidth * bulgeWidth));
	}

	public void setOffsetFromCenter(float offset)
	{
		this.offset = offset;
		layout();
	}

	public void setWidth(float newWidth)
	{
		origWidth = newWidth;
		layout();
	}
	
	public void setInset(float inset)
	{
		this.inset = inset;
		layout();
	}

	public void setRotation(int rot)
	{
		rotation.setRotation(rot);
		layout();
	}

	public DockItem getSelectedItem()
	{
		if (lastPressed == null)
			return null;
		else
			return (DockItem)lastPressed;
	}
	
	public void drawBefore()
	{
		layout();
		rotation.setRect(drawRect, origWidth);
		/*
		 * Draw a nice-looking background gradient.
		 */
		if (UIUtils.isJava2D(canvas))
		{
			Graphics2D g2 = menu.buff.g2;
			g2.setPaint(menu.style.getGradient((float) drawRect.getMinY(),
					(float) drawRect.getMaxY()));
			g2.fill(drawRect);
			g2.setStroke(menu.style.stroke);
			g2.setPaint(menu.style.strokeColor);
			RenderingHints rh = g2.getRenderingHints();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.draw(drawRect);
			g2.setRenderingHints(rh);
		} else
		{
			int alpha = (int) (menu.alpha * 255);
			int color;
			Color c;
			canvas.beginShape(PApplet.QUADS);
			canvas.stroke(canvas.color(style.strokeColor.getRGB(), alpha));
			c = style.menuGradLo;
			color = canvas.color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
			canvas.fill(color);
			canvas.vertex((float) drawRect.getMinX(), (float) drawRect
					.getMinY());
			canvas.vertex((float) drawRect.getMaxX(), (float) drawRect
					.getMinY());
			c = style.menuGradHi;
			color = canvas.color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
			canvas.fill(color);
			canvas.vertex((float) drawRect.getMaxX(), (float) drawRect
					.getMaxY());
			canvas.vertex((float) drawRect.getMinX(), (float) drawRect
					.getMaxY());
			canvas.endShape();
		}

		if (currentlyHovered != null)
		{
			MenuItem i = currentlyHovered;
			float ascent = UIUtils.getTextAscent(menu.canvas.g,
					menu.style.font, 24, false);
			float descent = UIUtils.getTextAscent(menu.canvas.g,
					menu.style.font, 24, false);
			float tHeight = (ascent + descent);
			float tWidth = UIUtils.getTextWidth(menu.canvas.g, menu.style.font,
					24, i.label, false);

			float tX = 0;
			float tY = 0;
			switch (rotation.rot)
			{
				case (LEFT):
					tX = inset + maxPossibleWidth + menu.style.padX;
					tY = i.getY() + i.getHeight() / 2 + tHeight / 2 - descent
							/ 2;
					break;
				case (RIGHT):
					tX = menu.canvas.width - inset - maxPossibleWidth
							- menu.style.padX - tWidth;
					tY = i.getY() + i.getHeight() / 2 + tHeight / 2 - descent
							/ 2;
					break;
				case (TOP):
					menu.canvas.textAlign(PApplet.CENTER);
					tX = i.getX() + i.getWidth() / 2;
					tY = inset + maxPossibleWidth + menu.style.padX + tHeight
							- descent;
					break;
				case (BOTTOM):
					menu.canvas.textAlign(PApplet.CENTER);
					tX = i.getX() + i.getWidth() / 2;
					tY = menu.canvas.width - inset - maxPossibleWidth
							- menu.style.padX;
					break;
			}

			Color c = menu.style.textColor;
			int alpha = (int) (menu.alpha * 255);
			menu.canvas.fill(menu.canvas.color(c.getRed(), c.getGreen(), c
					.getBlue(), alpha));
			// menu.canvas.fill(0,alpha);
			menu.canvas.textFont(FontLoader.vera);
			menu.canvas.textSize(24);
			menu.canvas.text(i.label, tX, tY);
			menu.canvas.textAlign(PApplet.LEFT);
		}

		if (lastPressed != null && triangleOnSelected)
		{
			MenuItem i = lastPressed;
			PGraphics pg = canvas.g;
			int alpha = (int) (menu.alpha * 255);
			Color c = menu.style.strokeColor;
			pg.fill(menu.canvas.color(c.getRed(), c.getGreen(), c.getBlue(),
					alpha));

			float height = i.getWidth() / 8;
			switch (rotation.rot)
			{
				case (LEFT):
					float cy = i.getY() + i.getHeight() / 2;
					float cx = inset + menu.style.margin;
					pg.triangle(cx, cy + height, cx, cy - height, cx + height,
							cy);
					i.setPosition(i.getX() + height, i.getY());
					break;
				case (RIGHT):
					cy = i.getY() + i.getHeight() / 2;
					cx = canvas.width - inset - menu.style.margin;
					pg.triangle(cx, cy + height, cx, cy - height, cx - height,
							cy);
					i.setPosition(i.getX() - height, i.getY());
					break;
				case (TOP):
					cy = inset + menu.style.margin;
					cx = i.getX() + i.getWidth() / 2;
					pg.triangle(cx + height, cy, cx - height, cy, cx, cy
							+ height);
					i.setPosition(i.getX(), i.getY() + height);
					break;
				case (BOTTOM):
					cy = canvas.width - inset - menu.style.margin;
					cx = i.getX() + i.getWidth() / 2;
					pg.triangle(cx + height, cy, cx - height, cy, cx, cy
							- height);
					i.setPosition(i.getX(), i.getY() - height);
					break;
			}
		}

	}

	public void focusEvent(FocusEvent e)
	{
		if (e.getID() == FocusEvent.FOCUS_LOST)
		{
			isActivated = false;
			layout();
		}
	}

	public void keyEvent(KeyEvent e)
	{
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (useCameraCoordinates)
			mousePt.setLocation(model);
		else
			mousePt.setLocation(screen);
		if (e.getID() == MouseEvent.MOUSE_MOVED
				|| e.getID() == MouseEvent.MOUSE_DRAGGED)
		{
			if (containsPoint(mousePt))
			{
				isActivated = true;
				FocusManager.instance.setModalFocus(this);
			} else
			{
				isActivated = false;
				FocusManager.instance.removeFromFocus(this);
			}
		}
		super.mouseEvent(e, screen, model);
		// layout();
	}

	public boolean containsPoint(Point pt)
	{
		rotation.setRect(mouseRect, curWidth);
		return mouseRect.contains(pt);
	}

	public float getX()
	{
		return 0;
	}

	public float getY()
	{
		return 0;
	}

	public void setPosition(float inset, float offset)
	{
		this.inset = inset;
		this.offset = offset;
		layout();
	}

	public MenuItem create(String label)
	{
		return new DockItem(label);
	}

	public DockItem add(String label, String iconFile)
	{
		DockItem addMe = new DockItem(label);
		add(addMe);
		addMe.setFile(iconFile);
		show();
		return addMe;
	}
	
	class DockRotationHandler
	{
		int rot = LEFT;

		void setRotation(int i)
		{
			rot = i;
			layout();
		}

		boolean isHorizontal()
		{
			return (rot == TOP || rot == BOTTOM);
		}

		boolean isVertical()
		{
			return (rot == LEFT || rot == RIGHT);
		}

		float getMousePos(Point pt)
		{
			switch (rot)
			{
				case (TOP):
				case (BOTTOM):
					return pt.x;
				case (LEFT):
				case (RIGHT):
				default:
					return pt.y;
			}
		}

		float getCenter()
		{
			if (!autoCenter)
				return offset + origWidth * items.size() / 2;
			else
			{
				switch (rot)
				{
					case (TOP):
					case (BOTTOM):
						return canvas.width / 2 + offset;
					case (LEFT):
					case (RIGHT):
					default:
						return canvas.height / 2 + offset;
				}
			}
		}

		void setRect(RoundRectangle2D.Float rect, float width)
		{
			float r = origWidth / 4;
			rect.setRoundRect(inset, curLow, width, curHeight, r, r);
			switch (rot)
			{
				case (LEFT):
					rect.setRoundRect(inset, curLow, width, curHeight, r, r);
					break;
				case (RIGHT):
					rect.setRoundRect(canvas.width - inset - width, curLow,
							width, curHeight, r, r);
					break;
				case (TOP):
					rect.setRoundRect(curLow, inset, curHeight, width, r, r);
					break;
				case (BOTTOM):
					rect.setRoundRect(curLow, canvas.height - inset - width,
							curHeight, width, r, r);
					break;
			}
		}

		void positionItem(MenuItem item, float pos)
		{
			DockItem d = (DockItem) item;
			PGraphics pg = canvas.g;
			switch (rot)
			{
				case (LEFT):
					d.layoutRule = MenuItem.LAYOUT_RIGHT;
					d.setPosition(inset, pos);
					break;
				case (RIGHT):
					d.layoutRule = MenuItem.LAYOUT_LEFT;
					d.setPosition(pg.width - item.getWidth() - inset, pos);
					break;
				case (TOP):
					d.layoutRule = MenuItem.LAYOUT_BELOW;
					d.setPosition(pos, inset);
					break;
				case (BOTTOM):
					d.layoutRule = MenuItem.LAYOUT_ABOVE;
					d.setPosition(pos, pg.height - item.getHeight() - inset);
					break;
			}
		}
	}
}
