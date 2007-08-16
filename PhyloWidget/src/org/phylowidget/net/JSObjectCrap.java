package org.phylowidget.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.phylowidget.PhyloWidget;

public class JSObjectCrap
{

	public static void reflectJS(String command)
	{
		String jsresult = null;
		boolean success = false;
		try {
		  Method getw = null, eval = null;
		  Object jswin = null;
		  Class c =
		    Class.forName("netscape.javascript.JSObject"); /* does it in IE too */
		  Method ms[] = c.getMethods();
		  for (int i = 0; i < ms.length; i++) {
		      if (ms[i].getName().compareTo("getWindow") == 0)
		         getw = ms[i];
		      else if (ms[i].getName().compareTo("eval") == 0)
		         eval = ms[i];
		      }
		  Object a[] = new Object[1];
		  a[0] = PhyloWidget.p;               /* this is the applet */
		  jswin = getw.invoke(c, a); /* this yields the JSObject */
		  a[0] = command;
		  Object result = eval.invoke(jswin, a);
		  if (result instanceof String)
		    jsresult = (String) result;
		  else
		    jsresult = result.toString();
		  success = true;
		  }
		catch (InvocationTargetException ite) {
		  jsresult = "" + ite.getTargetException();
		  }
		catch (Exception e) {
		  jsresult = "" + e;
		  }
		if (success)
		    System.out.println("eval succeeded, result is " + jsresult);
		else
		    System.out.println("eval failed with error " + jsresult);
	}
	
}
