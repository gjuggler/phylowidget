package org.andrewberman.ui.tween;

public interface TweenFunction {
	  public float easeIn(float t, float p, float b, float c, float d);
	  public float easeOut(float t, float p, float b, float c, float d);
	  public float easeInOut(float t, float p, float b, float c, float d);
	  public boolean isFinished(float t, float p, float b, float c, float d);
}
