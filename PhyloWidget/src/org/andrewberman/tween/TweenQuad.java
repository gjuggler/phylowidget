package org.andrewberman.tween;

public final class TweenQuad implements TweenFunction
{

	public static TweenQuad tween;
	
	static {
		tween = new TweenQuad();
	}
	
	public boolean isFinished(float t, float p, float b, float c, float d)
	{
		return (t >= d);
	}
	
	public float easeIn(float t, float p, float b, float c, float d)
	{
		return c*(t/=d)*t + b;
	}

	public float easeOut(float t, float p, float b, float c, float d)
	{
		return -c *(t/=d)*(t-2) + b;
	}

	public float easeInOut(float t, float p, float b, float c, float d)
	{
		if ((t/=d/2) < 1) return c/2*t*t + b;
		return -c/2 * ((--t)*(t-2) - 1) + b;
	}

}
