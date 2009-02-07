package org.andrewberman.ui.menu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class EnumScroller extends NumberScroller
{
	protected HashMap<Integer, String> numToString;

	public EnumScroller()
	{
		numToString = new HashMap<Integer, String>();
	}
	
	public void setValues(String values)
	{
		String[] tokens = values.split(",");

		int i = 0;
		for (String token : tokens)
		{
			token = token.trim();
			numToString.put(i, token);
			i++;
		}

		setMin(0);
		setMax(i-1);
		setIncrement(1);
		setScrollSpeed(0.1f);
	}

	@Override
	protected float parseValueFromString(Object s)
	{
		if (numToString != null)
		{
			// Go through our hashtable, returning the Integer key for the given string if we find it.
			Set<Entry<Integer, String>> entries = numToString.entrySet();
			for (Entry<Integer, String> e : entries)
			{
				if (e.getValue().equals(s))
					return e.getKey();
			}
		}
		return 0;
	}

	@Override
	protected String getStringValueForNumber(float value)
	{
		if (numToString == null)
			return "";
		
		String numericalValue = super.getStringValueForNumber(value);

		// Use the hashtable to grab the appropriate string for the current value.
		float curValue = value;
		int curInt = (int) curValue;
		
		String myString = numToString.get(curInt);
		if (myString == null)
		{
			System.err.println("Null string from EnumScroller!");
			return "Nothing";
		}
		return myString;
	}

}
