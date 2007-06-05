package org.andrewberman.tween;

public class TweenQuad implements TweenFunction {
	  
	  public TweenQuad() {
	    // Nothing to initialize.
	  }
	  
	  public float easeIn(float t, float b, float c, float d) {
	    return c*(t/=d)*t + b;
	  }
	  
	  public float easeOut(float t, float b, float c, float d) {
	    return -c * (t/=d)*(t-2) + b;
	  }
	  
	  public float easeInOut(float t, float b, float c, float d) {
	    if ((t/=d/2) < 1) return c/2*t*t + b;
	    return -c/2 * ((--t)*(t-2) - 1) + b;
	  }
	  
	}
