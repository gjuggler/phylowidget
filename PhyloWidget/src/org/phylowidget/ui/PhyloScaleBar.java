package org.phylowidget.ui;

import java.awt.geom.Point2D;
import java.util.List;

import org.andrewberman.ui.menu.Menu;
import org.andrewberman.ui.menu.MenuItem;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.BasicTreeRenderer;
import org.phylowidget.render.LayoutBase;
import org.phylowidget.render.LayoutCircular;
import org.phylowidget.render.LayoutCladogram;
import org.phylowidget.render.LayoutDiagonal;
import org.phylowidget.render.LayoutUnrooted;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class PhyloScaleBar extends Menu
{
	int mode = MODE_SCALE;
	public static final int MODE_SCALE = 0;
	public static final int MODE_TIME = 1;

	float minSize = 0.1f;
	float maxSize = 10000f;

	String units;

	float percentWidth = .5f;
	float percentPosition = .5f;
	float barHeight = 10;

	double[] sizes = new double[] { 0.01, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000,2000,5000 };

	public PhyloScaleBar(PApplet app)
	{
		super(app);

		units = "";
		
		setY(app.height-10);
	}

	@Override
	public synchronized void layout()
	{
		super.layout();

	}

	@Override
	protected synchronized void drawMyself()
	{
		super.drawMyself();
		if (PhyloWidget.trees.camera == null)
			return;
		if (PhyloWidget.trees.getTree() == null)
			return;
		if (!PhyloWidget.cfg.useBranchLengths)
			return;
		
		BasicTreeRenderer renderer = (BasicTreeRenderer) PhyloWidget.trees.getRenderer();

		if (renderer.getTreeLayout() instanceof LayoutDiagonal)
			return;
		if (renderer.getTreeLayout() instanceof LayoutCircular)
			return;
		
		if (mode == MODE_SCALE)
		{
			// Find x and y.
			float canvasW = canvas.width;
			float myWidth = canvasW * percentWidth;
			float myCenter = canvasW * percentPosition;

			// Special case: if we have a small percentPosition but a large myWidth.
			if (myCenter - myWidth < 0)
			{
				myCenter = myWidth / 2;
			}

			// ok, so by now we have the center position and a desired width for the scale bar.
			// Find the distance-per-pixel.
			PhyloTree tree = (PhyloTree) PhyloWidget.trees.getTree();
			
			if (tree.getNumEnclosedLeaves(tree.getRoot()) == 1)
				return;

			double branchLengthPerPixel = 1;
			
			LayoutBase curLayout = renderer.getTreeLayout();
			if (curLayout instanceof LayoutUnrooted)
			{
				// Find the per-pixel length.
				PhyloNode n = tree.getFurthestLeafFromVertex(tree.getRoot());
				while (n != null)
				{
					// Make sure we have a node with >0 branch length.
					if (tree.getBranchLength(n) > 0)
						break;
					n = tree.getParentOf(n);
				}
				// Now, find the pixel distance between this node and the next.
				double branchLength = tree.getBranchLength(n);
				PhyloNode parent = tree.getParentOf(n);
				Point2D.Double parentPt = new Point2D.Double(parent.getX(), parent.getY());
				Point2D.Double childPt = new Point2D.Double(n.getX(), n.getY());
				double distance = parentPt.distance(childPt);

				branchLengthPerPixel = branchLength / distance;
			} else if (curLayout instanceof LayoutCladogram)
			{
				// Find the per-pixel length.
				PhyloNode n = tree.getFurthestLeafFromVertex(tree.getRoot());
				while (n != null)
				{
					// Make sure we have a node with >0 branch length.
					if (tree.getBranchLength(n) > 0)
						break;
					n = tree.getParentOf(n);
				}
				// Now, find the pixel distance between this node and the next.
				double branchLength = tree.getBranchLength(n);
				PhyloNode parent = tree.getParentOf(n);
				Point2D.Double parentPt = new Point2D.Double(parent.getX(), parent.getY());
				Point2D.Double childPt = new Point2D.Double(n.getX(), n.getY());
//				double distance = parentPt.distance(childPt);
				double distance = Math.abs(parentPt.x - childPt.x);

				branchLengthPerPixel = branchLength / distance;
			} else
			{
				// Nothing to do...
			}
			double idealSize = 0;
			for (int i=sizes.length-1; i >= 0; i--)
			{
				double d = sizes[i];
				// Find the would-be width of this size scale.
				double wouldBeWidth = d/branchLengthPerPixel;
				if (wouldBeWidth < myWidth || i == 0)
				{
					idealSize = d;
					break;
				}
			}
			double scaleWidth = idealSize / branchLengthPerPixel;

			// Do the drawing.
			canvas.strokeWeight(2f);
			canvas.stroke(0);
			canvas.fill(0);
			canvas.textSize(10);
			canvas.line((float)(myCenter - scaleWidth/2), y, (float)(myCenter+scaleWidth/2), y);
			canvas.textAlign(canvas.CENTER, canvas.BOTTOM);
			canvas.text(idealSize+"",myCenter,y);

		} else if (mode == MODE_TIME)
		{

		}
	}

	@Override
	public MenuItem create(String label)
	{
		return null;
	}

}
