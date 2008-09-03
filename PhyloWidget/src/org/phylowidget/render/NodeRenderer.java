package org.phylowidget.render;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Set;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.TextField;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;
import org.phylowidget.UsefulConstants;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;
import processing.core.PGraphics;

public final class NodeRenderer implements UsefulConstants
{
	static Graphics2D g2;
	static FontMetrics fm;
	static BasicTreeRenderer r;

	static float[] ZEROES = new float[] { 0, 0 };

	/*
	 * Create the taxon color map, which is used if this tree has NHX annotations.
	 */
	static HashMap<String, Integer> taxonColorMap = new HashMap<String, Integer>();

	static NodeRender nr = new NodeRender();
	static LineRender lineRender = new LineRender();
	static ImageRender ir = new ImageRender();
	static LabelRender lr = new LabelRender();

	static RenderItem[] renderables = new RenderItem[] { ir, lr };
	static RenderItem[] untransformed = new RenderItem[] {};

	public NodeRenderer()
	{
	}

	public static final void renderNode(BasicTreeRenderer r, PhyloNode n)
	{
		PGraphics canvas = r.canvas;
		g2 = r.canvas.g2;

		NodeRenderer.r = r;

		// Translate the canvas to the node's x and y coords.
		float x = n.getRealX();
		float y = n.getRealY();
		canvas.pushMatrix();
		canvas.translate(x, y);
		canvas.rotate(n.getAngle());

		float dMult = 1;
		if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
			dMult = -1;

		n.rect.setFrame(n.getRealX(), n.getRealY(), 0, 0);

		canvas.translate(nr.render(canvas, n, false)[0] * dMult + RenderConstants.labelSpacing * dMult, 0);
		float dx = 0;
		for (RenderItem ri : renderables)
		{
			float[] xy = ri.render(canvas, n, true);
			dx = xy[0];
			canvas.translate((dx + RenderConstants.labelSpacing) * dMult, 0);
		}

		canvas.popMatrix();

		for (RenderItem ri : untransformed)
		{
			ri.render(canvas, n, true);
		}
	}

	public final static void setCornerPoints(BasicTreeRenderer r, PhyloNode n)
	{
		PGraphics canvas = r.canvas;
		g2 = r.canvas.g2;

		NodeRenderer.r = r;

		// Translate the canvas to the node's x and y coords.
		float x = n.getRealX();
		float y = n.getRealY();
		canvas.pushMatrix();
		canvas.translate(x, y);
		canvas.rotate(n.getAngle());

		float dMult = 1;
		if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
			dMult = -1;

		n.rect.setFrame(n.getRealX(), n.getRealY(), 0, 0);

		float dx = 0;
		float dy = 1;
		float maxHeight = 0;
		float totalWidth = 0;
		for (RenderItem ri : renderables)
		{
			float[] xy = ri.render(canvas, n, false);
			dx = xy[0];
			dy = xy[1];
			totalWidth += dx;
			maxHeight = Math.max(dy, maxHeight);
			//			canvas.translate((dx + RenderConstants.labelSpacing) * dMult, 0);
		}

		// Here's where we calculate the corners and register those points.
		if (dMult == -1)
			canvas.translate(-totalWidth, 0);
		setPoint(canvas, n, 0, -maxHeight / 2, n.corners[0]); // Top left.
		setPoint(canvas, n, totalWidth, -maxHeight / 2, n.corners[1]); // Top right..
		setPoint(canvas, n, totalWidth, maxHeight / 2, n.corners[2]); // Bottom right.
		setPoint(canvas, n, 0, maxHeight / 2, n.corners[3]); // Bottom left.

		canvas.popMatrix();
	}

	private static void setPoint(PGraphics canvas, PhyloNode n, float x, float y, Point2D pt)
	{
		float screenX = canvas.screenX(x, y);
		float screenY = canvas.screenY(x, y);
		pt.setLocation(screenX, screenY);
		//		canvas.ellipse(x, y, 5, 5); // For simple debugging.
	}

