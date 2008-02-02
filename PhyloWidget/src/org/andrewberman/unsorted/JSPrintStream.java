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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

public class JSPrintStream extends OutputStream
{

	public static final int BUFFER = 50;
	
	StringWriter wr = new StringWriter();
	JSCaller caller;
	String method;

	public JSPrintStream(Applet app, String methodToCall)
	{
		caller = new JSCaller(app);
		method = methodToCall;
		caller.reflectJS(method, "");
	}

	@Override
	public void flush() throws IOException
	{
		if (caller.reflectionWorking)
			caller.reflectJS(method, wr.toString());
		else
			System.out.print(wr.toString());
		wr.getBuffer().replace(0, wr.getBuffer().length(), "");
	}
	
	@Override
	public void write(int b) throws IOException
	{
		wr.write(b);
		if (wr.getBuffer().length() > BUFFER)
			flush();
//		caller.reflectJS(method, id, (char)b, b));
	}
	

}
