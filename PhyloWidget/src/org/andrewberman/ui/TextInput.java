package org.andrewberman.ui;

import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.andrewberman.ui.Blinker.BlinkListener;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphicsJava2D;

/**
 * A Processing-based text input field.
 * 
 * A text input field programmed from "the ground up," using only the basic mouse and keyboard events to create a text
 * input field. Supports cut, copy, paste, and all the usual keyboard shortcuts. The text can also be selected and
 * scrolled with the mouse.
 * 
 * This object currently only works with the Java2D renderer. It uses some funky offscreen buffer drawing to get things
 * looking right, and the P3D and OpenGL renderers have some nasty bugs when drawing offscreen.
 * 
 * @author Greg
 * 
 */
public class TextInput implements UIObject, BlinkListener, MouseListener, KeyListener, MouseMotionListener
{
	protected PApplet p;

	// Model.
	public StringBuffer text = new StringBuffer();

	// View.
	protected static PFont font;
	protected float x = 0; // X coordinate of lower left-hand corner.
	protected float y = 0; // Y coordinate of lower left-hand corner.
	protected float w = 0; // width of the box.
	protected float h = 0; // Height of the box. Undefined until setFont() is called.
	protected float tw = 0; // width of the text area (w - 2*PADDING).
	protected float th = 0; // height of the text area (h - 2*PADDING).
	protected float textWidth = 0;
	protected float textOffset = 0;
	protected float ascent = 0;
	protected float descent = 0;
	protected int fontSize;
	protected int viewLo = 0; // character index of the low end of the viewport.
	protected int viewHi = 0;
	protected Blinker blinker;
	protected int color;
	protected boolean textNeedsRedraw = false;
	private static final float CARET_MULT = .9f;
	private static final float PADDING = 5.0f;
	
	// Controller.
	protected int selLo = 0; // Selection length. Positive or negative, relative to selAnchorPos.
	protected int selHi = 0;
	protected int selAnchor = 0; // Selection anchor.
	protected int anchorPos = 0; // View anchor.
	protected boolean anchorRight = true; // True if view anchoring to the right, false if to the left.
	protected int caret = 0; // char index of the caret.
	protected int caretJump = 5; // Default the CARET_JUMP to 5 characters.
	protected boolean mouseDragging = false;
	protected float mouseDragPos = 0;
	protected float mouseDragCounter = 0;
	protected final static int MOUSE_DRAG_DELAY = 3;
	private static final int SELECT = 0;
	private static final int MOVE = 1;
	private Point2D.Float pt = new Point2D.Float(0, 0);

	// Clipboard.
	protected static StringClipboard clip = StringClipboard.instance;

	protected AffineTransform a;
	protected PGraphicsJava2D p2;

	public TextInput(PApplet applet, int size, float x, float y, float w)
	{
		p = applet;
		p.registerDraw(this);
		
		p.addMouseListener(this);
		p.addMouseMotionListener(this);
		p.addKeyListener(this);
		
		// View variables.
		this.x = x;
		this.y = y;
		this.w = w;
		this.color = p.color(0, 0, 0);
		
		// Default font to Times New Roman.
		setFont("TimesNewRoman-64.vlw", size);

		p2 = (PGraphicsJava2D) p.createGraphics((int)(w * 1.5), (int)th+1,
				PApplet.JAVA2D);
		p2.hint(PConstants.ENABLE_NATIVE_FONTS);
		p2.g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		p2.smooth();
		
		// Create a caret blinker.
		blinker = Blinker.instance;
		blinker.addBlinkListener(this);
	}

	private static final int DRAG_BORDER_FACTOR = 10;

