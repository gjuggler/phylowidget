package org.andrewberman.ui.menu;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.andrewberman.tween.PropertyTween;
import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenFriction;
import org.andrewberman.ui.EventManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.ShortcutManager;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.ifaces.Positionable;
import org.andrewberman.ui.ifaces.UIObject;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

/**
 * The <code>Menu</code> class represents a displayable, interactive menu. It
 * is abstract, so it can never be instantiated on its own. Instead, users
 * should call the constructor of one of its subclasses, such as
 * <code>Toolbar</code> or <code>Dock</code>.
 * <p>
 * The main purpose for the <code>Menu</code> class is to hold a large amount
 * of generic structure and logic for managing and displaying a menu-type object
 * within Processing. This includes: dealing with mouse events, keeping track of
 * which menu item is currently hovered, selected, and open, and creating and
 * drawing to an off-screen buffer when necessary (to allow us to use Java2D
 * functions within a P3D or OpenGL PApplet).
 * <p>
 * <p>
 * The <code>Menu</code> class is the base class for all other menu objects
 * within this package. It inherits from the <code>MenuItem</code> class,
 * because a Menu should act like a well-behaved MenuItem as well (this way, we
 * can cascade menus within other menus). It also helped keep down the amount of
 * code repetition.
 * <p>
 * The side effect of this completely inherited organization is, of course, some
 * complexity in thinking about the recursive method calls. You need to keep in
 * mind that when the <code>Menu</code> class calls
 * <code>super.someMethod()</code>, it's calling the method defined in the
 * <code>MenuItem</code> class.
 * <p>
 * There is also a slight issue with the fact that a <code>Menu</code> should
 * not be strictly considered a <code>MenuItem</code>, because more often
 * than not, the <code>Menu</code> object itself does not "act" like a menu.
 * Rather, its sub-items are what is shown to the screen and what interacts with
 * the user. See the classes that extend the <code>Menu</code> class for
 * examples of how to deal with this code organization.
 * <p>
 * <b>Important information for developers:</b> if you plan to create a new
 * type of menu using the <code>Menu</code> class as a base class, you should
 * keep in mind the following:
 * <ul>
 * <li>Check out the built-in Menu subclasses that already exist:
 * <code>Dock</code>, <code>Toolbar</code>, and <code>VerticalMenu</code>.
 * Examining how these classes interact with the <code>Menu</code> structure
 * will be very helpful when designing a novel Menu type.</li>
 * <li>Please, make use of the boolean "options" provided, and override the
 * <code>setOptions()</code> within your new class as a place to define your
 * new menu's behavioral and display options. Particularly important are
 * <code>useCameraCoordinates</code> and <code>usesJava2D</code>. See each
 * option's javadoc for more information.
 * 
 * @author Greg
 * @see org.andrewberman.ui.menu.MenuItem
 * @see org.andrewberman.ui.menu.Dock
 * @see org.andrewberman.ui.menu.Toolbar
 */
public abstract class Menu extends MenuItem implements UIObject, Positionable
{
	public static final int START_SIZE = 50;
	
	int offsetX = 0;
	int offsetY = 0;

	public StyleSet style;

