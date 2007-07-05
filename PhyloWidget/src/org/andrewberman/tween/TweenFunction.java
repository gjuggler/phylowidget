package org.andrewberman.tween;

public interface TweenFunction {
	  public float easeIn(float t, float b, float c, float d);
	  public float easeOut(float t, float b, float c, float d);
	  public float easeInOut(float t, float b, float c, float d);
}