	public void draw()
	{
		drawBorder();

		if (textNeedsRedraw)
		{
			drawText();
			textNeedsRedraw = false;
		}
		
		// Copy the offscreen text buffer into the current applet's viewport.
		// Vertically align the text so there is one descent of space along the top.
		p.image( p2.get((int)textOffset, 0, (int)tw+1,(int)h+1),
				x+PADDING, y - h + PADDING);

		drawCaret();
		
		// Handle the motions if the mouse drags near the edge of the textbox.
		if (mouseDragging)
		{
			mouseDragCounter--;
			if (mouseDragCounter <= 0)
			{
				mouseDragCounter = MOUSE_DRAG_DELAY;
				if (mouseDragPos <
						x + (w / DRAG_BORDER_FACTOR / 2.0))
				{
					this.selectChar(-1);
				} else if (mouseDragPos > 
						x + w - (w / DRAG_BORDER_FACTOR / 2.0))
				{
					this.selectChar(1);
				} else if (mouseDragPos <
						x + (w / DRAG_BORDER_FACTOR))
				{
					this.selectChar(-2);
					mouseDragCounter += MOUSE_DRAG_DELAY;
				} else if (mouseDragPos >
						x + w - (w / DRAG_BORDER_FACTOR))
				{
					this.selectChar(2);
					mouseDragCounter += MOUSE_DRAG_DELAY;
				}
			}
		}
	}

	public void drawText()
	{
		calculateWindow();
		p2.beginDraw();
		p2.background(255,255);
		p2.fill(color);
		p2.textFont(font, fontSize);
//		p2.textMode(PApplet.MODEL);
		p2.textAlign(PApplet.LEFT);
		p2.text(text.substring(viewLo, viewHi), 0, th - descent);
		if (selHi - selLo > 0)
		{
			// Draw the selection.
			p2.fill(0, 0, 255);
			p2.stroke(0, 0, 255);
			p2.rectMode(PApplet.CORNER);
			p2.rect(getPosForIndex(selLo) + textOffset, 0,
					getWidth(selLo,selHi), th);
			// Draw the white selected text.
			p2.fill(255);
			p2.textFont(font,fontSize);
//			p2.textMode(PApplet.MODEL);
			p2.textAlign(PApplet.LEFT);
			int min = Math.max(viewLo, selLo);
			int max = Math.min(viewHi, selHi);
			float offset = getWidth(viewLo, min);
			p2.text(text.substring(min, max), offset, th - descent);
		} else if (blinker.isOn)
		{
			// Draw the caret.
//			p2.stroke(color);
//			p2.strokeWeight(1);
//			p2.line(getPosForIndex(caret) + textOffset, 0,
//					getPosForIndex(caret) + textOffset, th);
		}
		p2.modified = true;
		p2.endDraw();
	}

	public void onBlink(Blinker b)
	{
		textNeedsRedraw = true;
	}

	public void drawCaret()
	{
		if (!blinker.isOn //|| !PhyloWidget.focus.isFocused(this)
				|| selHi - selLo != 0)
			return;
		p.stroke(color);
		p.strokeWeight(1);
		float caretOffset = 0;
		caretOffset = getPosForIndex(caret) + PADDING;
//		caretOffset += (caretOffset > 0 ? -1 : 2);
		float yOffset = (h - (CARET_MULT * h)) / 2.0f;
		p.line(x + caretOffset, y - h + yOffset, x + caretOffset, y - h
				+ yOffset + h * CARET_MULT);
	}

	// Returns the coordinate, relative to "x", of the character's start point.
	public float getPosForIndex(int i)
	{
		// if (i < viewLo || i > viewHi) return -1;
		if (anchorRight)
			return tw - getWidth(i, viewHi);
		else
			return getWidth(viewLo, i);
	}

//	public void drawSelection()
//	{
//		if (selHi - selLo == 0)
//			return;
//		p.fill(20, 120, 255);
//		p.stroke(20, 120, 255);
//		p.rectMode(PApplet.CORNER);
//
//		int min = Math.max(selLo, viewLo);
//		int max = Math.min(selHi, viewHi);
//
//		float preOffset = Math.max(0, getPosForIndex(min));
//		float widthOffset = Math.min(w - preOffset, getWidth(min, max));
//
//		float fullHeight = ascent + descent * 2;
//		float yInset = (fullHeight - CARET_MULT * fullHeight) / 2.0f;
//
//		p.rect(x + preOffset, y - ascent - descent + yInset, widthOffset,
//				fullHeight * CARET_MULT);
//	}

