package org.andrewberman.ui.menu;


public final class RadialMenuItem //extends MenuItem
{
//	float rLo,rHi,tLo,tHi = 0;
//	
//	float rectX,rectY,rectW,rectH = 0;
//	float textX,textY = 0;
//	float textWidth, textHeight, pad = 0;
//	float hintX,hintY = 0;
//	float fontSize,hintSize = 0;
//	static Area wedge,bufferWedge,mouseWedge;
//	
//	String label;
//	char hint;
//	
//	static StringBuffer sb = new StringBuffer();
//	static Ellipse2D.Float tempCircle = new Ellipse2D.Float(0,0,0,0);
//	static Arc2D.Float tempArc = new Arc2D.Float(Arc2D.PIE);
//	static RoundRectangle2D.Float roundedRect = new RoundRectangle2D.Float(0,0,0,0,0,0);
//	
//	AffineTransform buffTransform = AffineTransform.getTranslateInstance(0,0);
//	AffineTransform mouseTransform = AffineTransform.getTranslateInstance(0,0);
//	
//	public RadialMenuItem(Menu menu, String label, char hint, Object object, String function)
//	{
//		super(menu, object, function);
//		this.label = label;
//		this.hint = hint;
//	}
//
//	public void drawUnder()
//	{
//		float r = menu.radius;
//		roundedRect.setRoundRect(rectX*r, rectY*r, rectW*r, rectH*r,
//				rectW*r/4, rectW*r/4);
//		g2.setPaint(rectColor);
//		g2.fill(roundedRect);
//		g2.setPaint(rectStrokeColor);
//		g2.setStroke(rectStroke);
//		g2.draw(roundedRect);
//		super.drawUnder();
//	}
//	
//	public void draw()
//	{
//		if (this.state == org.andrewberman.ui.menu.HIDDEN) super.draw();
//		
//		float r = radius;
//		
//		pg.fill(0,menu.alpha);
//		pg.textFont();
//		pg.textSize(fontSize*r);
//		pg.text(label,textX*r,textY*r);
//	
//		drawShape();
//		
//		pg.fill(0,alpha);
//		pg.textSize(hintSize*r);
//		pg.text(hint,hintX*r,hintY*r);
//		
//		super.draw();
//	}
//	
//	public void drawShape()
//	{
//		bufferWedge = wedge.createTransformedArea(buffTransform);
//		mouseWedge = wedge.createTransformedArea(mouseTransform);
//		g2.setPaint(stateColors[state]);
//		g2.fill(bufferWedge);
//		g2.setStroke(segmentStroke);
//		g2.setPaint(strokeColor);
//		g2.draw(bufferWedge);
//	}
//	
//	public MenuItem createItem(String s, char c, Object o, String f)
//	{
//		return new RadialMenuItem(s,c,o,f);
//	}
//	
//	public void layout()
//	{
//		this.layout(rLo,rHi,tLo,tHi);
//	}
//	
//	public void layout(float radLo, float radHi, float thLo, float thHi)
//	{
//		this.rLo=radLo;
//		this.rHi=radHi;
//		this.tLo=thLo;
//		this.tHi=thHi;
//		
//		this.layoutText();
//		this.createShapes();
//		
//		float dTheta = thHi - thLo;
//		float thetaStep = dTheta / items.size();
//		for (int i=0; i < items.size(); i++)
//		{
//			RadialMenuItem seg = (RadialMenuItem) items.get(i);
//			float theta = thLo + i*thetaStep;
//			seg.layout(radHi,radHi+(radHi-radLo),theta,theta+thetaStep);
//		}
//	}
//	
//	public float radToDeg(float rad)
//	{
//		return PApplet.degrees(rad);
//	}
//	
//	public void createShapes()
//	{
//		tempCircle.setFrameFromCenter(0,0,rLo,-rLo);
//		tempArc.setFrame(-rHi,-rHi,2*rHi,2*rHi);
//		
//		float degLo = radToDeg(-tLo);
//		float degHi = radToDeg(-tHi);
//		
//		tempArc.setAngleStart(degLo);
//		tempArc.setAngleExtent(degHi-degLo);
//		wedge = new Area(tempArc);
//		Area delete = new Area(tempCircle);
//		wedge.subtract(delete);
//	}
//	
//	public void setColors()
//	{
////		alpha = constrain(alpha,0,255);
//		baseColor = new Color(230,230,240,menu.alpha);
//		stateColors[0] = baseColor;
//		stateColors[1] = Java2DUtils.lightenColor(baseColor,15);
//		stateColors[2] = Java2DUtils.lightenColor(baseColor,-25);
//		strokeColor = new Color(0,0,0,menu.alpha);
//		
//		segmentStroke = new BasicStroke(radius/20,BasicStroke.CAP_ROUND,
//				BasicStroke.JOIN_ROUND,radius/2);
//		rectStroke = segmentStroke;
//		
//		int a = (int)PApplet.constrain(230f*((float)menu.alpha/255f),0,255);
//		rectColor = new Color(255,255,255,a);
//		rectStrokeColor = new Color(0,0,0,a);
//	}
//	
//	protected Color baseColor;
//	protected Color[] stateColors = new Color[4];
//	protected Color strokeColor;
//	protected Stroke segmentStroke;
//	
//	protected Color rectColor;
//	protected Stroke rectStroke;
//	protected Color rectStrokeColor;
//	
//	
//	public void layoutText()
//	{
//		/*
//		 * Calculate the sine and cosine, which we'll need to use often.
//		 */
//		float theta = (tLo + tHi) / 2;
////		System.out.println(theta);
//		float cos = (float) Math.cos(theta);
//		float sin = (float) Math.sin(theta);
//		float outerX = cos*(rHi*1.5f);
//		float outerY = sin*(rHi*1.5f);
//		
//		float unitTextHeight = font.ascent()+font.descent();
//		fontSize = (rHi-rLo)/unitTextHeight * .9f;
//		float descent = font.descent()*fontSize;
//		float ascent = font.ascent()*fontSize;
//		textHeight = fontSize*unitTextHeight;
//		sb.replace(0, sb.length(), label);
//		textWidth = 0;
//		for (int i=0; i < sb.length(); i++)
//		{
//			textWidth += font.width(sb.charAt(i)) * fontSize;
//		}
//		// Calculate the necessary x and y offsets for the text.
//		textX = cos * textWidth/2;
////		if (Math.abs(cos) < 0.25) textX = 0;
//		textX += -textWidth / 2;
//		textX += outerX;
//		textY = sin * (textHeight)/2;
////		float textY = sin * (ascent + descent + 5)/2;
//		textY += -descent + (ascent + descent)/2;
//		textY += outerY;
//		
//		/*
//		 * Set the background rectangle.
//		 */
//		float pad = rHi/5;
//		rectX = textX-pad;
//		rectY = textY-ascent-descent/2-pad;
//		rectW = textWidth+2*pad;
//		rectH = textHeight+2*pad;
//		
//		/*
//		 * Now, let's handle the hint characters.
//		 */
//		float rMid = (rLo + rHi) / 2;
//		float centerX = cos * rMid;
//		float centerY = sin * rMid;
//		
//		hintSize = fontSize * 1.3f;
//		float naturalSize = font.size;
//		float multiplier = hintSize / naturalSize;
//		int i = font.index(hint);
//		float charHeight = font.height[i] * multiplier;
//		float charWidth = font.width[i] * multiplier;
//		float charDesc = (font.height[i] - font.topExtent[i])*multiplier;
//		
//		hintX = centerX - charWidth / 2.0f;
//		hintY = centerY - charDesc + charHeight / 2.0f;		
//	}
//	
//	public boolean containsPoint(Point p)
//	{
//		if (mouseWedge == null) return false;
//		return mouseWedge.contains(tempPt.x,tempPt.y);
//	}
//	
//	public void mouseEvent(MouseEvent e)
//	{
//		tempPt.setLocation(e.getX(),e.getY());
//		ProcessingUtils.screenToModel(tempPt);
//		super.mouseEvent(e);
//		if (containsPoint(tempPt))
//		{
//			changeCursor = true;
//			withinButtons = true;
//		}
//	}
//	
//	public void getRect(Rectangle2D.Float rect, Rectangle2D.Float buff)
//	{
//		if (bufferWedge == null) return;
//		float r = radius;
//		Rectangle2D.union(rect, bufferWedge.getBounds(), rect);
//		buff.x = pg.width/2;
//		buff.y = pg.height/2;
//		buff.x += rectX*r;
//		buff.y += rectY*r;
//		buff.width = rectW*r;
//		buff.height = rectH*r;
//		Rectangle2D.union(rect, buff, rect);
//	}
	
}
