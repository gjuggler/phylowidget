package org.phylowidget.tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import nexus.NexusBlockParser;
import nexus.NexusFileFormat;
import nexus.NexusFileListener;
import nexus.ParseException;

public class TreeIO extends NexusFileListener.Abstract
{

	private static TreeIO instance = new TreeIO();
	
	public static void parseNewickString(String s)
	{
		StringReader sr = new StringReader(s);
//		BufferedReader br = new BufferedReader(sr);
		try
		{
			NexusFileFormat.parseReader(instance, sr);
		} catch (Exception e)
		{
			// Do nothing yet.
		}
	}

	protected void beginFileComment()
	{
		// TODO Auto-generated method stub
		
	}

	protected void blockEnded(NexusBlockParser blockParser)
	{
		// TODO Auto-generated method stub
		
	}

	protected void endFileComment()
	{
		// TODO Auto-generated method stub
		
	}

	protected void fileCommentText(String comment)
	{
		// TODO Auto-generated method stub
		
	}

	public void endFile()
	{
		// TODO Auto-generated method stub
		
	}

	public void startFile()
	{
		// TODO Auto-generated method stub
		
	}
	
}