	public void drawBorder()
	{
		p.noFill();
		p.stroke(color);
		p.strokeWeight(1);
		p.rectMode(PApplet.CORNER);
		p.rect(x, y - h, w, h);
	}

	public void calculateWindow()
	{
		textWidth = 0;
		float charWidth = 0;
		if (anchorRight)
		{
			viewHi = anchorPos;
			int i = viewHi;
			while (textWidth <= tw && i > 0)
			{
				charWidth = font.width(text.charAt(i - 1)) * fontSize;
				textWidth += charWidth;
				i--;
			}
			viewLo = i;
			textOffset = textWidth - tw;
		} else
		{
			viewLo = anchorPos;
			int i = viewLo;
			while (textWidth <= tw && i < text.length())
			{
				charWidth = font.width(text.charAt(i)) * fontSize;
				textWidth += charWidth;
				i++;
			}
			viewHi = i;
			textOffset = 0;
		}
	}

	private float getWidth(int lo, int hi)
	{
		if (lo > hi) // Switch lo and hi if they're reversed.
		{
			int temp = lo;
			lo = hi;
			hi = temp;
		}
		float tempWidth = 0;
		for (int i = lo; i < hi; i++)
		{
			tempWidth += font.width(text.charAt(i)) * fontSize;
		}
		return tempWidth;
	}

	public void selectAll()
	{
		selAnchor = 0;
		selectChar(text.length());
	}

	public void selectWord(int n)
	{
		nextWord(SELECT, n);
	}

	public void moveWord(int n)
	{
		nextWord(MOVE, n);
	}



	public void nextWord(int type, int n)
	{
		String s = "";
		if (n > 0)
		{
			s = text.substring(caret, text.length());
			String[] words = s.split("\\s\\S", 2);
			String firstWord = words[0];
			if (type == SELECT)
				selectChar(firstWord.length() + 1);
			else
				moveChar(firstWord.length() + 1);
		} else
		{
			s = text.reverse().substring(text.length() - caret, text.length());
			text.reverse(); // Reverse the stringbuffer back to normal!
			String[] words = s.split("\\S\\s", 2);
			String firstWord = words[0];
			if (type == SELECT)
				selectChar(-1 * (firstWord.length() + 1));
			else
				moveChar(-1 * (firstWord.length() + 1));
		}
	}

	// Lowest-level method for selection moving.
	public void selectChar(int dist)
	{
		selLo = Math.min(caret + dist, selAnchor);
		selHi = Math.max(caret + dist, selAnchor);
		if (selLo < 0)
			selLo = 0;
		if (selHi > text.length())
			selHi = text.length();
		// System.out.println("Lo: "+selLo+" Hi:"+selHi);
		moveCaretTo(caret + dist);
	}

	// Lowest-level method for caret moving.
	public void moveChar(int dist)
	{
		if (caret + dist > text.length())
			dist = text.length() - caret;
		if (caret + dist < 0)
			dist = 0 - caret;
		selAnchor = caret + dist;
		selHi = selLo = selAnchor;
		moveCaretTo(caret + dist);
	}

	public void moveCaretTo(int index)
	{
		caret = index;

		if (caret > text.length())
			caret = text.length();
		else if (caret < 0)
			caret = 0;

		if (getWidth(0, text.length()) <= w)
		{
			anchorRight = false;
			anchorPos = 0;
		} else if (viewLo + 1 > caret)
		{
			anchorPos = caret - caretJump;
			anchorRight = false;
		} else if (viewHi - 1 < caret)
		{
			anchorPos = caret + caretJump;
			anchorRight = true;
		}

		if (anchorPos > text.length())
			anchorPos = text.length();
		else if (anchorPos < 0)
			anchorPos = 0;

		blinker.reset();
		textNeedsRedraw = true;
	}

	public void insert(String s, int pos)
	{
		text.insert(pos, s);
		moveChar(s.length());
		// textChanged();
	}

	public void insertCharAt(char c, int pos)
	{
		text.insert(pos, c);
		moveChar(1);
	}

