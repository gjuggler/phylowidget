package org.andrewberman.util;

import java.util.HashMap;
import java.util.Random;

public final class SortedItemList
{	
	public static final int STARTING_SIZE = 10;
	public static final Integer[] integers =
		{new Integer(0),new Integer(1),new Integer(2),new Integer(3)};
	
	/**
	 * An array of items. Only holds items up to maxIndex.
	 */
	protected ItemI[] items;
	
	/**
	 * The maximum index
	 */
	protected int maxIndex;
	
	/**
	 * These variables store the sorting state of this list.
	 * What = the integer value to be passed to ItemI.get(int what).
	 * Mult = the value to multiply by when sorting. Default to 1, set to -1 for
	 * reverse sorting. 
	 */
	protected int what = 0;
	protected int mult = 1;
	
	public SortedItemList()
	{
		this(new ItemI[STARTING_SIZE]);
		this.maxIndex = -1;
	}
	
	public SortedItemList(ItemI[] ranges)
	{
		this.items = ranges;
		// Assume this array is full, so set the maxIndex accordingly.
		this.maxIndex = ranges.length - 1;
	}
	
	public void printItems()
	{
		for (int i=0; i <= maxIndex; i++)
		{
			System.out.println(items[i]);
		}
	}
	
	public void insert(ItemI item)
	{
		this.insert(item, true);
	}
	
	public synchronized void insert(ItemI item, boolean resort)
	{
		maxIndex++;
		if (maxIndex >= items.length)
		{
			ItemI[] newArr = new ItemI[items.length << 2];
			System.arraycopy(items, 0, newArr, 0, items.length);
			items = newArr;
			System.out.println("Resizing to "+items.length);
		}
		items[maxIndex] = item;
		if (resort)
			sort();
	}
	
	public synchronized void delete(ItemI item)
	{
		// Find the desired item within the array.
		ItemI temp;
		int i;
		for (i=0; i <= maxIndex; i++)
		{
			temp = items[i];
			if (temp == item)
				break;
		}
		
		// Switch the current item with the last, decrement maxIndex,
		// and make the deleted array entry null.
		items[i] = items[maxIndex];
		items[maxIndex] = null;
		maxIndex--;
		sort();
	}
	
	/**
	 * Creates a new array and resets the maxIndex.
	 */
	public void clear()
	{
		items = new ItemI[STARTING_SIZE];
		maxIndex = -1;
	}
	
	public void markBelow(HashMap map, float target)
	{
		int indexLo = 0;
		int indexHi = searchLeft(target);
		
		markInRange(map,indexLo,indexHi);
	}
	
	public void markAbove(HashMap map, float target)
	{
		int indexLo = searchRight(target);
		int indexHi = maxIndex;
		
		markInRange(map,indexLo,indexHi);
	}
	
	/**
	 * 
	 * @param map
	 * @param lo
	 * @param hi
	 */
	public void markInRange(HashMap map, int indexLo, int indexHi)
	{

		Integer tick;
		for (int i=indexLo; i <= indexHi; i++)
		{
			if (map.containsKey(items[i]))
			{
				tick = (Integer)map.get(items[i]);
				map.put(items[i],integers[tick.intValue()+1]);
			} else
			{
				map.put(items[i],integers[0]);
			}
		}
	}
	
	public int searchRight(float target)
	{
		for (int i = 0; i <= maxIndex; i++)
		{
			if (items[i].get(what)*mult > target*mult)
				return i;
		}
		return maxIndex;
	}
	
	public int searchLeft(float target)
	{
		for (int i = maxIndex; i >= 0; i--)
		{
			if (items[i].get(what)*mult < target*mult)
				return i;
		}
		return 0;
	}
	
	public void setSort(int what, int mult)
	{
		this.what = what;
		this.mult = mult;
	}
	
	public void sort()
	{
		this.insertionSort(0, maxIndex, what, mult);
	}
	
	public void quickSort()
	{
		quickSort(0,maxIndex);
	}
	
	public void swap(int x, int y)
	{
		ItemI swap = items[x];
		items[x] = items[y];
		items[y] = swap;
	}
	/**
	 * 
	 */
	public void quickSort(int left, int right)
	{
		if (left < right)
		{
			int h = (right - left) / 2 + left;
			int l, r;
			float value = items[left].get(what)*mult;
			
			for (l = left + 1, r = right; l <= r;)
			{
				if (items[l].get(what)*mult < value)
					l++;
				else if (items[r].get(what)*mult >= value)
					r--;
				else
				{
					swap(l,r);
					l++;
					r--;
				}
			}
			swap(left, l - 1);
			quickSort(left, l - 2);
			quickSort(l, right);
		}
	}
	
	/**
	 * A recursive insertionsort implementation. Algorithm taken largely from
	 * Matthew Caryl's collision detection test.
	 * http://www.permutationcity.co.uk/programming/collisioncode/ClosestPairTest.java
	 * 
	 * @param items Our array of Range objects.
	 * @param lo
	 * @param hi
	 * @param multiplier either 1 or -1, depending on whether we're sorting in forward or reverse.
	 */
	private void insertionSort(int left, int right, int what, int mult)
	{
		if (left < right)
		{
			int h = (right - left) / 2 + left;
			
			// Sort on both sides of the middle.
			insertionSort(left, h, what, mult);
			insertionSort(h+1, right, what, mult);
			
			// ensure largest value is rightmost.
			if (items[h].get(what)*mult > items[right].get(what)*mult)
			{
				ItemI swap = items[h];
				for (int i=h; i < right; i++)
					items[i] = items[i+1];
				items[right] = swap;
				h--;
				if (h < left)
					return;
			}
			
			// ensure smallest value is leftmost.
			if (items[h+1].get(what)*mult < items[left].get(what)*mult)
			{
				ItemI swap = items[h+1];
				for (int i= h + 1; i > left; i--)
					items[i] = items[i-1];
				items[left] = swap;
				h++;
				if (h > right)
					return;
			}
			
			// reorder any problems in the middle
			for (int l = h; items[l].get(what)*mult > items[l+1].get(what)*mult; l--)
			{
				int i;
				ItemI swap = items[l];
				items[l] = items[l+1];
				for (i = l + 2; swap.get(what)*mult > items[i].get(what)*mult; i++)
					items[i - 1] = items[i];
				items[i - 1] = swap;
			}
		}
	}
	
	/**
	 * A main class, for testing purposes.
	 * @param args
	 */
	public static void main(String[] args)
	{
		Random random = new Random();
	
		SortedItemList list = new SortedItemList();
		list.setSort(0,1);
		list.sort();
		
		for (int i=0; i < 2000; i++)
		{
			list.insert(new Item(random.nextInt(2000)));
		}
		list.printItems();
	}	
}