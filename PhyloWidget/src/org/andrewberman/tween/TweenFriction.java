package org.andrewberman.tween;

public final class TweenFriction implements TweenFunction
{
	float easeSpeed;
	
	public static TweenFriction tween;
	
	static {
		tween = new TweenFriction(.3f);
	}
	
	public static TweenFriction tween(float speed)
	{
		return new TweenFriction(speed);
	}
	
	public TweenFriction(float easeSpeed)
	{
		this.easeSpeed = easeSpeed;
	}
	
	public boolean isFinished(float t, float p, float b, float c, float d)
	{
		return (p >= b + c - .01 && p <= b + c + .01);
	}
	
	public float easeIn(float t, float p, float b, float c, float d)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public float easeInOut(float t, float p, float b, float c, float d)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public float easeOut(float t, float p, float b, float c, float d)
	{
		return p*(1f-easeSpeed)+easeSpeed*(b+c);
	}

}
