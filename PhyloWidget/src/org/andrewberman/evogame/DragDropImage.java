package org.andrewberman.evogame;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuItem;
import org.andrewberman.ui.tween.PropertyTween;
import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenFunction;
import org.andrewberman.ui.tween.TweenQuad;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;
import processing.core.PImage;

public class DragDropImage extends Menu
{
	abstract class Followable
	{
		Object o;

		public Followable(Object o)
		{
			this.o = o;
		}

		public abstract float getHeight();

		public abstract float getWidth();

		public abstract float getX();

		public abstract float getY();
	}

	static int LONG = 30;
	static HashMap<PhyloNode, DragDropImage> nodeToImage;
	static int SHORT = 15;
	static float SIZE = 128;
	float aspectRatio;

	private float downMouseX;
	private float downMouseY;
	private float downPosX;
	private float downPosY;
	private boolean dragging;
	Followable f;

	boolean firstFrame = true;

	public float h;

	Tween hTween;
	private PImage img;
	String imgString;
	String targetNodeLabel;

	boolean isOpen = false;

	PhyloNode lastHoverTarget;

	private Rectangle2D.Float rectF = new Rectangle2D.Float();

	float SHRINK = 0.5f;
	public float w;
	Tween wTween;

	public DragDropImage(PApplet app)
	{
		super(app);
		TweenFunction tw = TweenQuad.tween;
		//		Tween tw = TweenFriction.tween(.25f);
		wTween = new PropertyTween(this, "w", tw, Tween.OUT, width, width, SHORT);
		hTween = new PropertyTween(this, "h", tw, Tween.OUT, height, height, SHORT);
	}

	public DragDropImage(PApplet app, String img, String targetNodeLabel)
	{
		this(app);
		setImage(img);
		this.targetNodeLabel = targetNodeLabel;
	}

	void attach(final PhyloNode n)
	{
		//		File f = new File(imgString);
		//		n.setAnnotation("img", f.toURL().toString());
		//		hidden = true;
		n.setAnnotation("img", "asdf");
		if (nodeToImage == null)
			nodeToImage = new HashMap<PhyloNode,DragDropImage>();
		nodeToImage.put(n, this);
		aTween.continueTo(1, SHORT);
		f = new Followable(n)
		{

			public float getHeight()
			{
				return n.range.render.getTextSize() / aspectRatio;
			}

			public float getWidth()
			{
				return n.range.render.getTextSize();
			}

			public float getX()
			{
				return n.getX() + n.range.render.getNodeRadius() * 1.05f + getWidth() / 2f;
			}

			public float getY()
			{
				return n.getY();
			}
		};
	}

	public boolean isAttachedToCorrectNode()
	{
		if (f == null)
			return false;
		PhyloNode n = (PhyloNode) f.o;
		if (n.getLabel().toLowerCase().equals(targetNodeLabel))
			return true;
		return false;
	}

	@Override
	protected boolean containsPoint(Point pt)
	{
		float w = wTween.getPosition();
		float h = hTween.getPosition();
		rectF.setRect(x - w / 2, y - h / 2, w, h);
		return rectF.contains(pt);
	}

	@Override
	public MenuItem create(String label)
	{
		return null;
	}

	void detach()
	{
		PhyloNode n = (PhyloNode) f.o;
		n.setAnnotation("img", null);
		if (nodeToImage == null)
			nodeToImage = new HashMap<PhyloNode,DragDropImage>();
		nodeToImage.remove(n);
		f = null;
	}

	@Override
	public synchronized void draw()
	{
		super.draw();
		wTween.update();
		hTween.update();

		// If we're attached to the node.
		if (f != null && !dragging)
		{
			x = f.getX();
			y = f.getY();
			wTween.continueTo(f.getWidth(), SHORT);
			hTween.continueTo(f.getHeight(), SHORT);
			return;
		}

		// Attached, but being dragged away.
		if (f != null && dragging)
		{
			// Check the distance and detach if far enough away.
			float fx = f.getX();
			float fy = f.getY();
			double distance = Math.sqrt((y - fy) * (y - fy) + (x - fx) * (x - fx));
			if (distance > 20)
				detach();
			return;
		}

		// Just dragging.
		if (dragging)
		{
			aTween.continueTo(0.5f, LONG);
		} else
		{
			aTween.continueTo(1f, LONG);
		}

		if (isOverTarget())
		{
			wTween.continueTo(width * SHRINK, SHORT);
			hTween.continueTo(height * SHRINK, SHORT);
			PhyloNode n = getTarget();
			if (n != null)
				n.setAnnotation("img", "");
			lastHoverTarget = getTarget();
		} else
		{
			if (lastHoverTarget != null)
			{
				lastHoverTarget.setAnnotation("img", null);
				lastHoverTarget = null;
			}
			wTween.continueTo(width, SHORT);
			hTween.continueTo(height, SHORT);
		}
	}

