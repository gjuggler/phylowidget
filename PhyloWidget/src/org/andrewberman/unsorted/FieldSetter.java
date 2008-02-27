package org.andrewberman.unsorted;

import java.lang.reflect.Field;
import java.util.HashMap;

public class FieldSetter
{

	public static void setFields(Object o, HashMap<String,String> fieldToVal)
	{
		for (String k : fieldToVal.keySet())
		{
			String v = fieldToVal.get(k);
			setField(o,k,v);
		}
	}
	
	
	private static void setField(Object o, String field, String param)
	{
		try
		{
			Field f = o.getClass().getField(field);
			Class<?> c = f.getType();
			if (c == String.class)
				f.set(o, param);
			else if (c == Boolean.TYPE)
				f.setBoolean(o, Boolean.parseBoolean(param));
			else if (c == Float.TYPE)
				f.setFloat(o, Float.parseFloat(param));
			else if (c == Integer.TYPE)
				f.setInt(o, Integer.parseInt(param));
			else if (c == Double.TYPE)
				f.setDouble(o, Double.parseDouble(param));
		} catch (Exception e)
		{
//			e.printStackTrace();
			return;
		}
	}
}
