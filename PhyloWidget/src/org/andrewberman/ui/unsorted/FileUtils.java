package org.andrewberman.ui.unsorted;

import java.io.File;
import java.io.FileReader;

public class FileUtils
{

	public static String getFileExtension(File f)
	{
		int dotIndex = f.getAbsolutePath().lastIndexOf(".");
		if (dotIndex == -1)
			return "";
		String extension = f.getAbsolutePath().substring(dotIndex+1);
		return extension;
	}
	
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
