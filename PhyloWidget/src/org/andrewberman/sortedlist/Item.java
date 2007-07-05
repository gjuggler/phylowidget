package org.andrewberman.sortedlist;

public final class Item implements ItemI
{
	public float value;
	
	public Item(float value)
	{
		this.value = value;
	}
	
	public float get(int what)
	{
		return value;
	}

}
