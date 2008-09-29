package org.andrewberman.ui.unsorted;

public class StringPair
{
	public String a;
	public String b;
	
	public StringPair(String a, String b)
	{
		this.a = a;
		this.b = b;
	}
	
	public String toString()
	{
		return a+"\t"+b;
	}
}
