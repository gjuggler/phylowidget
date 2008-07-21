package org.phylowidget.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import processing.xml.XMLElement;

public class ToLConverter
{

	public static void main(String[] args)
	{
		InputStream in = ClassLoader.getSystemResourceAsStream("data/tol.xml");
		
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();
			String s;
			while ((s = br.readLine()) != null)
			{
				sb.append(s+"\n");
			}
			
			String str = sb.toString();
			str = str.replaceAll("CDATA\\[\\]", "CDATA[NULL]");
//			System.out.println(str);
			
			XMLElement el = new XMLElement(new StringReader(str));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
}