	public void backspaceAt(int pos)
	{
		// Remove the char that exists before index pos.
		if (pos <= 0)
			return;
		text.deleteCharAt(pos - 1);
		moveChar(-1);
	}

	public void deleteAt(int pos)
	{
		// Remove the char that exists at index pos.
		if (pos < 0 || pos >= text.length())
			return;
		text.deleteCharAt(pos);
		moveChar(0);
	}

	public void deleteSelection()
	{
		text.delete(selLo, selHi);
		selHi = selAnchor = caret = selLo; // Need to resolve selection deletion before calling moveCaretTo.
		moveCaretTo(selLo);
	}

	public void cut()
	{
		String s = text.substring(selLo, selHi);
		clip.toClipboard(s);
		deleteSelection();
	}

	public void copy()
	{
		clip.toClipboard(text.substring(selLo, selHi));
	}

	public void paste()
	{
		if (selHi - selLo > 0)
		{
			deleteSelection();
		}
		String s = clip.fromClipboard();
		insert(s, caret);
	}

	public String getText()
	{
		return text.toString();
	}


	public void mouseEvent(MouseEvent e)
	{
//		if (PhyloWidget.focus.isModal() && !PhyloWidget.focus.isFocused(this)) return;
		if (e.getID() != MouseEvent.MOUSE_DRAGGED)
		{
			mouseDragging = false;
		}
		if (e.getID() == MouseEvent.MOUSE_MOVED
				|| e.getID() == MouseEvent.MOUSE_RELEASED
				|| e.getID() == MouseEvent.MOUSE_ENTERED)
			return;
		if (e.isPopupTrigger())
			return;
		
		pt.x = e.getX();
		pt.y = e.getY();
		ProcessingUtils.screenToModel(pt);
		
		if (this.containsPoint(pt.x,pt.y,true)) // contained within text+padding area.
		{
			if (this.containsPoint(pt.x, pt.y,false)) // contained within text area.
			{
//				PhyloWidget.focus.setFocus(this);
				// Find the correct insertion point.
				int insertionIndex = viewLo;
				float ult = getPosForIndex(viewLo);
				float penult = ult;
				for (int i = viewLo; i <= viewHi; i++)
				{
					float pos = x + PADDING + getPosForIndex(i); // get the left edge of the "i"th character.
					insertionIndex = i;
					penult = ult;
					ult = pos;
					if (pos > pt.x)
					{
						break; // If the left edge is past our point, bail out.
					}
				}
				float middle = (ult + penult) / 2;
				if (pt.x < middle)
					insertionIndex--;
				int diff = insertionIndex - caret;
				if (e.getID() == MouseEvent.MOUSE_DRAGGED)
				{
					mouseDragging = true;
//					PhyloWidget.focus.setModalFocus(this);
					mouseDragPos = pt.x;
					// if (insertionIndex <= viewLo || insertionIndex >= viewHi)
					if (pt.x < x + w / DRAG_BORDER_FACTOR
							|| pt.x > y + w - w / DRAG_BORDER_FACTOR)
						return; // This case is handled within the draw() function.
					selectChar(diff);
				} else
				{
					moveChar(diff);
				}
			}
		} else if (e.getID() == MouseEvent.MOUSE_PRESSED)
		{
			// Point is not contained within this box, so unset focus if necessary.
//			if (PhyloWidget.focus.removeFromFocus(this))
//			{
//				selHi = selLo = selAnchor = caret; // Remove the selection when focus goes away.
//				textNeedsRedraw = true;
//			}
		}
	}
	
	public boolean containsPoint(float px, float py, boolean includePadding)
	{
		float myPad = PADDING;
		if (includePadding) myPad = 0;
		if (px < x + myPad || px > x + w - myPad)
			return false;
		if (py < y - h + myPad || py > y - myPad)
			return false;
//		System.out.println("inside! x: "+px+"   y: "+py);
		return true;
	}

