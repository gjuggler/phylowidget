package org.andrewberman.ui;

import java.lang.reflect.Method;

public class Action
{
	public Object o;
	public String s;
	public Method m;
	
	public Action(Object o, String s)
	{
		this.o = o;
		this.s = s;
		loadMethod();
	}
	
	public void loadMethod()
	{
		if (s != null && !s.equals("") && o != null)
		{
			try
			{
				m = o.getClass().getMethod(s, null);
			} catch (SecurityException e)
			{
				e.printStackTrace();
			} catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void performAction()
	{
		try
		{
			m.invoke(o, null);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
}
