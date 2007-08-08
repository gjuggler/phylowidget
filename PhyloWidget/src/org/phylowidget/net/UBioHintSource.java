package org.phylowidget.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class UBioHintSource
{
	//http://www.ubio.org/searchHelp.php?q=homo&sid=1
	Getter activeGetter;
	HintListener listener;
	int hintCount;

	Pattern p = Pattern.compile("(?<=\\>)[^<]+");
	
	public UBioHintSource(HintListener listener)
	{
		this.listener = listener;
	}
	
	public void getHints(String query)
	{
		hintCount++;
		Getter get = new Getter(this,query);
		activeGetter = get;
	}
	
	public void parseHints(Getter get, String response)
	{
		if (get.count < hintCount) return;
		String[] tokens = response.split("<br>");
		for (int i=0; i < tokens.length; i++)
		{
			String s = tokens[i];
			System.out.println(s.replaceAll("\\<.*?\\>",""));
		}
	}
	
	class Getter extends Thread
	{
		String query;
		UBioHintSource source;
		int count;
		
		public Getter(UBioHintSource source, String query)
		{
			this.query = query;
			this.source = source;
			this.count = source.hintCount;
			this.start();
		}
		
		public void run()
		{
			String s = "http://www.ubio.org/searchHelp.php?";
			s += "q=" + query;
			s += "&sid=" + Math.round(Math.random()*100);
			try
			{
				URL url = new URL(s);
				URLConnection urlc = url.openConnection();
				InputStream in = urlc.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String inputLine;
				StringBuffer everything = new StringBuffer();
				while ((inputLine = br.readLine()) != null) 
				{
					everything.append(inputLine);
				}
				source.parseHints(this,everything.toString());
		        in.close();
			} catch (Exception e)
			{
				// Silently die.
			}
		}
	}
	
}
