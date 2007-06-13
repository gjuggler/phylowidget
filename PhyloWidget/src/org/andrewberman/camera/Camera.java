package org.andrewberman.camera;

import java.awt.geom.Rectangle2D;

import org.andrewberman.phyloinfo.PhyloWidget;
import org.andrewberman.tween.Tween;
import org.andrewberman.tween.TweenListener;
import org.andrewberman.tween.TweenQuad;

import processing.core.PApplet;

public abstract class Camera extends TweenListener{
	
	  public float x;
	  public float y;
	  public float z;
	  
	  private Tween xTween;
	  private Tween yTween;
	  private Tween zTween;
	  
	  private int FRAMES = 60;
	  
	  public Camera() {
	    x = 0;
	    y = 0;
	    z = 1;
	    
	    xTween = new Tween(this,new TweenQuad(),"inout",0,0,FRAMES,false);
	    yTween = new Tween(this,new TweenQuad(),"inout",0,0,FRAMES,false);
	    zTween = new Tween(this,new TweenQuad(),"out",.1f,.1f,FRAMES,false);
	  }
	  
	  public void centerTo(Rectangle2D.Float rect) {
		  xTween.continueTo((float)rect.getCenterX(), FRAMES);
		  yTween.continueTo((float)rect.getCenterY(), FRAMES);
	  }
	  
	  public void zoomCenterTo(Rectangle2D.Float rect) {
		  float xAspect = (float)rect.width / (float)(getStageWidth());
		  float yAspect = (float)rect.height / (float)(getStageHeight());
		  if (xAspect > yAspect)
		  {
			  zTween.continueTo(1.0f/xAspect, FRAMES);
			  xTween.continueTo((float)rect.getCenterX()/xAspect, FRAMES);
			  yTween.continueTo((float)rect.getCenterY()/xAspect, FRAMES);
		  } else
		  {
			  zTween.continueTo(1/yAspect, FRAMES);
			  yTween.continueTo((float)rect.getCenterY()/yAspect, FRAMES);
			  xTween.continueTo((float)rect.getCenterX()/yAspect, FRAMES);
		  }
	  }
	  
	  public abstract float getStageWidth();
	  public abstract float getStageHeight();
	  
	  public float getTranslationX() {
	    return (float)getStageWidth()/2.0f - (float)x;
	  }
	  public float getTranslationY() {
	    return (float)getStageHeight()/2.0f - (float)y;
	  }
	  public float getScale() {
	    return (float)z;
	  }
	  public void updatePosition() {
		x = xTween.update();
		y = yTween.update();
		z = zTween.update();
	  }
	}

