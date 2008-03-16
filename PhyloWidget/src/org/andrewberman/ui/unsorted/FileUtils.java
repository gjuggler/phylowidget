package org.andrewberman.ui.unsorted;

import java.io.File;
import java.io.FileReader;

public class FileUtils
{

	public static String getFileAsString(String filename) throws Exception
	{
		return getFileAsString(new File(filename));
	}
	
	public static String getFileAsString(File f) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		FileReader fr = new FileReader(f);
		int c;
		while ((c = fr.read()) != -1)
		{
			sb.append((char)c);
		}
		return sb.toString();
	}
}
