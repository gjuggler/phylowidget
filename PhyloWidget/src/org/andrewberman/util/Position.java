package org.andrewberman.util;

public class Position implements Cloneable
{

	public float x;
	public float y;
	public float z;

	public Position(float x, float y)
	{
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	public Position(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX()
	{
		return x;
	}

	public float getY()
	{
		return y;
	}

	public float getZ()
	{
		return z;
	}

	public Object clone()
	{
		return new Position(x, y, z);
	}

}
