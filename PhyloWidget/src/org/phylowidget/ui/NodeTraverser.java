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
package org.phylowidget.ui;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.andrewberman.ui.AbstractUIObject;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.UIRectangle;
import org.andrewberman.ui.UIUtils;
import org.andrewberman.ui.tools.Tool;
import org.andrewberman.ui.tween.Tween;
import org.andrewberman.ui.tween.TweenListener;
import org.andrewberman.ui.tween.TweenQuad;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.RenderConstants;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class NodeTraverser extends AbstractUIObject implements TweenListener, KeyListener
{
	public static final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;
	private NodeRange curNodeRange;
	public Tween glowTween;

	boolean isGlowing;
	Point mousePt = new Point();

	ArrayList nearNodes = new ArrayList(10);

	private PApplet p;
	private PWContext context;
	
	Point pt = new Point();

	UIRectangle rect = new UIRectangle();

	Point tempPt = new Point();

	public NodeTraverser(PApplet p)
	{
		this.p = p;
		this.context = PWPlatform.getInstance().getThisAppContext();
		glowTween = new Tween(this, TweenQuad.tween, Tween.INOUT, 1f, .75f, 30);
		context.event().add(this);
		p.addKeyListener(this);
	}

	public void destroy()
	{
		context.event().remove(this);
		p.removeKeyListener(this);
		glowTween = null;
	}
	
	public boolean containsPoint(NodeRange r, Point pt)
	{
		tempPt.setLocation(getX(r), getY(r));
		if (r == null || r.render == null)
			return false;
		float radius = r.render.getNodeRadius();
		radius = Math.max(radius, 2);
		float distance = (float) pt.distance(tempPt);
		// System.out.println("rad:" + radius + " dist:" + distance);
		if (distance < radius)
		{
			return true;
		}

		Rectangle rc = new Rectangle();
		rc.setFrameFromDiagonal(r.loX, r.loY, r.hiX, r.hiY);
		return rc.contains(pt);
	}

	private boolean glow = true;
	public void setGlow(boolean glow)
	{
		this.glow = glow;
	}
	
	public synchronized void draw()
	{
		/*
		 * Update the glowing circle's radius.
		 */
		if (isGlowing && glow)
		{
			glowTween.update();
			float glowRadius = context.trees().getRenderer().getTextSize() / 2;
			glowRadius *= glowTween.getPosition();

			p.noFill();
			p.strokeWeight(glowRadius / 10f);
			int color = RenderConstants.hoverColor.getRGB();
			p.stroke(color);
			NodeRange r = getCurRange();
			float cX = getX(r);
			float cY = getY(r);
			p.ellipse(cX, cY, glowRadius, glowRadius);
		}
	}

	public void focusEvent(FocusEvent e)
	{

	}

	public NodeRange getCurRange()
	{
		if (curNodeRange == null)
		{
			/*
			 * If we haven't set a "focused" NodeRange yet, set it to the root
			 * node.
			 */
			TreeRenderer render = context.trees().getRenderer();
			if (render == null)
				return null;
			RootedTree t = render.getTree();
			PhyloNode n = (PhyloNode) t.getRoot();
			curNodeRange = rangeForNode(render, n);
		}
		return curNodeRange;
	}

	public PhyloNode getCurrentNode()
	{
		NodeRange nr = getCurRange();
		return nr.node;
	}

	Rectangle2D.Float tempRect = new Rectangle2D.Float();
	private NodeRange getNearestNode(float x, float y)
	{
		getWithinRange(x, y, 100);
		pt.setLocation(x, y);
		float nearestDist = Float.MAX_VALUE;
		NodeRange temp = null;
		for (int i = 0; i < nearNodes.size(); i++)
		{
			NodeRange r = (NodeRange) nearNodes.get(i);
			PhyloNode n = r.node;
			switch (r.type)
			{
				case (NodeRange.NODE):
					float dist = (float) pt.distance(getX(r), getY(r));
					if (containsPoint(r, pt))
					{
						nearestDist = dist;
						return r;
					}
					if (dist < nearestDist)
					{
						temp = r;
						nearestDist = dist;
					}
					break;
			}
		}
		return temp;
	}

	private void getWithinRange(float x, float y, float radius)
	{
//		float ratio = TreeManager.camera.getZ();
		float ratio = 1;
		float rad = radius * ratio;

		pt.setLocation(x, y);
		rect.x = (float) (pt.getX() - rad);
		rect.y = (float) (pt.getY() - rad);
		rect.width = rad * 2;
		rect.height = rad * 2;
		UIUtils.screenToModel(pt);
		UIUtils.screenToModel(rect);
		nearNodes.clear();
		context.trees().nodesInRange(nearNodes, rect);
	}

	float getX(NodeRange r)
	{
		return r.node.getX();
	}

	float getY(NodeRange r)
	{
		return r.node.getY();
	}

	public void keyEvent(KeyEvent e)
	{
		if (e.getID() != KeyEvent.KEY_PRESSED)
			return;
		if (context.ui().contextMenu.isOpen())
			return;
		if (context.focus().getFocusedObject() != null)
			return;
		int code = e.getKeyCode();
		
		Tool t = context.event().getToolManager().getCurrentTool();
		
		switch (code)
		{
			case (KeyEvent.VK_LEFT):
				navigate(LEFT);
				break;
			case (KeyEvent.VK_RIGHT):
				navigate(RIGHT);
				break;
			case (KeyEvent.VK_UP):
				navigate(UP);
				break;
			case (KeyEvent.VK_DOWN):
				navigate(DOWN);
				break;
			case (KeyEvent.VK_ENTER):
				if (t.respondToOtherEvents() && isGlowing)
				{
					openContextMenu();
					isGlowing = false;
				}
				break;
		}
	}

	private PhyloNode previousHoveredNode;
	
	boolean pressedWithinNode;
	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		mousePt.setLocation(screen);
		pt.setLocation(screen);
		if (context.event().getToolManager() == null)
			return;
		Tool t = context.event().getToolManager().getCurrentTool();
		if (t == null)
			return;
		if (context.ui().context == null || context.ui().contextMenu.isOpen())
			return;
		if (getCurRange() == null)
			return;
		if (context.focus().getFocusedObject() != null)
			return;
		context.trees().getRenderer().setMouseLocation(pt);
		setCurRange(getNearestNode((float) pt.getX(), (float) pt.getY()));
		boolean containsPoint = containsPoint(getCurRange(), pt);
		switch (e.getID())
		{
			case (MouseEvent.MOUSE_DRAGGED):
			case (MouseEvent.MOUSE_PRESSED):
				if (containsPoint && t.respondToOtherEvents())
				{
					pressedWithinNode = true;
					glowTween.stop();
				}
			case (MouseEvent.MOUSE_MOVED):
				PhyloTree tree = (PhyloTree) context.trees().getTree();
				if (containsPoint && t.respondToOtherEvents())
				{
					UIUtils.setCursor(this, p, Cursor.HAND_CURSOR);
					tree.setHoveredNode(getCurRange().node);
					PhyloNode hoveredNode = getCurRange().node;
					if (hoveredNode != previousHoveredNode)
					{
						fireEvent(NODE_OVER_EVENT); // GJ 2009-02-15 adding hover and glow event firing.
						previousHoveredNode = hoveredNode;
					}
				} else
				{
					UIUtils.releaseCursor(this, p);
					tree.setHoveredNode(null);
				}
				break;
			case (MouseEvent.MOUSE_CLICKED):
//			case (MouseEvent.MOUSE_RELEASED):
				if (containsPoint && t.respondToOtherEvents() && pressedWithinNode)
				{
					openContextMenu();
					isGlowing = false;
				}
				break;
		}
		if (!containsPoint)
			pressedWithinNode = false;
		if (!t.respondToOtherEvents())
		{
			PhyloTree tree = (PhyloTree) context.trees().getTree();
			tree.setHoveredNode(null);
			setCurRange(null);
		}
	}

	private void navigate(int dir)
	{
		/*
		 * Ok, our strategy for navigation will be as follows:
		 * 
		 * 1. Get all nodes within a reasonable range. 2. Score the closest
		 * nodes, adding points for closeness but deducting points for being
		 * off-axis.
		 */
		Point base = new Point();
		NodeRange cur = getCurRange();

		/*
		 * The LEFT or RIGHT directions should always go IN or OUT of the tree,
		 * while the up and down will use the score-based search.
		 */
		if (dir == LEFT || dir == RIGHT)
		{
			RootedTree t = cur.render.getTree();
			PhyloNode curNode = null;
			if (dir == LEFT)
			{
				if (t.getParentOf(cur.node) != null)
				{
					curNode = (PhyloNode) t.getParentOf(cur.node);
					setCurRange(rangeForNode(cur.render, curNode));
					return;
				}
			} else if (dir == RIGHT)
			{
				if (!t.isLeaf(cur.node))
				{
					List kids = t.getChildrenOf(cur.node);
					curNode = (PhyloNode) kids.get(kids.size() - 1);
					setCurRange(rangeForNode(cur.render, curNode));
					return;
				}
			}

		}

		switch (dir)
		{
			case (LEFT):
				base.setLocation(-1, .1);
				break;
			case (RIGHT):
				base.setLocation(1, -.1);
				break;
			case (UP):
				base.setLocation(0, -1);
				break;
			case (DOWN):
				base.setLocation(0, 1);
				break;
		}

		pt.setLocation(cur.node.getLayoutX(), cur.node.getLayoutY());
		getWithinRange(cur.node.getLayoutX(), cur.node.getLayoutY(), 200);
		Point pt2 = new Point();
		float maxScore = -Float.MAX_VALUE;
		NodeRange maxRange = null;
		for (int i = 0; i < nearNodes.size(); i++)
		{
			NodeRange r = (NodeRange) nearNodes.get(i);
			if (r.type == NodeRange.LABEL)
				continue;
			if (r.node == cur.node)
				continue;
			pt2.setLocation(r.node.getLayoutX(), r.node.getLayoutY());
			float score = score(pt, pt2, base);
			if (score > maxScore)
			{
				maxScore = score;
				maxRange = r;
			}
		}
		if (maxRange == null)
		{
			System.out.println("Nothing near found");
		} else
		{
			setCurRange(maxRange);
		}
	}

	void openContextMenu()
	{
		context.ui().contextMenu.open(getCurRange());
	}

	private NodeRange rangeForNode(TreeRenderer tr, PhyloNode n)
	{
		return n.range;
	}

	private void resetGlow()
	{
		glowTween.rewind();
		glowTween.start();
	}

	float score(Point me, Point him, Point dirV)
	{
		him.translate((float) -me.getX(), (float) -me.getY());
		float len = him.length();
		float dot = dirV.dotProd(him);
		if (dot / len < 0.1)
			return -Float.MAX_VALUE;
		return dot / len - len / 30;
	}

	public static final int NODE_GLOW_EVENT = 23987325;
	public static final int NODE_OVER_EVENT = 23987326;
	public void setCurRange(NodeRange r)
	{
		if (r != null && r != curNodeRange)
		{
			// This is a "new" hovered node. Trigger an event!
			fireEvent(this.NODE_GLOW_EVENT);
			
			Tool t = context.event().getToolManager().getCurrentTool();
			if (t.respondToOtherEvents())
			{
				RootedTree tree = r.render.getTree();
			}
		}
		/*
		 * Logic for the glow resetting and whatnot.
		 */
		if (r == null)
		{
			isGlowing = false;
		} else
		{
			if (isGlowing == false)
				resetGlow();
			if (r != curNodeRange)
			{
				curNodeRange = r;
				resetGlow();
			}
			isGlowing = true;
		}
	}

	public void tweenEvent(Tween source, int eventType)
	{
		if (eventType == Tween.FINISHED)
			source.yoyo();
	}

	public void keyPressed(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyReleased(KeyEvent e)
	{
		keyEvent(e);
	}

	public void keyTyped(KeyEvent e)
	{
		keyEvent(e);
	}

}
