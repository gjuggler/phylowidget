package org.andrewberman.tween;

import org.andrewberman.phyloinfo.PhyloWidget;

public class Tween {
	  
	  public TweenListener listener;
	  public TweenFunction func;
	    
	  public Object obj;
	  public String tweenProp;
	  public boolean isTweening;
	  public int managerIndex;
	  public int tweenType; // 0 = in, 1 = out, 2 = inout
	  
	  public float position;
	  public float time;
	  public float begin;
	  public float change;
	  public float duration;
	  public boolean useSeconds;

	  public Tween(TweenListener _listen, TweenFunction _func, String _type, float _begin, float _end,
	      float _duration, boolean _useSeconds)
	  {
	    this.listener = _listen;
	    this.func = _func;
	    _type = _type.toLowerCase();
	    if (_type.equals("in")) {
	     tweenType = 0; 
	    } else if (_type.equals("out")) {
	     tweenType = 1; 
	    } else {
	     tweenType = 2; 
	    }
	    this.begin = _begin;
	    this.position = _begin;
	    this.change = _end - _begin;
	    this.useSeconds = _useSeconds;
//	    if (this.useSeconds) {
//	      this.duration = _duration * PhyloWidget.instance.frameRate;
//	    } else {
//		this.duration = _duration;
//	    }
	    this.duration = _duration;
	    this.time = 0;
	    this.isTweening = false;
	    start();
	  }
		
	  public float getTime() {
	    return this.time;
	  }
	  public void setTime(float t) {
	    this.time = t;
	    update();
	  }
		
	  public float getPosition() {
	    return this.position;
	  }
	  public void setPosition(float newPos) {
	    // Do nothing... instead of a new position, set a new time!
	  }
		
	  public float getDuration() {
	    return this.duration;
	  }
	  public void setDuration(float newD) {
	    float oldD = this.duration;
	    // This sets a new duration, but keeps the object's current position the same.
	    // Lets us change the duration while a tween is running.
	    this.time = (this.time / oldD) * newD;
	    update();
	  }
				
	  public float getFinish() {
	    return this.begin + this.change;
	  }
	  public void setFinish(float f) {
	    // TODO: Figure out a way to change the finish without changing anything else.
	  }  
		
	  public void remove() {
//	    TweenManager.getInstance().removeTween(this);
	  }
		
	  public void rewind() {
	    this.time = 0;
	    this.position = this.begin;
	    listener.onMotionUpdated(this);
//	    this.obj[this.tweenProp] = this.position;
	  }
	  public void fforward() {
	    this.time = this.duration;
	    this.position = this.begin + this.change;
	    listener.onMotionUpdated(this);
//	    this.obj[this.tweenProp] = this.position;
	  }
	  public void start() {
	    this.isTweening = true;
	    listener.onMotionStarted(this);
	  }
	  public void stop() {
	    this.isTweening = false;
	    listener.onMotionStopped(this);
	  }
	  public void continueTo(float newF, float newD) {
	    if (newD == -1) newD = this.duration;
	    //FlashConnect3.trace("tweenTo: "+newF + "   " +newD);
	    this.begin = this.position;
	    this.change = newF - this.begin;
	    this.time = 0;
	    this.duration = newD;
	    start();
	  }
	  public void yoyo() {
	    continueTo(this.begin,this.time);
	  }
		
	  public float update() {
	    if (isTweening)
	    {
	      if (this.time > this.duration)
	      {
	        fforward();
	        stop();
	        listener.onMotionFinished(this);
	      } else
	      {
	        this.time ++;
	        switch(tweenType) {
	         case 0:
	           position = func.easeIn(time,begin,change,duration);
	           break; 
	         case 1:
	           position = func.easeOut(time,begin,change,duration);
	           break;
	         case 2:
	           position = func.easeInOut(time,begin,change,duration);
	           break;
	        }
	        listener.onMotionUpdated(this);
	      }
	    }
	    return position;
	  }
	}