	static void getColorsForSpeciesMap()
	{
		int n = taxonColorMap.size();
		Set<String> keys = taxonColorMap.keySet();
		float step = 1f / (n + 1f);
		float pos = 0;
		for (String key : keys)
		{
			pos += step;
			int color = Color.HSBtoRGB(pos, .7f, .85f);
			taxonColorMap.put(key, color);
		}
	}

	static float strokeForNode(PhyloNode n)
	{
		float stroke = r.baseStroke;
		if (n.found)
		{
			stroke *= RenderConstants.foundStroke;
			return stroke;
		}
		switch (n.getState())
		{
			case (PhyloNode.CUT):
				stroke *= RenderConstants.dimStroke;
				break;
			case (PhyloNode.COPY):
				stroke *= RenderConstants.copyStroke;
				break;
			case (PhyloNode.NONE):
			default:
				stroke *= RenderConstants.regStroke;
				break;
		}
		return stroke;
	}

	static private Point2D.Float tempPoint = new Point2D.Float();

	private static final void registerPoint(PGraphics canvas, PhyloNode n, float x, float y)
	{
		float screenX = canvas.screenX(x, y);
		float screenY = canvas.screenY(x, y);
		//		if (n.rect.contains(screenX,screenY))
		//			return;

		n.rect.add(screenX, screenY);
		n.range.loX = (float) n.rect.getMinX();
		n.range.hiX = (float) n.rect.getMaxX();
		n.range.loY = (float) n.rect.getMinY();
		n.range.hiY = (float) n.rect.getMaxY();
	}

	private static final float getFloatAnnotation(PhyloNode n, String key)
	{
		String ann = n.getAnnotation(key);
		if (ann == null)
			return -1;
		return Float.parseFloat(ann);
	}

