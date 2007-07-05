package org.andrewberman.tween;

public final class TweenLinear implements TweenFunction
{

	public static TweenLinear instance;
	
	static {
		instance = new TweenLinear();
	}
	
	public float easeIn(float t, float b, float c, float d)
	{
		return c*t/d+b;
	}

	public float easeOut(float t, float b, float c, float d)
	{
		return c*t/d+b;
	}

	public float easeInOut(float t, float b, float c, float d)
	{
		return c*t/d+b;
	}

}
