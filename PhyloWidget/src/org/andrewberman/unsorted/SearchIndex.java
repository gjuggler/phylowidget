package org.andrewberman.unsorted;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sun.corba.se.spi.orb.StringPair;

public class SearchIndex<T>
{
	HashMap<String, ArrayList<T>> map;
	Collection<T> refList;

	int k = 3; // Word size.

	public SearchIndex()
	{
		map = new HashMap<String, ArrayList<T>>();
	}

	public void add(T item)
	{
		insertRemove(item, true);
	}

	private void insertRemove(T item, boolean insertMe)
	{
		String s = item.toString().toLowerCase();
		if (s.length() < k)
		{
			// Special case: string length is less than our word size.
		}
		for (int i = 0; i < s.length(); i++)
		{
			for (int j = 1; j <= k; j++)
			{
				if (i + j > s.length())
					break;
				String sub = s.substring(i, i + j);
				ArrayList<T> list = map.get(sub);
				if (list == null)
				{
					if (insertMe)
					{
						list = new ArrayList<T>();
						map.put(sub, list);
					} else
					{
						continue;
					}
				}
				if (insertMe)
				{
					list.add(item);
				} else
				{
					if (list.size() == 1)
					{
						map.remove(sub);
					} else
						list.remove(item);
				}
			}
		}
	}

	public void remove(T item)
	{
		insertRemove(item, false);
	}

	public Collection<T> search(String query)
	{
		query = query.toLowerCase();
		
		HashSet<T> hits = new HashSet<T>();
		
		int ws = Math.min(query.length(), k);
		for (int i = 0; i < query.length() - ws + 1; i++)
		{
			String sub = query.substring(i, i + ws);
			ArrayList<T> list = map.get(sub);
			if (list != null)
			{
				hits.addAll(list);
			}
		}
		
		ArrayList<T> matches = new ArrayList<T>();
		for (T hit : hits)
		{
			String s = hit.toString().toLowerCase();
			if (s.contains(query))
				matches.add(hit);
		}
		return matches;
	}

	public String toString()
	{
		return map.toString();
	}

	public static void main(String[] args)
	{
		SearchIndex<StrangePair> si = new SearchIndex<StrangePair>();

		si.add(new StrangePair("Hello", "World!"));
		si.add(new StrangePair("Hell no!", "Whirled!"));
		si.add(new StrangePair("Lonely Hello, there!", "Get a life!"));
		StrangePair p = new StrangePair("Greg Rocks!", "You got it.");
		si.add(p);
		System.out.println(si.search("o"));
	}

	static class StrangePair extends StringPair
	{

		public StrangePair(String first, String second)
		{
			super(first, second);
		}

		public String toString()
		{
			return getFirst();
		}
	}
}
