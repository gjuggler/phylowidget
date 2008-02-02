/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.unsorted;

import java.applet.Applet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JSCaller
{

	private Applet app;
	public boolean reflectionWorking = true;
	
	public JSCaller(Applet p)
	{
		app = p;
	}
	
	public void reflectJS(String command,String... args)
	{
		if (!reflectionWorking) return;
		String jsresult = null;
		boolean success = false;
		try
		{
			Method getw = null, eval = null, call = null;
			Object jso = null;
			Class c = Class.forName("netscape.javascript.JSObject");
			Method ms[] = c.getMethods();
			for (int i = 0; i < ms.length; i++)
			{
				if (ms[i].getName().compareTo("getWindow") == 0)
					getw = ms[i];
				else if (ms[i].getName().compareTo("eval") == 0)
					eval = ms[i];
				else if (ms[i].getName().compareTo("call") == 0)
					call = ms[i];
			}
			Object a[] = new Object[1];
			a[0] = app; /* this is the applet */
			jso = getw.invoke(c, a); /* this yields the JSObject */
//			System.out.println(jso);
			Object[] objArr = new Object[2];
			objArr[0] = command;
			objArr[1] = args;
			Object result = call.invoke(jso, objArr);
		} catch (InvocationTargetException ite)
		{
//			ite.printStackTrace();
			reflectionWorking = false;
		} catch (Exception e)
		{
//			e.printStackTrace();
			reflectionWorking = false;
		}
	}
}
