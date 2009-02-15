/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.ui.unsorted;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class JSCaller
{
	private Applet app;
	public boolean reflectionWorking = true;

	Method getWindow = null;
	Method eval = null;
	Method call = null;
	Method setMember = null;
	Method getMember = null;
	Object jsObject = null;

	public JSCaller(Applet app)
	{
		this.app = app;
		try {
			initialize();
		} catch (Exception e)
		{
			return;
		}
	}

	private void initialize()
	{
		/*
		 * If we've already got the jsObject, then we're already initialized.
		 */
		if (!reflectionWorking)
		{
//			throw new RuntimeException("Reflection not working!");
		}
		if (jsObject != null)
			return;

		try
		{
			ClassLoader cl = app.getClass().getClassLoader();
			Class c = cl.loadClass("netscape.javascript.JSObject");
			Method methods[] = c.getMethods();
			for (Method m : methods)
			{
				if (m.getName().compareTo("getWindow") == 0)
					getWindow = m;
				else if (m.getName().compareTo("eval") == 0)
					eval = m;
				else if (m.getName().compareTo("call") == 0)
					call = m;
				else if (m.getName().compareTo("setMember") == 0)
					setMember = m;
				else if (m.getName().compareTo("getMember") == 0)
					getMember = m;
			}
			jsObject = getWindow.invoke(c, app);
			reflectionWorking = true;
		} catch (Exception e)
		{
			reflectionWorking = false;
			throw new RuntimeException("JS reflection failed -- maybe we're not inside a browser?");
		}
	}

	public synchronized void injectJavaScript(String file)
	{
		initialize();
		try
		{
			InputStream in = openStreamRaw(file);
			BufferedReader read = new BufferedReader(new InputStreamReader(in));

			StringBuffer buff = new StringBuffer();
			String s;
			while ((s = read.readLine()) != null)
			{
				buff.append(s.trim());
			}
			eval(buff.toString());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized Object eval(String expression) throws Exception
	{
		initialize();
//		app.getAppletContext().showDocument(new URL("javascript:"+expression));
		Object result = eval.invoke(jsObject, expression);
		return null;
	}

	public synchronized Object call(String methodName, Object... args)
			throws Exception
	{
		return callWithObject(jsObject, methodName, args);
	}

	public synchronized Object call(String methodName) throws Exception
	{
		return callWithObject(jsObject,methodName);
	}
	
	public synchronized Object callWithObject(Object object,String methodName, Object... args) throws Exception
	{
		initialize();
		Object result = call.invoke(object, methodName,args);
		return result;
	}
	
	public synchronized void setMember(String memberName, Object value)
			throws Exception
	{
		initialize();
		setMember.invoke(jsObject,memberName, value);
	}

	public synchronized Object getMember(String memberName) throws Exception
	{
		initialize();
		Object result = getMember.invoke(jsObject,memberName);
		return result;
	}
	
	public synchronized Object getWindow()
	{
		initialize();
		return jsObject;
	}

	private InputStream openStreamRaw(String filename)
	{
		InputStream stream = null;

		if (filename == null)
			return null;

		if (filename.length() == 0)
		{
			// an error will be called by the parent function
			//System.err.println("The filename passed to openStream() was empty.");
			return null;
		}

		// safe to check for this as a url first. this will prevent online
		// access logs from being spammed with GET /sketchfolder/http://blahblah
		try
		{
			URL url = new URL(filename);
			stream = url.openStream();
			return stream;

		} catch (MalformedURLException mfue)
		{
			// not a url, that's fine

		} catch (FileNotFoundException fnfe)
		{
			// Java 1.5 likes to throw this when URL not available. (fix for 0119)
			// http://dev.processing.org/bugs/show_bug.cgi?id=403

		} catch (IOException e)
		{
			// changed for 0117, shouldn't be throwing exception
			e.printStackTrace();
			//System.err.println("Error downloading from URL " + filename);
			return null;
			//throw new RuntimeException("Error downloading from URL " + filename);
		}

		// using getClassLoader() prevents java from converting dots
		// to slashes or requiring a slash at the beginning.
		// (a slash as a prefix means that it'll load from the root of
		// the jar, rather than trying to dig into the package location)
		ClassLoader cl = getClass().getClassLoader();

		// by default, data files are exported to the root path of the jar.
		// (not the data folder) so check there first.
		stream = cl.getResourceAsStream("data/" + filename);
		if (stream != null)
		{
			String cn = stream.getClass().getName();
			// this is an irritation of sun's java plug-in, which will return
			// a non-null stream for an object that doesn't exist. like all good
			// things, this is probably introduced in java 1.5. awesome!
			// http://dev.processing.org/bugs/show_bug.cgi?id=359
			if (!cn.equals("sun.plugin.cache.EmptyInputStream"))
			{
				return stream;
			}
		}

		// when used with an online script, also need to check without the
		// data folder, in case it's not in a subfolder called 'data'
		// http://dev.processing.org/bugs/show_bug.cgi?id=389
		stream = cl.getResourceAsStream(filename);
		if (stream != null)
		{
			String cn = stream.getClass().getName();
			if (!cn.equals("sun.plugin.cache.EmptyInputStream"))
			{
				return stream;
			}
		}
		return stream;
	}
}
