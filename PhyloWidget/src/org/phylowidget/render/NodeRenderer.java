package org.phylowidget.render;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.andrewberman.ui.Color;
import org.andrewberman.ui.TextField;
import org.phylowidget.PhyloTree;
import org.phylowidget.PhyloWidget;
import org.phylowidget.UsefulConstants;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

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
	static CigarRender cr = new CigarRender();
	static ImageRender ir = new ImageRender();
	static LabelRender lr = new LabelRender();

	static RenderItem[] renderables = new RenderItem[] { ir, cr, lr };
	static RenderItem[] structRenderables = new RenderItem[] { lineRender, nr };

	public NodeRenderer()
	{
	}

	public static final void render(BasicTreeRenderer r, PhyloNode n)
	{
		renderImpl(r, n, true);
	}

	static final void renderImpl(BasicTreeRenderer r, PhyloNode n, boolean actuallyRender)
	{
		PGraphics canvas = r.canvas;
		if (canvas == null)
		{
			//			throw new RuntimeException("Null canvas!");
			return;
		}
		//		g2 = r.canvas.g2;

		NodeRenderer.r = r;

		// Translate the canvas to the node's x and y coords.
		float x = n.getX();
		float y = n.getY();
		if (r.treeLayout instanceof LayoutCladogram && r.tree.isLeaf(n))
		{
			if (PhyloWidget.cfg.alignLabels || n.getAnnotation("cigar") != null)
			{
				PhyloNode mostDistant = (PhyloNode) r.tree.getFurthestLeafFromVertex(r.tree.getRoot());
				x = mostDistant.getX();
			}
		}
		canvas.pushMatrix();
		canvas.translate(x, y);
		canvas.rotate(n.getAngle());

		float dMult = 1;
		if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
			dMult = -1;

		n.rect.setFrame(n.getX(), n.getY(), 0, 0);

		// GJ 19-09-08: fixed the spacing of rendered elements.
		float rowHeight = r.getTextSize();
		float dotWidth = r.getNodeOffset(n);

		if (PhyloWidget.cfg.treatNodesAsLabels)
		{
			boolean drawNode = (actuallyRender && n.drawLabel) || PhyloWidget.cfg.showAllLeafNodes;
			nr.render(canvas, n, drawNode, true);
		}

		if (r.getTree().isCollapsed(n))
		{
			canvas.translate(rowHeight * .3f, 0);
		}

		canvas.translate(dotWidth * dMult + RenderConstants.labelSpacing * dMult * rowHeight, 0);
		float dx = 0;
		for (RenderItem ri : renderables)
		{
			boolean drawLabel = (n.drawLabel && actuallyRender);
			float[] xy = ri.render(canvas, n, drawLabel, true);
			dx = xy[0];
			canvas.translate(dx + (RenderConstants.labelSpacing) * dMult * rowHeight, 0);
		}

		canvas.popMatrix();

		for (RenderItem ri : structRenderables)
		{
			boolean drawNode = (actuallyRender && n.drawLineAndNode);
			if (ri == nr)
			{
				if (PhyloWidget.cfg.treatNodesAsLabels)
					continue;
				// GJ 19-09-08 clarify: this is a special case, where we want all nodes to be drawn.
				drawNode = (actuallyRender && n.drawLineAndNode) || PhyloWidget.cfg.showAllLeafNodes;
			}
			ri.render(canvas, n, drawNode, false);
		}

	}

	public final static void setCornerPoints(BasicTreeRenderer r, PhyloNode n)
	{
		renderImpl(r, n, false);
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
		if (n.getLabel().length() == 0)
			return;
		
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
		try {
			float f = Float.parseFloat(ann);
			return f;
		} catch (Exception e)
		{
			return -1;
		}
	}

	public static abstract class RenderItem
	{
		protected float offX;
		protected float offY;

		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender, boolean preTransformed)
		{
			if (preTransformed)
			{
				offX = 0;
				offY = 0;
			} else
			{
				offX = n.getX();
				offY = n.getY();
			}
			return null;
		}
	}

	public static class NodeRender extends RenderItem
	{
		@Override
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender, boolean preTransformed)
		{
			super.render(canvas, n, actuallyRender, preTransformed);
			return drawNodeMarkerImpl(canvas, n, actuallyRender);
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
			float thisDotSize = nodeSizeForNode(n);
			if (canvas == null)
				return new float[] { thisDotSize / 2, thisDotSize / 2 };

			canvas.fill(nodeColor(n));
			canvas.noStroke();

			RootedTree tree = r.tree;

			if (thisDotSize == 0)
				return ZEROES;
			if (n.isNHX() && PhyloWidget.cfg.colorDuplications && !tree.isLeaf(n))
			{
				String s = n.getAnnotation(DUPLICATION);
				if (s != null)
				{
					if (PhyloNode.parseTruth(s))
					{
						canvas.fill(RenderConstants.copyColor.getRGB());
					} else
					{
						canvas.fill(new Color(0, 0, 255).getRGB());
					}
				}
			}

			// Transparent google chart: http://chart.apis.google.com/chart?cht=p&chd=t:60,40&chs=50x50&chf=bg,s,FFFFFF00
			
			// GJ 19-09-08: Try out registering node points for overlap...
			if (PhyloWidget.cfg.treatNodesAsLabels)
			{
				registerPoint(canvas, n, offX - thisDotSize / 2, offY - thisDotSize / 2);
				registerPoint(canvas, n, offX + thisDotSize / 2, offY + thisDotSize / 2);
			}

			if (!actuallyRender)
			{
				return new float[] { thisDotSize / 2, thisDotSize / 2 };
			}

			int shape = getNodeShape(n);

			if (shape == TRIANGLE)
			{
				canvas.beginShape();
				float r = thisDotSize * .75f;
				canvas.vertex(offX + 0.866f * r, offY + 0.5f * r);
				canvas.vertex(offX - 0.866f * r, offY + 0.5f * r);
				canvas.vertex(offX, offY - 1 * r);
				canvas.endShape();
			} else if (shape == SQUARE)
			{
				canvas.rect(offX - thisDotSize / 2, offY - thisDotSize / 2, thisDotSize, thisDotSize);
			} else if (shape == STAR)
			{
				canvas.pushMatrix();
				canvas.translate(offX,offY);
				canvas.beginShape();
				float r = thisDotSize ;
				float s = thisDotSize * .4f;
				for (int i=0; i < 10; i++)
				{
					double theta = Math.PI*2 * (i/10f) - Math.PI/2;
					if (i % 2 == 0)
					{
						canvas.vertex((float)(r * Math.cos(theta)),(float)(r*Math.sin(theta)));
					} else
					{
						canvas.vertex((float)(s * Math.cos(theta)),(float)(s*Math.sin(theta)));
					}
				}
				canvas.endShape();
				canvas.popMatrix();
			} else if (shape == FILLED_CIRCLE)
			{
				// Inside.
				canvas.ellipseMode(PGraphics.CENTER);
				canvas.ellipse(offX, offY, thisDotSize, thisDotSize);
				
				// Outline.
				canvas.ellipseMode(PGraphics.CENTER);
				canvas.strokeWeight(thisDotSize/10f);
				canvas.stroke(Color.black.getRGB());
				canvas.noFill();
				canvas.ellipse(offX,offY,thisDotSize,thisDotSize);
			} else
			{
				// Default to circle
				canvas.ellipseMode(PGraphics.CENTER);
				canvas.ellipse(offX, offY, thisDotSize, thisDotSize);
			}

			return new float[] { thisDotSize / 2, thisDotSize / 2 };
		}

		static final int CIRCLE = 0;
		static final int SQUARE = 1;
		static final int TRIANGLE = 2;
		static final int STAR = 3;
		static final int FILLED_CIRCLE = 4;

		static int getNodeShape(PhyloNode n)
		{
			String annotation = n.getAnnotation(UsefulConstants.NODE_SHAPE);
			String shape = PhyloWidget.cfg.nodeShape.toLowerCase();
			if (annotation != null)
			{
				shape = annotation.toLowerCase();
			}
			if (shape.startsWith(SHAPE_TRIANGLE))
				return TRIANGLE;
			else if (shape.startsWith(SHAPE_SQUARE))
				return SQUARE;
			else if (shape.startsWith(SHAPE_STAR))
				return STAR;
			else if (shape.startsWith(SHAPE_FILLED_CIRCLE))
				return FILLED_CIRCLE;
			else
				return CIRCLE;
		}

		static int nodeColor(PhyloNode n)
		{
			if (n.found)
			{
				return RenderConstants.foundColor.getRGB();
			}
			if (n == ((PhyloTree) r.tree).hoveredNode && PhyloWidget.cfg.colorHoveredBranch)
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
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender, boolean preTransformed)
		{
			super.render(canvas, n, actuallyRender, preTransformed);
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
			if (c == tree.hoveredNode && PhyloWidget.cfg.colorHoveredBranch)
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
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender, boolean preTransformed)
		{
			super.render(canvas, n, actuallyRender, preTransformed);
			return renderImage(r, n, actuallyRender);
		}

		private float imageSizeForNode(BasicTreeRenderer r, PhyloNode n)
		{
			float thisRowSize = r.getTextSize() * PhyloWidget.cfg.imageSize * n.bulgeFactor;
			if (!PhyloWidget.cfg.showAllLabels)
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
				Graphics2D g2 = ((PGraphicsJava2D) r.canvas).g2;
				PGraphics canvas = r.canvas;

				float dx = (rowHeight - scaledW) / 2;
				if (alignRight)
					dx -= scaledW;

				Image img = null;
				if (RenderOutput.isOutputting && PhyloWidget.cfg.outputFullSizeImages)
				{
					try
					{
						img = ImageIO.read(new URL(n.getFullImageURL()));
					} catch (Exception e)
					{
						e.printStackTrace();
						img = PhyloWidget.trees.imageLoader.getImageForNode(n);
					}
				} else
				{
					float minSide = Math.min(scaledH, scaledW);
					if (minSide > 300)
					{
						System.out.println("Loading full image...");
						n.loadFullImage();
					}
					img = PhyloWidget.trees.imageLoader.getImageForNode(n);
				}
				if (img != null)
				{
					if (RenderOutput.isOutputting)
						System.out.println("DRAW IMAGE " + n.getLabel());
					//					img.flush();
					g2.drawImage(img, (int) dx, (int) -scaledH / 2, (int) scaledW, (int) scaledH, null);
				}
				if (RenderOutput.isOutputting && img != null)
				{
					img.flush();
					img = null;
				}
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
		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender, boolean preTransformed)
		{
			super.render(canvas, n, actuallyRender, preTransformed);
			// Calculate the row / text size.
			float curTextSize = textSizeForNode(r, n);

			boolean alignRight = false;
			if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
				alignRight = true;

			// Grab the NHX annotated label size mult. factor
			float labelMult = getFloatAnnotation(n, LABEL_SIZE);
			if (labelMult > -1)
				curTextSize *= labelMult;

			// If collapsed, label size is smaller.
			if (r.getTree().isCollapsed(n))
			{
				curTextSize *= .6f;
			}

			boolean alwaysRender = false;
			float always = getFloatAnnotation(n, LABEL_ALWAYSSHOW);
			if (always > -1)
				alwaysRender = true;
			always = getFloatAnnotation(n, LABEL_ALWAYSSHOW_ALT);
			if (always > -1)
				alwaysRender = true;

			/*
			 * Early exit strategy if text is too small. Don't do this if we're outputting to a file.
			 */
			if (curTextSize < .5f && !RenderOutput.isOutputting && !alwaysRender)
			{
				return ZEROES;
			}

			if (PhyloWidget.cfg.textRotation != 0)
			{
				canvas.rotate(PApplet.radians(PhyloWidget.cfg.textRotation));
			}

			RootedTree tree = r.tree;
			if (tree.isLeaf(n) && (n.found || alwaysRender))
			{
				/*
				 * Draw a background rect.
				 */
				if (actuallyRender)
				{
					canvas.noStroke();
					canvas.fill(RenderConstants.foundBackground.getRGB());
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
			curTextSize = Math.min(curTextSize, 128);
			//			if (curTextSize*100 == 0 && actuallyRender)
			//				return new float[]{dx,curTextSize};
			canvas.textSize(curTextSize);
			if (n.found)
			{
				canvas.fill(RenderConstants.foundForeground.getRGB());
			}

			if (!tree.isLeaf(n))
			{
				if (PhyloWidget.cfg.showCladeLabels)
				{
					float s = strokeForNode(n);

					// TODO: Make a background rect, like we do for found nodes.
					//					if (actuallyRender)
					//					{
					//						canvas.fill(RenderConstants.foundBackground.getRGB());
					//						//					canvas.noStroke();
					//						canvas.rect(offX - dx, offY + curTextSize / 2 - curTextSize / 3 - s, offX, offY - curTextSize
					//								/ 2 - curTextSize / 3 - s);
					//					}

					canvas.fill(textColor(n));
					curTextSize *= 0.75f;

					dx = curTextSize * n.unitTextWidth;
					registerPoint(canvas, n, offX, offY - curTextSize / 2 - curTextSize / 3 - s);
					registerPoint(canvas, n, offX - dx, offY + curTextSize / 2 - curTextSize / 3 - s);
					if (actuallyRender)
					{
						canvas.textSize(curTextSize);
						canvas.textAlign(canvas.RIGHT, canvas.BASELINE);
						canvas.fill(textColor(n));
						//						canvas.text(n.getLabel(), 0, r.dFont * curTextSize / r.textSize);
						canvas.text(tree.getLabel(n), offX - curTextSize / 3 - s, offY - s - curTextSize / 3);
					}
				}
			} else
			{
//				if (PhyloWidget.cfg.textRotation != 0)
//				{
//					canvas.rotate(-PApplet.radians(PhyloWidget.cfg.textRotation));
//				}
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
//				if (PhyloWidget.cfg.textRotation != 0)
//				{
//					canvas.rotate(PApplet.radians(PhyloWidget.cfg.textRotation));
//				}
				if (actuallyRender)
					canvas.text(tree.getLabel(n), 0, 0 + r.dFont * curTextSize / r.textSize);
			}
			if (actuallyRender)
				n.lastTextSize = curTextSize;
			return new float[] { dx, curTextSize };
		}

		private float textSizeForNode(BasicTreeRenderer r, PhyloNode n)
		{
			String always = n.getAnnotation(UsefulConstants.LABEL_ALWAYSSHOW);
			if (always == null)
				always = n.getAnnotation(UsefulConstants.LABEL_ALWAYSSHOW_ALT);
			boolean alwaysShow = false;
			if (always != null && always.equals("1"))
				alwaysShow = true;
			if (PhyloWidget.cfg.hideAllLabels && !alwaysShow)
				return 0;
			float thisRowSize = r.getTextSize() * PhyloWidget.cfg.textScaling * n.bulgeFactor;

			if (PhyloWidget.cfg.showAllLabels) // If showing all labels, don't do the mintext setting.
				return r.getTextSize() * PhyloWidget.cfg.textScaling;

//			thisRowSize = Math.max(thisRowSize, PhyloWidget.cfg.minTextSize);
			if (thisRowSize < PhyloWidget.cfg.minTextSize)
				thisRowSize = PhyloWidget.cfg.minTextSize;
			return thisRowSize;
		}

		static int textColor(PhyloNode n)
		{
			if (r.getTree().isCollapsed(n))
			{
				return PhyloWidget.cfg.getTextColor().brighter(128).getRGB();
			}
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
				dX += nr.render(canvas, n, false, true)[0];
				dX += ir.render(canvas, n, false, true)[0];
			}

			float x = n.getX() + r.getNodeRadius() + r.getNormalLineWidth() * 2;
			float y = n.getY();
			tf.setTextSize(curTextSize);
			tf.setWidth(textWidth);
			tf.setPositionByBaseline(x + dX, y + dY);
		}
	}

	public static class CigarRender extends RenderItem
	{
		static Pattern p = Pattern.compile("(\\d*?)([MD])", Pattern.CASE_INSENSITIVE);

		public float[] render(PGraphics canvas, PhyloNode n, boolean actuallyRender, boolean preTransformed)
		{
			super.render(canvas, n, actuallyRender, preTransformed);

			boolean alignRight = false;
			if (n.getTextAlign() == PhyloNode.ALIGN_RIGHT)
				alignRight = true;

			String cigarLine = null;
			cigarLine = n.getAnnotation(UsefulConstants.CIGAR);
			if (cigarLine == null)
				return ZEROES;

			AlignmentBlocks abs = parseCigarBreakpoints(cigarLine);
			List<Integer> bps = abs.blocks;
			int length = abs.totalLength;

			// Calculate how much to scale the aligned blocks.
			float thisRowSize = cigarSizeForNode(r, n);
			float totalWidth = PhyloWidget.cfg.cigarScaling * thisRowSize;
			float widthPerBp = totalWidth / (float) length;

			if (actuallyRender)
			{
				for (int i = 0; i < bps.size() - 1; i += 2)
				{
					int lo = bps.get(i);
					int hi = bps.get(i + 1);

					float loX = lo * widthPerBp;
					float hiX = hi * widthPerBp;

					canvas.noStroke();
					canvas.fill(alignmentColor(n));
					canvas.rect(loX, -thisRowSize / 2, hiX - loX, thisRowSize);
				}
			}
			return new float[] { totalWidth, thisRowSize };
		}

		private AlignmentBlocks parseCigarBreakpoints(String cigarLine)
		{
			// Turn a cigar line into an array of match start and end points.
			Matcher m = p.matcher(cigarLine);
			int count = 0;
			ArrayList<Integer> breakpoints = new ArrayList<Integer>();
			while (m.find())
			{
				String numS = m.group(1);
				int num = 1;
				if (numS.length() > 0)
					num = Integer.parseInt(numS);
				String type = m.group(2);

				if (type.equals("M"))
					breakpoints.add(count);
				for (int i = 0; i < num; i++)
				{
					count++;
				}
				if (type.equals("M"))
					breakpoints.add(count);
			}
			AlignmentBlocks ab = new AlignmentBlocks();
			ab.totalLength = count;
			ab.blocks = breakpoints;
			return ab;
		}

		private float cigarSizeForNode(BasicTreeRenderer r, PhyloNode n)
		{
			float thisRowSize = r.getTextSize();
			if (!PhyloWidget.cfg.showAllLabels)
				thisRowSize = Math.max(thisRowSize, PhyloWidget.cfg.minTextSize);

			// If we find a NHX image size annotation, scale accordingly.
			float iMult = getFloatAnnotation(n, CIGAR_SIZE);
			if (iMult > -1)
				thisRowSize *= iMult;

			return thisRowSize;
		}

		static int alignmentColor(PhyloNode n)
		{
			if (n.isNHX())
			{
				String labelColor = n.getAnnotation(ALIGNMENT_COLOR);
				String tax = n.getAnnotation(TAXON_ID);
				String spec = n.getAnnotation(SPECIES_NAME);
				if (labelColor != null)
				{
					int c = Color.parseColor(labelColor).getRGB();
					return c;
				}
			}
			return PhyloWidget.cfg.getAlignmentColor().getRGB();
		}
	}

	static class AlignmentBlocks
	{
		int totalLength;
		List<Integer> blocks;
	}
}
