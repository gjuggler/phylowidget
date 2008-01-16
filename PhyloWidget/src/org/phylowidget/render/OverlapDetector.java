package org.phylowidget.render;

import java.util.ArrayList;
import java.util.Collections;

public class OverlapDetector
{

	private ArrayList<Range> ranges = new ArrayList<Range>();

	public OverlapDetector()
	{
	}

	public void clear()
	{
		ranges.clear();
	}

	public void insert(double lo, double hi)
	{
		insert(new Range(lo, hi));
	}

	public boolean overlaps(double lo, double hi)
	{
		return overlaps(new Range(lo,hi));
	}
	
	public boolean overlaps(Range r)
	{
		for (int i = 0; i < ranges.size(); i++)
		{
			Range r2 = ranges.get(i);
			if (r.intersects(r2))
				return true;
		}
		return false;
	}

	public void insert(Range r)
	{
		boolean hitSomething = false;
		for (int i = 0; i < ranges.size(); i++)
		{
			Range r2 = ranges.get(i);
			if (r.intersects(r2))
			{
				hitSomething = true;
				r.absorbRange(r2);
				ranges.remove(i);
				i--;
			} else if (hitSomething)
			{
				/*
				 * If we already hit something and now don't intersect, then we must be finished.
				 */
				break;
			}
		}
		ranges.add(r);
		Collections.sort(ranges);
	}

	@Override
	public String toString()
	{
		return ranges.toString();
	}

	public class Range implements Comparable
	{

		double min;
		double max;

		public Range(double min, double max)
		{
			this.min = min;
			this.max = max;
		}

		void absorbRange(Range r)
		{
			setMin(Math.min(r.getMin(), getMin()));
			setMax(Math.max(r.getMax(), getMax()));
		}

		boolean intersects(Range r)
		{
			if (getMin() > r.getMax())
				return false;
			else if (getMax() < r.getMin())
				return false;
			else if (getMin() <= r.getMax() && getMin() >= r.getMin())
				return true;
			else if (getMax() >= r.getMin() && getMax() <= r.getMax())
				return true;
			else if (getMin() <= r.getMin() && getMax() >= r.getMax())
				return true;
			else
				return false;
		}

		boolean absorbIfIntersects(Range r)
		{
			if (intersects(r))
			{
				absorbRange(r);
				return true;
			} else
				return false;
		}

		public double getMin()
		{
			return min;
		}

		public void setMin(double min)
		{
			this.min = min;
		}

		public double getMax()
		{
			return max;
		}

		public void setMax(double max)
		{
			this.max = max;
		}

		public int compareTo(Object o)
		{
			Range r1 = this;
			Range r2 = (Range) o;

			if (r1.getMin() < r2.getMin())
				return -1;
			else if (r1.getMin() == r2.getMin())
				return 0;
			else
				return 1;
		}

		@Override
		public String toString()
		{
			return "min:" + getMin() + " max:" + getMax() + "\n";
		}
	}

	public static void main(String[] args)
	{
		OverlapDetector od = new OverlapDetector();
		od.insert(50, 70);
		od.insert(40, 60);
		od.insert(50, 55);
		System.out.println(od.overlaps(od.new Range(20, 40)));
		System.out.println(od);
	}

}
