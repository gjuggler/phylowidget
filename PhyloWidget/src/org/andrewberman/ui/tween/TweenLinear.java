package org.andrewberman.ui.tween;

public final class TweenLinear implements TweenFunction
{

	public static TweenLinear instance;
	
	static {
		instance = new TweenLinear();
	}
	
	public boolean isFinished(float t, float p, float b, float c, float d)
	{
		return (t >= d);
	}
	
	public float easeIn(float t, float p, float b, float c, float d)
	{
		return c*t/d+b;
	}

	public float easeOut(float t, float p, float b, float c, float d)
	{
		return c*t/d+b;
	}

	public float easeInOut(float t, float p, float b, float c, float d)
	{
		return c*t/d+b;
	}

}
