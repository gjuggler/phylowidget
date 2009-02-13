package org.phylowidget.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuItem;
import org.phylowidget.PWContext;
import org.phylowidget.PhyloTree;
import org.phylowidget.render.RenderConstants;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class NodeUncollapser extends Menu{

	PWContext pwContext;
	PhyloNode node;
	Shape s;
	
	private Shape origShape;
	
	public static HashMap<PhyloNode,NodeUncollapser> map = new HashMap<PhyloNode,NodeUncollapser>();
	
	public NodeUncollapser(PApplet app, PhyloNode node)
	{
		super(app);
		pwContext = (PWContext) context;
		this.node = node;
		
		Polygon p = new Polygon();
		p.addPoint(0, 10);
		p.addPoint(13,0);
		p.addPoint(0,-10);
		origShape = p;
		
		s = origShape;
		
		map.put(node,this);
	}
	
	@Override
	public boolean isOpen()
	{
		return true;
	}
	
	public static boolean containsNode(PhyloNode n)
	{
		return map.containsKey(n);
	}
	
	@Override
	public void draw()
	{
		super.draw();
		// Remove ourselves if the node is no longer collapsed.
		if (!node.getTree().isCollapsed(node))
		{
			System.out.println("No longer collapsed!");
			dispose();
		}
		
		// Remove ourselves if node is no longer in tree, or tree is null.
		if (!node.getTree().containsVertex(node))
		{
			System.out.println("Doesn't contain!");
			dispose();
		}
		
		// Update our position to match the node.
		float dotWidth = node.range.render.getNodeRadius();
		float rowHeight = node.range.render.getTextSize();
		float spacing = node.range.render.getNodeOffset(node) + rowHeight*RenderConstants.labelSpacing*2;
//		float spacing = rowHeight * RenderConstants.labelSpacing*2 + dotWidth;
		float dx = (float) (spacing * Math.cos(node.getAngle()));
		float  dy = (float) (spacing * Math.sin(node.getAngle()));
		AffineTransform at = AffineTransform.getTranslateInstance(node.getX()+dx,node.getY()+dy);
		at.rotate(node.getAngle());
		float scale = rowHeight * .3f;
		scale = Math.max(scale,5);
		scale /= 10;
		at.scale(scale, scale);
		s = at.createTransformedShape(origShape);
		setPosition(node.getX()+dx, node.getY()+dy);
		
		setHidden(false);
		PhyloNode parent = (PhyloNode) node.getParent();
		while (parent != null)
		{
			PhyloTree tree = parent.getTree();
			if (tree != null && tree.isCollapsed(parent))
			{
				setHidden(true);
			}
			parent = (PhyloNode) parent.getParent();
		}
	}
	
	@Override
	public void performAction()
	{
		super.performAction();
		pwContext.getPW().setMessage("");
		node.getTree().uncollapseNode(node);
		pwContext.ui().layout();
		dispose();
	}
	
	@Override
	protected void drawMyself()
	{
		super.drawMyself();
		canvas.stroke(100);
		canvas.noFill();
		canvas.strokeWeight(2);
		buff.g2.setColor(new Color(255,255,0));
		buff.g2.fill(s);
		if (mouseInside)
		{
			buff.g2.setStroke(new BasicStroke(1f));
			buff.g2.setColor(new Color(0,0,0));
		} else
		{
			buff.g2.setStroke(new BasicStroke(.5f));
			buff.g2.setColor(new Color(0,140,200));	
		}
		buff.g2.draw(s);
	}
	
	boolean mouseJustLeft = false;
	@Override
	protected void itemMouseEvent(MouseEvent e, Point pt)
	{
		super.itemMouseEvent(e, pt);
		
		if (mouseInside)
		{
			List<PhyloNode> kids = node.getTree().getChildrenOf(node);
			int numLeaves = 0;
			for (PhyloNode n : kids) {
				numLeaves += n.getNumLeaves();
			}
			pwContext.getPW().setMessage("Click to uncollapse "+numLeaves+" leaf nodes.");
		}
		
		if (!mouseInside && !mouseJustLeft)
		{
			mouseJustLeft = true;
		}
		
		if (mouseJustLeft)
		{
			mouseJustLeft = false;
			pwContext.getPW().setMessage("");
		}
	}
	
	@Override
	public void dispose()
	{
		map.remove(node);
		super.dispose();
	}

	@Override
	protected boolean containsPoint(Point pt)
	{
		return s.contains(pt.x, pt.y);
	}
	
	@Override
	public MenuItem create(String label)
	{
		return null;
	}
	
	
	
}
