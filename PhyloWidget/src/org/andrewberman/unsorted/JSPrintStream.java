package org.andrewberman.unsorted;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JSPrintStream extends OutputStream
{
	
	JSCaller caller = new JSCaller();
	String method;
	String id;
	
	public JSPrintStream(String methodToCall, String objectID)
	{
		method = methodToCall;
		id = objectID;
	}

	@Override
	public void write(int b) throws IOException
	{
		System.out.println("Writing: "+String.valueOf(b));
		caller.reflectJS(method, id, String.valueOf(b));
	}
	
	
	
}
