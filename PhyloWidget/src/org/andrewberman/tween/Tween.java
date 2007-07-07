package org.andrewberman.tween;


public final class Tween
{
	public TweenListener listener;
	public TweenFunction func;

	public static final int STARTED = 0;
	public static final int UPDATED = 1;
	public static final int STOPPED = 2;
	public static final int FINISHED = 3;
	
	public static final int IN = 0;
	public static final int OUT = 1;
	public static final int INOUT = 2;
	
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

	public Tween(TweenListener _listen, TweenFunction _func, int _type,
			float _begin, float _end, float _duration)
	{
		this.listener = _listen;
		this.func = _func;
		this.tweenType = _type;
		this.begin = _begin;
		this.position = _begin;
		this.change = _end - _begin;
//		this.useSeconds = _useSeconds;
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

	public float getTime()
	{
		return this.time;
	}

	public void setTime(float t)
	{
		this.time = t;
		update();
	}

	public float getPosition()
	{
		return this.position;
	}

	public void setPosition(float newPos)
	{
		// Do nothing... instead of a new position, set a new time!
	}

	public float getDuration()
	{
		return this.duration;
	}

	public void setDuration(float newD)
	{
		float oldD = this.duration;
		// This sets a new duration, but keeps the object's current position the same.
		// Lets us change the duration while a tween is running.
		this.time = (this.time / oldD) * newD;
		update();
	}

	public float getFinish()
	{
		return this.begin + this.change;
	}

	public void setFinish(float f)
	{
		// TODO: Figure out a way to change the finish without changing anything else.
	}

	public void remove()
	{
		//	    TweenManager.getInstance().removeTween(this);
	}

	public void rewind()
	{
		this.time = 0;
		this.position = this.begin;
		listener.tweenEvent(this,UPDATED);
		//	    this.obj[this.tweenProp] = this.position;
	}

	public void fforward()
	{
		this.time = this.duration;
		this.position = getFinish();
		listener.tweenEvent(this, UPDATED);
		//	    this.obj[this.tweenProp] = this.position;
	}

	public void start()
	{
		this.isTweening = true;
		listener.tweenEvent(this, STARTED);
	}

	public void stop()
	{
		this.isTweening = false;
		listener.tweenEvent(this, STOPPED);
	}

	public void continueTo(float newF)
	{
		continueTo(newF, this.duration);
	}

	public void continueTo(float newF, float newD)
	{
		this.begin = this.position;
		this.change = newF - this.begin;
		this.time = 0;
		this.duration = newD;
		start();
	}

	public void restart(float newStart, float newFinish, float newD)
	{
		this.begin = newStart;
		this.change = newFinish - newStart;
		this.time = 0;
		this.duration = newD;
		start();
	}
	
	public void yoyo()
	{
		continueTo(this.begin, this.time);
	}

	public float update()
	{
		if (isTweening)
		{
			if (this.time >= this.duration)
			{
				fforward();
				stop();
				listener.tweenEvent(this, FINISHED);
			} else
			{
				this.time++;
				switch (tweenType)
				{
					case IN:
						position = func.easeIn(time, begin, change, duration);
						break;
					case OUT:
						position = func.easeOut(time, begin, change, duration);
						break;
					case INOUT:
						position = func
								.easeInOut(time, begin, change, duration);
						break;
				}
				listener.tweenEvent(this, UPDATED);
			}
		}
		return position;
	}
}
