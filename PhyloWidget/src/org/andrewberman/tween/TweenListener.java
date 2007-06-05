package org.andrewberman.tween;

public abstract class TweenListener {
	  
	  public void onMotionFinished(Tween t) {}
	  
	  public void onMotionLooped(Tween t) {}
	  
	  public void onMotionChanged(Tween t){}
	  
	  public void onMotionStarted(Tween t){}
	  
	  public void onMotionStopped(Tween t){}
	  
	  public void onMotionResumed(Tween t){}
	  
	  public void onMotionUpdated(Tween t){}
	  
	}
