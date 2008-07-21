package org.phylowidget.render;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.andrewberman.ui.TextField;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;

public class LabelRenderer
{
	Graphics2D g2;
	FontMetrics fm;

	boolean fitImagesToSquare;

	public LabelRenderer()
	{
		fitImagesToSquare = true;
	}

	/**
	 * Gets the aspect ratio of this label, defined as the width per unit height.
	 * @param g
	 * @param n
	 * @param height
	 * @return
	 */
	public float getExpectedAspectRatio(BasicTreeRenderer r, PhyloNode n,
			PFont f)
	{
		PGraphicsJava2D g = r.canvas;
		if (g.g2 != g2)
		{
			g2 = g.g2;
			fm = g2.getFontMetrics(f.font);
		}

		float width = 0;
		/*
		 * Get the image width (if it exists).
		 */
		Image img = PhyloWidget.trees.imageLoader.getImageForNode(n);
		if (img != null)
		{
			if (fitImagesToSquare)
			{
				width += 1 + r.style.labelSpacing;
			} else
			{
				float aspectR = (float) img.getWidth(null)
						/ (float) img.getHeight(null);
				width += aspectR;
				width += r.style.labelSpacing;
			}
		}
		/*
		 * Get the label width.
		 */
		n.unitTextWidth = fm.getStringBounds(n.getLabel(), g2).getWidth() * 1.0;
		width += (float) n.unitTextWidth * PhyloWidget.cfg.textScaling;
		width += r.style.labelSpacing; // Spacing is 1/10.

		return width;
	}

	public float[] getSize(BasicTreeRenderer r, PhyloNode n, float rowHeight)
	{
		float width = 0;
		float height = 0;
		/*
		 * Get the size of the image.
		 */
		Image img = PhyloWidget.trees.imageLoader.getImageForNode(n);
		float imgHeight = 0;
		float imgWidth = -1;
		if (img != null)
		{
			float[] size = imageSize(n, rowHeight);
			imgWidth = size[0];
			imgHeight = size[1];
			if (imgWidth > -1)
				width += r.style.labelSpacing*rowHeight;
			if (fitImagesToSquare)
				width += rowHeight;
			else
				width += imgWidth;
		}
		width += r.style.labelSpacing * rowHeight;
		/*
		 * Get the size of the text.
		 */
		float curTextSize = rowHeight * PhyloWidget.cfg.textScaling
				* n.bulgeFactor;
		n.lastTextSize = curTextSize;
		float textWidth = (float) n.unitTextWidth * curTextSize;
		float textHeight = curTextSize;
		width += textWidth;

		height = Math.max(imgHeight, textHeight) * .95f; // Scale the height by a little bit, so we don't confuse the occlusion filter.

		return new float[] { width, height };
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
		scaling *= PhyloWidget.cfg.imageSize;
		float scaledW = imgW * scaling;
		float scaledH = imgH * scaling;
		return new float[] { scaledW, scaledH };
	}

	/*
	 * Renders a given image and returns the width that it's taking up.
	 */
	float renderImage(BasicTreeRenderer r, PhyloNode n, float x, float y,
			float rowHeight, boolean actuallyRender)
	{
		String imgS = n.getAnnotation("img");
		if (imgS == null)
			return 0;

		float[] size = imageSize(n, rowHeight);
		float scaledW = size[0];
		float scaledH = size[1];

		if (actuallyRender)
		{
			Graphics2D g2 = r.canvas.g2;
			PGraphics canvas = r.canvas;
			canvas.pushMatrix();
			canvas.translate(x, y);
			float dx = (rowHeight - scaledW) / 2;
			//		g2.drawImage(img.)
			Image img = PhyloWidget.trees.imageLoader.getImageForNode(n);

			g2.drawImage(img, (int) dx, (int) -scaledH / 2, (int) scaledW,
					(int) scaledH, null);
			//		canvas.image(img, dx, -scaledH / 2, scaledW, scaledH);

			canvas.popMatrix();
		}

		if (fitImagesToSquare)
			return rowHeight + r.style.labelSpacing * rowHeight;
		else
			return scaledW + r.style.labelSpacing * rowHeight;
	}

