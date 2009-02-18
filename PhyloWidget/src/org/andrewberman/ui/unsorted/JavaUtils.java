package org.andrewberman.ui.unsorted;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JavaUtils
{

	public static String join(String delimiter, Collection s)
	{
		StringBuffer buffer = new StringBuffer();
		Iterator iter = s.iterator();
		while (iter.hasNext())
		{
			buffer.append(iter.next().toString());
			if (iter.hasNext())
			{
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}
	
	public static String join(String delimiter, String... array)
	{
		StringBuffer buffer = new StringBuffer();
		int len = array.length;
		for (int i = 0; i < len; i++)
		{
			buffer.append(array[i]);
			if (i < len - 1)
				buffer.append(delimiter);
		}
		return buffer.toString();
	}
	
	public static Map<String, Object> getChangedFields(Object a, Object b)
	{
		Class aClass = a.getClass();
		Class bClass = b.getClass();
		if (!aClass.equals(bClass))
		{
			System.out.println("Classes a and b not equal!");
		}

		HashMap<String, Object> changedFields = new HashMap<String, Object>();

		Field[] fields = aClass.getFields();
		for (Field f : fields)
		{
			try
			{
				if (f.get(a).equals(f.get(b)))
				{
					System.out.println("Equal on field " + f.getName());
				} else
				{
					changedFields.put(f.getName(), f.get(a));
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return changedFields;
	}
	
}