	/**
	 * The current "canvas" PApplet object.
	 */
	protected PApplet canvas;
	/**
	 * The graphics2D object to which we may be drawing.
	 */
	protected PGraphicsJava2D buff;
	/**
	 * A RenderingHints object, used during the <code>draw()</code> cycle to
	 * store and reload the rendering hints on the graphics2D object being used.
	 */
	RenderingHints origRH;
	/**
	 * A Composite object, used to store and reload the Graphics2D's original
	 * composite state during the draw cycle.
	 */
	Composite origComp;
	/**
	 * Two Rectangle objects which are passed to the sub-items during the
	 * getRect() phase of the draw cycle.
	 */
	Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
	Rectangle2D.Float buffRect = new Rectangle2D.Float(0, 0, 0, 0);
	/**
	 * One Point object which will be passed to the sub-items during the
	 * mouseEvent() cycle.
	 */
	Point mousePt = new Point(0, 0);
	/**
	 * A Tween for the alpha value. Only created if <code>autoDim</code> is
	 * set to true.
	 */
	PropertyTween aTween;
	/**
	 * References to a few relevant MenuItems.
	 */
	MenuItem currentlyHovered, lastPressed, lastHovered;
	/**
	 * The current alpha value for this menu.
	 */
	public float alpha = 1.0f;
	/**
	 * Indicates whether the menu was "just" shown. Should be true only during
	 * the first <code>mouseEvent</code> cycle seen after this menu is shown.
	 */
	boolean justShown = false;
	/*
	 * =============================== GENERAL OPTIONS FOR SUBCLASSES
	 */
	/**
	 * A very important option. If set to FALSE, then this <code>Menu</code>
	 * instance will draw itself to SCREEN coordinates. If set to TRUE, however,
	 * then this <code>Menu</code> will draw itself to the MODEL coordinates.
	 * In general, this is best left to FALSE for UI objects, as they are
	 * usually drawn relative to the screen, despite what the "model" camera may
	 * be doing.
	 * <p>
	 * This option is made <code>public</code>, as opposed to most of the
	 * other <code>protected</code> options, because it is easily forseeable
	 * that the user might want to change the screen-vs-camera behavior of a
	 * menu without going through the effort of making a new subclass and
	 * overriding the <code>setOptions</code> method.
	 */
	public boolean useCameraCoordinates = true;
	/**
	 * This parameter signals that the sub-classing menu is going to draw itself
	 * using Java2D. As such, if the base canvas isn't Java2D, then we will draw
	 * to the off-screen buffer and then blend the image back onto the canvas.
	 * If this value is set to FALSE, then the <code>Menu</code> will always
	 * draw directly to the on-screen PGraphics canvas. Schweet!
	 */
	protected boolean usesJava2D = true;
	/**
	 * If true, a mouse hover will expand a menu item. If false, a menu item
	 * requires a click to expand.
	 */
	protected boolean hoverNavigable = true;
	/**
	 * If true, a click will hide a submenu as well as show it. This option
	 * works best when set to the OPPOSITE of the above hoverNavigable option.
	 */
	protected boolean clickToggles = false;
	/**
	 * If true, only one of this MenuItem's submenus will be allowed to be shown
	 * at once. Generally best left set to true.
	 */
	protected boolean singletNavigation = true;
	/**
	 * If true, this menu's items will act like a "menu" and open up on the
	 * mouse down event, as opposed to the more "button"-like behavior of
	 * opening on the mouse up event.
	 */
	protected boolean actionOnMouseDown = false;
	/**
	 * Defines the behavior of a click that occurs outside the bounds of the
	 * menu and all of its sub-menus).
	 */
	protected int clickAwayBehavior;
	public static final int CLICKAWAY_HIDES = 0;
	public static final int CLICKAWAY_COLLAPSES = 1;
	public static final int CLICKAWAY_IGNORED = 2;
	/**
	 * If true, this Menu will change to the hand cursor when one of its
	 * constituent sub-MenuItems is selected. I am the walrus.
	 */
	protected boolean useHandCursor = true;
	/**
	 * If true, this menu will hide itself when one of its component MenuItems
	 * has its action performed. If false, it will remain open.
	 */
	protected boolean hideOnAction = true;
	/**
	 * If true, this menu will dim to loAlpha if the mouse is not inside the
	 * menu or any of its sub-items.
	 */
	protected boolean autoDim = false;
	/**
	 * The "dim" alpha value to drop to. Only effective when
	 * <code>autodim</code> is true.
	 */
	public float dimAlpha = .3f;
	/**
	 * The full alpha value to jump to when the mouse is over this menu. Only
	 * effective when <code>autoDim</code> is true.
	 */
	public float fullAlpha = 1f;

	public Menu(PApplet app)
	{
		super("");
		label = this.getClass().getName();
		UIUtils.loadUISinglets(app);
		EventManager.instance.add(this); // Add ourselves to EventManager.
		canvas = app;
		setMenu(this);
		style = StyleSet.defaultStyle();
		setOptions(); // Give our subclassers a chance to set their options.

		if (UIUtils.isJava2D(canvas))
			buff = (PGraphicsJava2D) canvas.g;
		else if (usesJava2D)
			createBuffer(START_SIZE, START_SIZE);
		if (autoDim)
			aTween = new PropertyTween(this, "alpha",
					TweenFriction.tween(.25f), Tween.OUT, fullAlpha, dimAlpha,
					15);
	}

