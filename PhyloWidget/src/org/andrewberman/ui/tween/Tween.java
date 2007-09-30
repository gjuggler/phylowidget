package org.andrewberman.ui.tween;


public class Tween
{
	public TweenListener listener;
	public TweenFunction function;

	public static final int STARTED = 0;
	public static final int UPDATED = 1;
	public static final int STOPPED = 2;
	public static final int FINISHED = 3;
	public static final int REWOUND = 4;
	public static final int FFORWARDED = 5;
	
	public static final int IN = 0;
	public static final int OUT = 1;
	public static final int INOUT = 2;
	
	public boolean isTweening;
	public int tweenType; // use the IN/OUT/INOUT static variables with this.

	public float position;
	public float time;
	public float begin;
	public float change;
	public float duration;

	public Tween(TweenListener listener, TweenFunction function, int type)
	{
		this.listener = listener;
		this.function = function;
		this.tweenType = type;
		this.isTweening = false;
	}
	
	public Tween(TweenListener listener, TweenFunction function, int tweenType,
			float begin, float end, float duration)
	{
		this.listener = listener;
		this.function = function;
		this.tweenType = tweenType;
		this.begin = begin;
		this.position = begin;
		this.change = end - begin;
//		this.useSeconds = _useSeconds;
		//	    if (this.useSeconds) {
		//	      this.duration = _duration * PhyloWidget.instance.frameRate;
		//	    } else {
		//		this.duration = _duration;
		//	    }
		this.duration = duration;
		this.time = 0;
		this.isTweening = false;
		start();
	}

	public float getTime()
	{
		return this.time;
	}

//	public void setTime(float t)
//	{
//		this.time = t;
//		update();
//	}

	public float getPosition()
	{
		return this.position;
	}

	public float getDuration()
	{
		return this.duration;
	}

//	public void setDuration(float newD)
//	{
//		float oldD = this.duration;
//		// This sets a new duration, but keeps the object's current position the same.
//		// Lets us change the duration while a tween is running.
//		this.time = (int) ((this.time / oldD) * newD);
//		update();
//	}

	public float getFinish()
	{
		return this.begin + this.change;
	}

//	public void setFinish(float f)
//	{
//		// TODO: Figure out a way to change the finish without changing anything else.
//	}

	public void rewind()
	{
		this.time = 0;
		this.position = this.begin;
		dispatchEvent(REWOUND);
		//	    this.obj[this.tweenProp] = this.position;
	}

	public void fforward()
	{
		this.time = this.duration;
		this.position = getFinish();
		dispatchEvent(FFORWARDED);
		//	    this.obj[this.tweenProp] = this.position;
	}

	public void start()
	{
		this.isTweening = true;
		dispatchEvent(STARTED);
	}

	public void stop()
	{
		this.isTweening = false;
		dispatchEvent(STOPPED);
	}

	public void continueTo(float newF)
	{
		if (newF == begin + change)
			return;
		if (!isTweening)
			start();
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

	public final void dispatchEvent(int eventType)
	{
		if (listener != null)
			listener.tweenEvent(this, eventType);
	}
	
	public float update()
	{
		if (isTweening)
		{
			if (function.isFinished(time, position, begin, change, duration))
			{
				fforward();
				stop();
				dispatchEvent(FINISHED);
//				System.out.println("STOP!"+listener);
			} else
			{
				this.time++;
				switch (tweenType)
				{
					case IN:
						position = function.easeIn(time, position, begin, change, duration);
						break;
					case OUT:
						position = function.easeOut(time, position, begin, change, duration);
						break;
					case INOUT:
						position = function
								.easeInOut(time, position, begin, change, duration);
						break;
				}
				dispatchEvent(UPDATED);
			}
		}
		return position;
	}
}
