package org.andrewberman.unsorted;

public class BulgeUtil
{

	public static float bulge(float dist, float bulgeAmount,float bulgeWidth)
	{
		return (float)(1.0 + bulgeAmount
		*Math.exp(-dist * dist / (bulgeWidth * bulgeWidth)));
	}
	
}