	public void setTargetNodeLabel(String s)
	{
		this.targetNodeLabel = s.toLowerCase();
	}

	@Override
	public synchronized void drawMyself()
	{
		int color = canvas.color(255, 0, 0);

		if (isAttached())
		{
			int clr = 0;
			if (isAttachedToCorrectNode())
				clr = canvas.color(0, 255, 0);
			else
				clr = canvas.color(255, 0, 0);
			canvas.rectMode(canvas.CENTER);
			canvas.strokeWeight(3);
			canvas.stroke(clr);
			canvas.noFill();
			canvas.rect(x, y, w, h);
			canvas.rectMode(canvas.CORNER);
		}

		if (img == null)
		{
			canvas.fill(color);
			canvas.rect(x, y, w, h);
		} else
		{
			canvas.imageMode(canvas.CENTER);
			canvas.smooth();
			canvas.image(img, x, y, w, h);

			canvas.noSmooth();
			canvas.imageMode(canvas.CORNER);
		}
		canvas.noTint();
	}

	PhyloNode getTarget()
	{
		return null;
//		return EvoGameApplet.ui.getHoveredNode();
	}

	boolean isAttached()
	{
		return (f != null);
	}

	@Override
	public boolean isOpen()
	{
		return true;
	}

	boolean isOverTarget()
	{
		PhyloNode n = getTarget();
		if (n != null && dragging)
		{
			// Check for whether there's already an image on that node.
			if (nodeToImage != null && nodeToImage.containsKey(n) && nodeToImage.get(n) != this)
				return false;
			return true;
		}
		return false;
	}

	@Override
	public synchronized void layout()
	{
		super.layout();
		menu.zSort();
		//		setSize(w,h);
	}

	void recalcSize()
	{
		float w = img.width;
		float h = img.height;
		if (w > SIZE)
		{
			float ratio = SIZE / w;
			w *= ratio;
			h *= ratio;
		}
		if (h > SIZE)
		{
			float ratio = SIZE / h;
			h *= ratio;
			w *= ratio;
		}
		//		setSize(w,h);
		//		setSize(w,h);
		aspectRatio = w / h;
		width = w;
		height = h;
		wTween.continueTo(w);
		hTween.continueTo(h);
	}

	public void setImage(String filename)
	{
		String base = "../../src/org/andrewberman/evogame/";
		PImage img = null;
//		img = EvoGameApplet.p.loadImage(base + filename);
//		imgString = base + filename;
//		if (img == null)
//		{
//			// Load it up from the actual filename.
//			img = EvoGameApplet.p.loadImage(filename);
//			imgString = filename;
//			if (img == null)
//			{
					String path = EvoGameApplet.p.getDocumentBase().toString();
					int ind = path.lastIndexOf("/");
					if (ind != -1)
						path = path.substring(0,ind);
//					if (PhyloWidget.cfg.debug)	
//						System.out.println(path);
					img = EvoGameApplet.p.loadImage(path+"/"+filename);
//			}
//		}

		//			Image img = ImageIO.read(in);
		this.img = img;
		aspectRatio = (float) img.width / (float) img.height;
		recalcSize();
		wTween.fforward();
		hTween.fforward();
	}

	public void setTree(String tree)
	{
		EvoGameApplet.p.changeSetting("tree", tree);
	}
	
	public void setNextMenu(String menu)
	{
		EvoGameApplet.nextMenu = menu;
	}
	
	@Override
	public void setOptions()
	{
		super.setOptions();
		useHandCursor = true;
		modalFocus = false;
		focusOnShow = false;
		consumeEvents = false;
	}

	@Override
	protected void visibleMouseEvent(MouseEvent e, Point tempPt)
	{
		super.visibleMouseEvent(e, tempPt);
		//		if (mouseInside || dragging)
		//			setCursor(Cursor.HAND_CURSOR);
		//		
		//		if (!mouseInside && !dragging)
		//			return;

		if (e.getID() == MouseEvent.MOUSE_RELEASED)
		{
			if (isOpen)
			{
				close();
				isOpen = false;
			}
			if (isOverTarget() && dragging)
			{
				PhyloNode n = getTarget();
				attach(n);
			}
		}
		if (e.getID() == MouseEvent.MOUSE_PRESSED)
		{
			downMouseX = tempPt.x;
			downMouseY = tempPt.y;
			downPosX = tempPt.x;
			downPosY = tempPt.y;
			if (mouseInside)
			{
				open();
				isOpen = true;
				e.consume();
			}
		}
		if (!mouseInside && !dragging)
			return;
		if (e.getID() == MouseEvent.MOUSE_DRAGGED)
		{
			if (isOpen)
			{
				//				z = 0;
				dragging = true;
				float dX = tempPt.x - downMouseX;
				float dY = tempPt.y - downMouseY;
				setPosition(downPosX + dX, downPosY + dY);
			}
		} else
		{
			dragging = false;
			//			z = -1;
			//			UIGlobals.g.event().zSort();
		}
	}

}
