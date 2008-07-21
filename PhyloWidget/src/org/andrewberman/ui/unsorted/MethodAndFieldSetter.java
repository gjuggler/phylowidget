package org.andrewberman.ui.unsorted;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodAndFieldSetter
{

	public static void setMethodsAndFields(Object o,
			Map<String, String> fieldToVal)
	{
		for (String f : fieldToVal.keySet())
		{
			String v = fieldToVal.get(f);
			/*
			 * Set the xxxx(String s) field.
			 */
			try
			{
				setField(o, f, v);
			} catch (Exception e1)
			{
			}
			/*
			 * Call the methods.
			 * First try setXXXX, then just xxxxx
			 */
			for (String s : new String[] { "set" + upperFirst(f), f })
			{
				try
				{
					callMethod(o, s, v);
					break;
				} catch (Exception e)
				{
					try
					{
						callMethod(o, s, Float.parseFloat(v));
						break;
					} catch (Exception e2)
					{
						try
						{
							callMethod(o, s, Boolean.parseBoolean(v));
							break;
						} catch (Exception e3)
						{
							try
							{
								callMethod(o, s, Integer.parseInt(v));
								break;
							} catch (Exception e4)
							{
								continue;
							}
						}
					}
				}
			}
		}
	}

	private static void callMethod(Object o, String method, Object param)
			throws Exception
	{
		Method m = null;
		try
		{
			m = o.getClass().getMethod(method, param.getClass());
			m.invoke(o, param);
		} catch (Exception e)
		{
//			System.err.println(e);
			throw new RuntimeException(e);
		}
	}

	private static void callMethod(Object o, String method, boolean param)
	{
		Method m = null;
		try
		{
			m = o.getClass().getMethod(method, boolean.class);
			m.invoke(o, param);
		} catch (Exception e)
		{
//			System.err.println(e);
			throw new RuntimeException(e);
		}
	}
	
	private static void callMethod(Object o, String method, float param)
	{
		Method m = null;
		try
		{
			m = o.getClass().getMethod(method, float.class);
			m.invoke(o, param);
		} catch (Exception e)
		{
//			System.err.println(e);
			throw new RuntimeException(e);
		}
	}

	private static void callMethod(Object o, String method, int param)
	{
		Method m = null;
		try
		{
			m = o.getClass().getMethod(method, int.class);
			m.invoke(o, param);
		} catch (Exception e)
		{
//			System.err.println(e);
			throw new RuntimeException(e);
		}
	}
	
	public static void setField(Object o, String field, String param)
			throws Exception
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
	}

	static String upperFirst(String s)
	{
		String sub = s.substring(0, 1).toUpperCase();
		return sub + s.substring(1);
	}
}