	private static final int LEFT = -1;
	private static final int RIGHT = 1;

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
		calculateWindow();
//		textNeedsRedraw = true;
	}
	
	public void keyEvent(KeyEvent e)
	{
//		if (!PhyloWidget.focus.isFocused(this))
//			return;
		int code = e.getKeyCode();
//		System.out.println(KeyEvent.getKeyText(code)+ " " + code);
		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		if (e.getID() == KeyEvent.KEY_PRESSED)
		{
			switch (code)
			{
				case (37): // Left
					if (shift && ctrl)
						selectWord(LEFT);
					else if (shift)
						selectChar(LEFT);
					else if (ctrl)
						moveWord(LEFT);
					else
						moveChar(LEFT);
					break;
				case (39): // Right
					if (shift && ctrl)
						selectWord(RIGHT);
					else if (shift)
						selectChar(RIGHT);
					else if (ctrl)
						moveWord(RIGHT);
					else
						moveChar(RIGHT);
					break;
				case (8): // Backspace
					if (selHi - selLo > 0)
						deleteSelection();
					else
						backspaceAt(caret);
					break;
				case (127): // Delete
					if (selHi - selLo > 0)
						deleteSelection();
					else
						deleteAt(caret);
					break;
				case (36): // Home
					if (shift)
						selectChar(-caret);
					else
						moveChar(-caret);
					break;
				case (35): // End
					if (shift)
						selectChar(text.length() - caret);
					else
						moveChar(text.length() - caret);
					break;
				case (16): // Shift
				case (17): // Control
				case (18): // Alt
				case (9): // Tab
					// Do nothing.
					break;
				case (88): // X
					if (ctrl)
						cut();
					else
						insertCharAt(e.getKeyChar(), caret);
					break;
				case (67): // C
					if (ctrl)
						copy();
					else
						insertCharAt(e.getKeyChar(), caret);
					break;
				case (86): // V
					if (ctrl)
						paste();
					else
						insertCharAt(e.getKeyChar(), caret);
					break;
				case (65): // A
					if (ctrl)
						selectAll();
					else
						insertCharAt(e.getKeyChar(), caret);
					break;
				default:
					if (!e.isActionKey() && !ctrl)
					{
						System.err.println(KeyEvent.getKeyText(code) + "  "
								+ code);
						char c = e.getKeyChar();
						insertCharAt(c, caret);
					}
					break;
			}
			// printState();
		}
	}

	public void setFont(String fontName, int fontSize)
	{
		if (font == null)
			font = p.loadFont("TimesNewRoman-64.vlw");
		this.fontSize = fontSize;

		// Calculate the caret jump based on the ratio between text and inputbox width.
		float approxNumChars = (float) w / (font.width('o') * fontSize);
		caretJump = (int) (approxNumChars / 4);

		// Calculate various view variables.
		ascent = font.ascent() * fontSize;
		descent = font.descent() * fontSize;
		h = ascent + 2*descent + 2*PADDING;
		tw = w - 2*PADDING;
		th = h - 2*PADDING;
	}

	public synchronized void printState()
	{
		System.err.println("Text: " + text.toString());
		System.err.println("Text length: " + text.length());
		System.err.println("Anchor: " + (anchorRight ? "Right" : "Left")
				+ "   Position: " + anchorPos);
		System.err.println("View Low: " + viewLo + "   View High: " + viewHi);
		System.err.println("Caret Position: " + caret);
		System.err.println("Selection: " + text.substring(selLo, selHi));
		System.err.println("");
	}
	
	public void mouseClicked(MouseEvent e){mouseEvent(e);}
	public void mouseEntered(MouseEvent e){mouseEvent(e);}
	public void mouseExited(MouseEvent e){mouseEvent(e);}
	public void mousePressed(MouseEvent e){mouseEvent(e);}
	public void mouseReleased(MouseEvent e){mouseEvent(e);}
	public void keyPressed(KeyEvent e){keyEvent(e);}
	public void keyReleased(KeyEvent e){keyEvent(e);}
	public void keyTyped(KeyEvent e){keyEvent(e);}

	public void mouseDragged(MouseEvent e){mouseEvent(e);}
	public void mouseMoved(MouseEvent e){mouseEvent(e);}
}