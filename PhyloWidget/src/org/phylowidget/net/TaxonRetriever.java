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
package org.phylowidget.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TaxonRetriever
{

	static Pattern taxonPattern = Pattern.compile("<span.*?>(.*?)</span>");
	
	public static String[] getHints(String query)
	{
		try
		{
			URL url = new URL("http://www.ebi.ac.uk/integr8/OrganismSearch.do?action=orgNames&orgName="+query);
			BufferedReader in = new BufferedReader( new InputStreamReader(url.openStream()));
			String inputLine;
			String dest = new String();
			while ((inputLine = in.readLine()) != null)
			    dest = dest.concat(inputLine);
			in.close();
			
			/*
			 * Now, we parse the html and return the string array.
			 */
			Matcher m = taxonPattern.matcher(dest);
			ArrayList<String> strings = new ArrayList<String>();
			while (m.find())
			{
//				System.out.println(m.group(1));
				strings.add(m.group(1));
			}
			
			return strings.toArray(new String[]{});
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return new String[0];
	}
	
}
