package org.andrewberman.ui.unsorted;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Json
{

	public static void main(String[] args)
	{
		HashMap<String,Object> hash = new HashMap<String,Object>();
		hash.put("size", "big!");
		
		HashMap<String,String> secondHash = new HashMap<String,String>();
		secondHash.put("pw_always","yes");
		secondHash.put("pw_never","no!");
		hash.put("hash", secondHash);
		
		System.out.println(Json.hashToJson(hash));
	}
	
	public static String hashToJson(Map map)
	{
		return JSONObject.toJSONString(map);
	}
	
	public static Map jsonToHash(String json)
	{
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj = (JSONObject) parser.parse(json);
			return obj;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static void addHashToJson(StringBuffer sb, Map map)
	{
		sb.append("{");
		
		Set<String> keys = map.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext())
		{
			String key = it.next();
			Object val = map.get(key);
			sb.append("\""+key+"\"");
			sb.append(":");
			if (val instanceof Map)
			{
				addHashToJson(sb,(Map)val);
			} else
			{
				String s = val.toString();
				s = s.replaceAll("\"","");
				sb.append("\""+s+"\"");
			}
			if (it.hasNext())
				sb.append(",");
		}
		sb.append("}");
	}
}
