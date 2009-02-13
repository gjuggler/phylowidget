package org.andrewberman.ui.unsorted;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JavaUtils
{

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
