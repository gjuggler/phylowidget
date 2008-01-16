package org.andrewberman.sortedlist;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.phylowidget.PhyloWidget;
import org.phylowidget.render.NodeRange;

import processing.core.PApplet;

public class SortedXYRangeList
{

	protected SortedItemList loX = new SortedItemList();
	protected SortedItemList hiX = new SortedItemList();
	protected SortedItemList loY = new SortedItemList();
	protected SortedItemList hiY = new SortedItemList();

	public static final float NO_MARK = Float.MAX_VALUE;

	private SortedItemList[] lists = { loX, hiX, loY, hiY };

	public SortedXYRangeList()
	{
		loX.setSort(ItemI.LO_X, 1);
		hiX.setSort(ItemI.HI_X, 1);
		loY.setSort(ItemI.LO_Y, 1);
		hiY.setSort(ItemI.HI_Y, 1);
	}

	public void update()
	{
		sort();
	}

	public void insert(ItemI range)
	{
		insert(range, true);
	}

	public void insert(ItemI range, boolean sort)
	{
		for (int i = 0; i < lists.length; i++)
		{
			lists[i].insert(range, sort);
		}
	}

	public void clear()
	{
		for (int i = 0; i < lists.length; i++)
		{
			lists[i].clear();
		}
	}

	public void sort()
	{
		for (int i = 0; i < lists.length; i++)
		{
			lists[i].sort();
		}
	}

	/**
	 * Do a "full" sort of the entire list, i.e. do the most efficient list sort
	 * given that it's expected to be almost completely out of order.
	 */
	public void sortFull()
	{
		for (int i = 0; i < lists.length; i++)
		{
			lists[i].sortFull();
		}
	}

	public void print()
	{
		lists[0].printItems();
	}

	public synchronized void getInRange(ArrayList list, Rectangle2D.Float rect)
	{
//		PApplet p = PhyloWidget.p;
		this.getInRange(list, rect.x, rect.x + rect.width, rect.y, rect.y
				+ rect.height);
//		p.stroke(0);
//		p.noFill();
//		p.rect(rect.x, rect.y, rect.width, rect.height);
	}

	protected HashMap hash = new HashMap(1000);

	public synchronized void getInRange(ArrayList list, float left,
			float right, float top, float bottom)
	{
		hash.clear();

		int targetNumMarks = 0;
		if (right != NO_MARK && left != NO_MARK)
		{
			targetNumMarks += 2;
			loX.markBelow(hash, right);
			hiX.markAbove(hash, left);
		}
		if (top != NO_MARK && bottom != NO_MARK)
		{
			targetNumMarks += 2;
			loY.markBelow(hash, bottom);
			hiY.markAbove(hash, top);
		}

		Object[] keys = hash.keySet().toArray();
		int count = 0;
		for (int i = 0; i < keys.length; i++)
		{
			Integer num = (Integer) hash.get(keys[i]);
			if (num == SortedItemList.integers[targetNumMarks - 1])
			{
				list.add(keys[i]);
//				NodeRange r = (NodeRange) keys[i];
//				PhyloWidget.p.rect(r.loX, r.loY, r.hiX - r.loX, r.hiY - r.loY);
				count++;
			}
		}
	}

	/**
	 * Main method, for testing purposes.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		SortedXYRangeList list = new SortedXYRangeList();
		Random random = new Random();
		for (int i = 0; i < 100; i++)
		{
			XYRange range = new XYRange(null, random.nextFloat(), random
					.nextFloat(), random.nextFloat(), random.nextFloat());
			list.insert(range);
		}

		// list.getInRange(0f, .5f, 0f, .5f);
	}

}
