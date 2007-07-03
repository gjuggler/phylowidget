package org.andrewberman.util;

import java.util.ArrayList;
import java.util.Random;

import processing.core.PApplet;

public class HitDetectionTest extends PApplet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SortedXYRangeList list = new SortedXYRangeList();
	
	public void setup()
	{
		size(400,400,P3D);
		createRectangles();
	}
	
	XYRange[] ranges;
	Random random = new Random();
	
	public void createRectangles()
	{
		ranges = new XYRange[500];
		for (int i=0; i < ranges.length; i++)
		{
			float w = random.nextFloat()*10;
			float h = random.nextFloat()*10;
			float x = random.nextFloat()*width;
			float y = random.nextFloat()*height;
			XYRange r = new XYRange(null,x,x+w,y,y+h);
			ranges[i] = r;
			list.insert(r,false);
		}
//		System.out.println("begin sort");
//		list.quickSort();
//		System.out.println("end sort!");
	}
	
	public void draw()
	{
		int DISTANCE = 10;
		float loX = mouseX - DISTANCE;
		float hiX = mouseX + DISTANCE;
		float loY = mouseY - DISTANCE;
		float hiY = mouseY + DISTANCE;
		
		background(255);
		stroke(0);
		noFill();
		
		rect(loX,loY,hiX-loX,hiY-loY);
		
		list.sort();
		
//		Object[] keys = list.getIntersectingRanges(loX, hiX, loY, hiY);
//		for (int i=0; i < keys.length; i++)
//		{
//			Integer integer = (Integer)list.hash.get(keys[i]);
//			if (integer.intValue() == 3)
//			{
//				XYRange r = (XYRange)keys[i];
//				rect(r.loX,r.loY,r.hiX-r.loX,r.hiY-r.loY);
//			}
//		}
		
		
		for (int i=0; i < ranges.length; i++)
		{
			XYRange r = ranges[i];
			float dx = random.nextInt(5) - 2;
			float dy = random.nextInt(5) - 2;
			r.loX += dx;
			r.hiX += dx;
			r.loY += dy;
			r.hiY += dy;
		}
	}
	
}