	protected void setOptions()
	{
		// Subclassers should put changes in the boolean options here.
	}

	protected void createBuffer(int w, int h)
	{
		buff = (PGraphicsJava2D) canvas.createGraphics(w, h, PApplet.JAVA2D);
	}

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
		layout(); // TODO: Should this be here? For now, I'm leaving it in.
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	/**
	 * Creates a MenuItem that this Menu can have added to it. Subclassers
	 * should implement this method to create a new top-level Menuitem, i.e.
	 * your DinnerMenu object should create and return a DinnerMenuItem that
	 * could then be inserted into your DinnerMenu using add(MenuItem).
	 * 
	 * @param label
	 *            the label of the MenuItem to be created
	 * @return a MenuItem that is compatible with the current Menu instance.
	 */
	public abstract MenuItem create(String label);

	public void show()
	{
		super.show();
		showChildren();
		justShown = true;
	}

	public void hide()
	{
		super.hide();
		hideAllChildren();
		UIUtils.releaseCursor(this, canvas);
//		new Error("hey!").printStackTrace();
	}

	public boolean isRootMenu()
	{
		return (menu == this);
	}

	public void setMenu(Menu m)
	{
		super.setMenu(m);
		if (!isRootMenu())
		{
			/*
			 * If we're no longer the root menu, then remove ourselves from the
			 * EventManager's control.
			 */
			EventManager.instance.remove(this);
		}
	}

	public void layout()
	{
		hint();
		super.layout();
		unhint();
	}

	public void draw()
	{
		if (!isVisible())
			return;
		if (!isRootMenu())
		{
			drawBefore();
			super.draw();
			drawAfter();
			return;
		}
		if (autoDim)
			aTween.update();
		if (UIUtils.isJava2D(canvas))
		{
			/*
			 * If this is a root menu and our canvas PGraphics is a
			 * JavaGraphics2D instance, then we can draw with Java2D directly to
			 * the canvas.
			 */
			canvas.pushMatrix();
			resetMatrix(canvas.g);
//			canvas.translate(x, y);
			hint();
			drawBefore();
			super.draw(); // Draw all the sub segments.
			drawAfter();
			unhint();
			canvas.popMatrix();
		} else if (!usesJava2D)
		{
			/*
			 * If this menu has indicated that it won't draw Java2D unless it
			 * checks itself for a J2D canvas, then we can also draw directly to
			 * the canvas.
			 */
			canvas.pushMatrix();
			resetMatrix(canvas.g);
//			canvas.translate(x, y);
			drawBefore();
			super.draw();
			drawAfter();
			canvas.popMatrix();
		} else
		{
			/*
			 * If our root canvas is either OpenGL or P3D, we need to draw to
			 * the offscreen Java2D buffer and then blit it onto the canvas
			 * PGraphics.
			 */
			resizeBuffer();
			hint();
			buff.beginDraw();
			buff.background(255, 0);
			buff.translate(-x,-y);
			buff.translate(-offsetX,-offsetY);
//			buff.translate(offsetX, offsetX);
			drawBefore();
			super.draw(); // Draws all of the sub segments.
			drawAfter();
			buff.modified = true;
			buff.endDraw();
			drawToCanvas();
			unhint();
		}
	}

	protected void hint()
	{
		origRH = buff.g2.getRenderingHints();
		origComp = buff.g2.getComposite();
		buff.g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		 buff.g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		 RenderingHints.VALUE_ANTIALIAS_ON);
		 buff.g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
		 RenderingHints.VALUE_STROKE_PURE);