	public static abstract class RenderItem
	{
		public abstract float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender);
	}

	public static class NodeRender extends RenderItem
	{
		@Override
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender)
		{
			offX = 0;
			offY = 0;
			return drawNodeMarkerImpl(canvas, n, actuallyRender);
		}

		static float offX;
		static float offY;

		public void renderUntransformed(PGraphics canvas, PhyloNode n)
		{
			offX = n.getRealX();
			offY = n.getRealY();
			drawNodeMarkerImpl(canvas, n, true);
		}

		private float nodeSizeForNode(PhyloNode n)
		{
			float thisDotSize = r.dotWidth;

			// Multiply by inner ratio.
			if (!r.tree.isLeaf(n))
			{
				thisDotSize *= PhyloWidget.cfg.innerNodeRatio;
			}

			// Look for the NSZ annotations.
			float nS = getFloatAnnotation(n, NODE_SIZE);
			if (nS > -1)
				thisDotSize *= nS;

			return thisDotSize;
		}

		protected float[] drawNodeMarkerImpl(PGraphics canvas, PhyloNode n, boolean actuallyRender)
		{
			canvas.fill(nodeColor(n));
			canvas.noStroke();

			RootedTree tree = r.tree;
			float thisDotSize = nodeSizeForNode(n);

			if (thisDotSize == 0)
				return ZEROES;
			if (n.isNHX() && PhyloWidget.cfg.colorDuplications && !tree.isLeaf(n))
			{
				String s = n.getAnnotation(DUPLICATION);
				if (s != null)
				{
					if (s.toLowerCase().startsWith("t") || s.toLowerCase().startsWith("y"))
					{
						canvas.fill(RenderConstants.copyColor.getRGB());
					} else
					{
						canvas.fill(new Color(0, 0, 255).getRGB());
					}
				}
			}

			if (!actuallyRender)
				return new float[] { thisDotSize / 2, thisDotSize / 2 };

			if (PhyloWidget.cfg.nodeShape.equals("square"))
			{
				canvas.rect(offX - thisDotSize / 2, offY - thisDotSize / 2, thisDotSize, thisDotSize);
			} else
			{
				canvas.ellipseMode(PGraphics.CENTER);
				canvas.ellipse(offX, offY, thisDotSize, thisDotSize);
			}

			return new float[] { thisDotSize / 2, thisDotSize / 2 };
		}

		static int nodeColor(PhyloNode n)
		{
			if (n.found)
			{
				return RenderConstants.foundColor.getRGB();
			}
			if (n == ((PhyloTree) r.tree).hoveredNode)
			{
				return RenderConstants.hoverColor.getRGB();
			}
			switch (n.getState())
			{
				case (PhyloNode.CUT):
					return RenderConstants.dimColor.getRGB();
				case (PhyloNode.COPY):
					return RenderConstants.copyColor.getRGB();
				case (PhyloNode.NONE):
				default:
					int c = PhyloWidget.cfg.getNodeColor().getRGB();

					String nodeColor = n.getAnnotation(NODE_COLOR);
					if (nodeColor != null)
					{
						c = Color.parseColor(nodeColor).getRGB();
					}
					return c;
			}
		}

	}

	public static class LineRender extends RenderItem
	{
		@Override
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender)
		{
			if (!actuallyRender)
				return ZEROES;
			PhyloNode parent = (PhyloNode) n.getParent();
			if (parent != null)
				drawLine(r, parent, n);
			return ZEROES;
		}

		protected void drawLine(BasicTreeRenderer r, PhyloNode p, PhyloNode c)
		{
			if (p == null)
				return;
			/*
			 * Keep in mind that p may be null (in the case of root node).
			 */
			float weight = nodeStroke(r, c);
			//		weight = 1;
			if (weight == 0)
				return;
			/*
			 * Only set a minimum weight of 0.5 if we're not outputting to a file.
			 */
			if (!RenderOutput.isOutputting)
				weight = Math.max(0.5f, weight);
			r.canvas.strokeWeight(weight);
			r.canvas.stroke(lineColor(c));

			PhyloTree tree = (PhyloTree) r.getTree();
			if (c == tree.hoveredNode)
			{
				r.canvas.stroke(RenderConstants.hoverColor.getRGB());
				r.canvas.strokeWeight(weight * RenderConstants.hoverStroke);
			}

			if (c.isNHX() && PhyloWidget.cfg.colorBootstrap && !c.found)
			{
				double d = getFloatAnnotation(c, BOOTSTRAP);
				if (d > -1)
				{
					d = (100 - d) * 200f / 100f;
					d = r.clamp(d, 0, 255);
					r.canvas.stroke(PhyloWidget.cfg.getBranchColor().brighter(d).getRGB());
				}
			}

			r.getTreeLayout().drawLine(r.canvas, p, c);

			//			r.canvas.line(p.getRealX(), p.getRealY(), p.getRealX(), c.getRealY());
			//			r.canvas.line(p.getRealX(), c.getRealY(), c.getRealX(),c.getRealY());
		}

		static float nodeStroke(BasicTreeRenderer r, PhyloNode n)
		{
			float stroke = strokeForNode(n);
			float bSize = getFloatAnnotation(n, BRANCH_SIZE);
			if (bSize > -1)
				stroke *= bSize;
			return stroke;
		}

		static int lineColor(PhyloNode n)
		{
			if (n.found)
			{
				return RenderConstants.foundColor.getRGB();
			}
			switch (n.getState())
			{
				case (PhyloNode.CUT):
					return RenderConstants.dimColor.getRGB();
				case (PhyloNode.COPY):
					return RenderConstants.copyColor.getRGB();

				case (PhyloNode.NONE):
				default:
					int c = PhyloWidget.cfg.getBranchColor().getRGB();
					String branchColor = n.getAnnotation(BRANCH_COLOR);
					if (branchColor != null)
					{
						c = Color.parseColor(branchColor).getRGB();
					}
					return c;
			}
		}
	}

	static class ImageRender extends RenderItem
	{
		static boolean fitImagesToSquare = true;

		@Override
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender)
		{
			return renderImage(r, n, actuallyRender);
		}

		private float imageSizeForNode(BasicTreeRenderer r, PhyloNode n)
		{
			float thisRowSize = r.getTextSize() * PhyloWidget.cfg.imageSize * n.bulgeFactor;
			thisRowSize = Math.max(thisRowSize, PhyloWidget.cfg.minTextSize);

			// If we find a NHX image size annotation, scale accordingly.
			float iMult = getFloatAnnotation(n, IMAGE_SIZE);
			if (iMult > -1)
				thisRowSize *= iMult;

			return thisRowSize;
		}

		/*
		 * Renders a given image and returns the width that it's taking up.
		 */
		float[] renderImage(BasicTreeRenderer r, PhyloNode n, boolean actuallyRender)
		{
			String imgS = n.getAnnotation("img");
			if (imgS == null)
				return ZEROES;

			boolean alignRight = false;
			if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
				alignRight = true;

			float rowHeight = imageSizeForNode(r, n);

			float[] size = imageSize(n, rowHeight);
			float scaledW = size[0];
			float scaledH = size[1];

			if (actuallyRender)
			{
				Graphics2D g2 = r.canvas.g2;
				PGraphics canvas = r.canvas;

				float dx = (rowHeight - scaledW) / 2;
				if (alignRight)
					dx -= scaledW;

				Image img = PhyloWidget.trees.imageLoader.getImageForNode(n);
				g2.drawImage(img, (int) dx, (int) -scaledH / 2, (int) scaledW, (int) scaledH, null);
				//		canvas.image(img, dx, -scaledH / 2, scaledW, scaledH);
			}

			if (fitImagesToSquare)
				return new float[] { rowHeight, rowHeight };
			else
				return new float[] { scaledW, rowHeight };
		}

		public float[] imageSize(PhyloNode n, float rowHeight)
		{
			Image img = PhyloWidget.trees.imageLoader.getImageForNode(n);
			if (img == null)
				return new float[] { 0, 0 };

			float scaling = 1;
			float imgW = img.getWidth(null);
			float imgH = img.getHeight(null);
			if (fitImagesToSquare)
			{
				if (imgW > imgH)
				{
					scaling = rowHeight / imgW;
				} else
				{
					scaling = rowHeight / imgH;
				}
			} else
			{
				scaling = rowHeight / imgH;
			}
			float scaledW = imgW * scaling;
			float scaledH = imgH * scaling;
			return new float[] { scaledW, scaledH };
		}
	}

	static class LabelRender extends RenderItem
	{
		@Override
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender)
		{
			// Calculate the row / text size.
			float curTextSize = textSizeForNode(r, n);

			boolean alignRight = false;
			if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
				alignRight = true;

			// Grab the NHX annotated label size mult. factor
			float labelMult = getFloatAnnotation(n, LABEL_SIZE);
			if (labelMult > -1)
				curTextSize *= labelMult;

			//			canvas.strokeWeight(nodeStroke(r,n));
			//			canvas.fill(textColor(n));

			/*
			 * Early exit strategy if text is too small. Don't do this if we're outputting to a file.
			 */
			if (curTextSize < .5f && !RenderOutput.isOutputting)
			{
				return ZEROES;
			}

			if (PhyloWidget.cfg.textRotation != 0)
			{
				canvas.rotate(PApplet.radians(PhyloWidget.cfg.textRotation));
			}

			RootedTree tree = r.tree;
			if (tree.isLeaf(n) && n.found)
			{
				/*
				 * Draw a background rect.
				 */
				canvas.noStroke();
				canvas.fill(RenderConstants.foundBackground.getRGB());
				if (actuallyRender)
				{
					if (alignRight)
						canvas.rect(-n.unitTextWidth * curTextSize, -curTextSize / 2,
							(float) (n.unitTextWidth * curTextSize), curTextSize);
					else
						canvas.rect(0, -curTextSize / 2, (float) (n.unitTextWidth * curTextSize), curTextSize);
				}
			}

			float dx = curTextSize * n.unitTextWidth;

			/*
			 * THIS IS THE MAIN LABEL DRAWING CODE. SO SLEEK, SO SIMPLE!!!
			 */
			canvas.fill(textColor(n));
//			canvas.textFont(r.font);
			curTextSize = Math.min(curTextSize, 128);
			canvas.textSize(curTextSize);
			//		canvas.textSize(10);
			if (n.found)
			{
				canvas.fill(RenderConstants.foundForeground.getRGB());
			}

			if (!tree.isLeaf(n))
			{
				if (PhyloWidget.cfg.showCladeLabels)
				{
					curTextSize *= 0.75f;
					canvas.textSize(curTextSize);
					canvas.textAlign(canvas.RIGHT, canvas.BOTTOM);
					float s = strokeForNode(n);
//					registerPoint(canvas,n,)
					if (actuallyRender)
					{
						canvas.text(n.getLabel(), n.getRealX()-curTextSize / 3 - s, n.getRealY()-s - curTextSize / 5);
					}
				}
			} else
			{
				if (alignRight)
				{
					canvas.textAlign(canvas.RIGHT, canvas.BASELINE);
					registerPoint(canvas, n, 0, -curTextSize / 2);
					registerPoint(canvas, n, -dx, curTextSize / 2);
				} else
				{
					canvas.textAlign(canvas.LEFT, canvas.BASELINE);
					registerPoint(canvas, n, 0, -curTextSize / 2);
					registerPoint(canvas, n, dx, curTextSize / 2);
				}
				if (actuallyRender)
					canvas.text(n.getLabel(), 0, 0 + r.dFont * curTextSize / r.textSize);
			}
			if (actuallyRender)
				n.lastTextSize = curTextSize;
			return new float[] { dx, curTextSize };
		}

		private float textSizeForNode(BasicTreeRenderer r, PhyloNode n)
		{
			if (PhyloWidget.cfg.hideAllLabels)
				return 0;
			float thisRowSize = r.getTextSize() * PhyloWidget.cfg.textScaling * n.bulgeFactor;

			if (PhyloWidget.cfg.showAllLabels) // If showing all labels, don't do the mintext setting.
				return r.getTextSize() * PhyloWidget.cfg.textScaling;

			thisRowSize = Math.max(thisRowSize, PhyloWidget.cfg.minTextSize);
			return thisRowSize;
		}

		static int textColor(PhyloNode n)
		{
			if (n.isNHX())
			{
				int c = Color.black.getRGB();
				String labelColor = n.getAnnotation(LABEL_COLOR);
				String tax = n.getAnnotation(TAXON_ID);
				String spec = n.getAnnotation(SPECIES_NAME);
				if (labelColor != null)
				{
					c = Color.parseColor(labelColor).getRGB();
				} else if (tax != null && PhyloWidget.cfg.colorSpecies)
				{
					c = taxonColorMap.get(tax).intValue();
				} else if (spec != null && PhyloWidget.cfg.colorSpecies)
				{
					c = taxonColorMap.get(spec);
				}
				return c;
			} else
			{
				return PhyloWidget.cfg.getTextColor().getRGB();
			}
		}

		public void positionText(BasicTreeRenderer r, PhyloNode n, TextField tf)
		{
			//			float imgW = renderImage(r, n, 0, 0, textSize,false);
			//			if (imgW > 0)
			//				imgW += RenderConstants.labelSpacing*r.rowSize;
			float dX = 0;
			float curTextSize = n.lastTextSize;

			float dY = r.dFont * curTextSize / r.textSize;

			float textWidth = (float) n.unitTextWidth * curTextSize;

			PGraphics canvas = null;
			if (canvas != null)
			{
				dX += nr.render(canvas, n, false)[0];
				dX += ir.render(canvas, n, false)[0];
			}

			float x = n.getRealX() + r.getNodeRadius() + r.getNormalLineWidth() * 2;
			float y = n.getRealY();
			tf.setTextSize(curTextSize);
			tf.setWidth(textWidth);
			tf.setPositionByBaseline(x + dX, y + dY);
		}
	}
}