	public void renderNode(BasicTreeRenderer r, PhyloNode n, float x, float y,
			float effectiveRowHeight)
	{
		x += r.getNodeRadius() + r.getNormalLineWidth() * 2;
		float dX = renderImage(r, n, x, y, effectiveRowHeight,true);
		x += dX;

		PGraphics canvas = r.canvas;
		g2 = r.canvas.g2;
		float curTextSize = effectiveRowHeight * PhyloWidget.cfg.textScaling
				* n.bulgeFactor;

		canvas.strokeWeight(r.strokeForNode(n));
		canvas.fill(r.textColor(n));

		/*
		 * Early exit strategy if text is too small. Don't do this if we're outputting to a file.
		 */
		if (curTextSize < .5f && !RenderOutput.isOutputting)
		{
			return;
		}

		canvas.pushMatrix();
		canvas.translate(x, y);
		if (PhyloWidget.cfg.textRotation != 0)
			canvas.rotate(PApplet.radians(PhyloWidget.cfg.textRotation));

		RootedTree tree = r.tree;
		RenderStyleSet style = r.style;
		if (tree.isLeaf(n) && n.found)
		{
			/*
			 * Draw a background rect.
			 */
			canvas.noStroke();
			canvas.fill(style.foundBackground.getRGB());
			canvas.rect(0, -curTextSize / 2,
					(float) (n.unitTextWidth * curTextSize), curTextSize);
		}

		/*
		 * THIS IS THE MAIN LABEL DRAWING CODE. SO SLEEK, SO SIMPLE!!!
		 */
		canvas.textFont(r.font);
		curTextSize = Math.min(curTextSize, 128);
		canvas.textSize(curTextSize);
		//		canvas.textSize(10);
		if (n.found)
		{
			canvas.fill(style.foundForeground.getRGB());
		}

		if (!tree.isLeaf(n))
		{
			curTextSize *= 0.5f;
			canvas.textSize(curTextSize);
			canvas.fill(PhyloWidget.cfg.getTextColor().brighter(100).getRGB());
			//			curTextSize = rowSize;
			//			canvas.textSize(rowSize);
			canvas.textAlign(canvas.RIGHT, canvas.BOTTOM);
			float s = r.strokeForNode(n);
			canvas.text(n.getLabel(), -r.dotWidth - curTextSize / 3 - s, -s
					- curTextSize / 5);
		} else
		{
			canvas.textAlign(canvas.LEFT, canvas.BASELINE);
			//			g2.setFont(r.font.font.deriveFont(curTextSize));
			//			g2.drawString(n.getLabel(),0,0+r.dFont*curTextSize/r.textSize);
			Point2D.Float zero = new Point2D.Float(0, 0);
			Point2D.Float dst = new Point2D.Float(0, 0);
			try
			{
				g2.getTransform().inverseTransform(zero, dst);
			} catch (NoninvertibleTransformException e)
			{
				e.printStackTrace();
			}
			if (dst.x < -canvas.width)
			{
				;
			} else
			{
				canvas.text(n.getLabel(), 0, 0 + r.dFont * curTextSize
						/ r.textSize);
			}
		}
		canvas.popMatrix();

	}

	public void positionText(BasicTreeRenderer r, PhyloNode n, TextField tf,
			float textSize)
	{
		float imgW = renderImage(r, n, 0, 0, textSize,false);
//		if (imgW > 0)
//			imgW += r.style.labelSpacing*r.rowSize;
		float dX = imgW;
		float curTextSize = textSize * PhyloWidget.cfg.textScaling
				* n.bulgeFactor;

		float dY = r.dFont * curTextSize / r.textSize;

		float textWidth = (float) n.unitTextWidth * curTextSize;

		float x = r.getX(n) + r.getNodeRadius() + r.getNormalLineWidth() * 2;
		float y = r.getY(n);
		tf.setTextSize(curTextSize);
		tf.setWidth(textWidth);
		tf.setPositionByBaseline(x + dX, y + dY);
	}

}