//		 buff.g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
//		 buff.g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		// buff.g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
		// RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		buff.g2.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha));
	}

	protected void unhint()
	{
		buff.g2.setRenderingHints(origRH);
		buff.g2.setComposite(origComp);
	}

	protected void drawBefore()
	{
		// Subclasses can use this to do some of their own drawing.
	}

	protected void drawAfter()
	{
		// Subclasses can use this to do some of their own drawing.
	}

	protected void resetMatrix(PGraphics graphics)
	{
		if (useCameraCoordinates)
			return;
		UIUtils.resetMatrix(graphics);
	}

	protected void drawToCanvas()
	{
		int w = (int) (rect.width + PAD*2);
		int h = (int) (rect.height + PAD*2);
		canvas.pushMatrix();
		resetMatrix(canvas.g);
		canvas.image(buff, x+offsetX, y+offsetY, w,
				h, 0, 0, w, h);
		canvas.popMatrix();
		
		/*
		 * Draw some debug rectangles:
		 *  - Red: bounding rectangle for the menu.
		 *  - Green: size of the buffer PGraphics
		 *  - Blue: the area actually copied from the buffer to the drawing canvas.
		 */
//		canvas.noFill();
//		canvas.stroke(255,0,0);
//		canvas.rect(rect.x, rect.y, rect.width, rect.height);
//		canvas.stroke(0,255,0);
//		canvas.rect(x+offsetX,y+offsetY,buff.width,buff.height);
//		canvas.stroke(0,0,255);
//		canvas.rect(x+offsetX,y+offsetY,w,h);
	}

	static final int PAD = 10;
	
	protected void resizeBuffer()
	{
		rect.setFrame(x, y, 0, 0);
		buffRect.setFrame(x, y, 0, 0);
		getRect(rect, buffRect);
		
		float dX = 0;
		float dY = 0;
//		if (rect.x - (x+offsetX) < PAD)
			dX = rect.x - (x + offsetX + PAD);
//		if (rect.y - (y+offsetY) < PAD)
			dY = rect.y - (y + offsetY + PAD);
		
		offsetX += dX;
		offsetY += dY;
		
		int newWidth = buff.width;
		int newHeight = buff.height;
		boolean resizeMe = false;
		if (rect.width > buff.width - PAD*2)
		{
			newWidth = (int) (rect.width + PAD*2);
			resizeMe = true;
		}
		if (rect.height > buff.height - PAD*2)
		{
			newHeight = (int) (rect.height + PAD*2);
			resizeMe = true;
		}
		
		if (resizeMe)
		{
			createBuffer(newWidth,newHeight);
		}
	}

	protected boolean containsPoint(Point pt)
	{
		return false;
	}

	// protected void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
	// {
	// super.getRect(rect, buff);
	// }

	public void keyEvent(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case (KeyEvent.VK_ESCAPE):
				hide();
				break;
		}

		super.keyEvent(e);
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		if (!isVisible())
			return;
		Point useMe = model;
		if (isRootMenu() && !useCameraCoordinates)
			useMe = screen;
		/*
		 * create a copy of the point we decided to use, and translate it
		 * accordingly.
		 */
		mousePt.setLocation(useMe);
//		mousePt.translate(-x, -y);
		/*
		 * Send the mouse events through the tree of sub-items.
		 */
		itemMouseEvent(e, mousePt);
		if (!mouseInside && isVisible()
				&& e.getID() == MouseEvent.MOUSE_PRESSED)
		{
			switch (clickAwayBehavior)
			{
				case (CLICKAWAY_HIDES):
//					if (justShown)
//						justShown = false;
//					else
//					{
						hide();
//					}
					break;
				case (CLICKAWAY_COLLAPSES):
					setOpenItem(null);
					break;
				case (CLICKAWAY_IGNORED):
				default:
					break;
			}
		}

		if (useHandCursor && isVisible())
		{
			if (mouseInside)
			{
				UIUtils.setCursor(this, canvas, Cursor.HAND_CURSOR);
			} else
			{
				UIUtils.releaseCursor(this, canvas);
			}
		}
	}

	protected void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);
		if (mouseInside)
		{
//			System.out.println(this+"   is consuming!");
			e.consume();
		}
		if (autoDim)
		{
			if (mouseInside)
			{
				aTween.continueTo(fullAlpha);
			} else if (menu.currentlyHovered == null)
			{
				aTween.continueTo(dimAlpha);
			}
		}
	}

	public void focusEvent(FocusEvent e)
	{
		if (e.getID() == FocusEvent.FOCUS_LOST)
		{
			hide();
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseDragged(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	public void mouseMoved(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

}
