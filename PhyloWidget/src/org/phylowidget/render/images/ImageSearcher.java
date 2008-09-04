package org.phylowidget.render.images;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.phylowidget.PhyloWidget;
import org.phylowidget.UsefulConstants;
import org.phylowidget.tree.PhyloNode;

public class ImageSearcher implements UsefulConstants
{
	PhyloNode node;
	int pos;
	Vector<SingleResult> results;

	public ImageSearcher(PhyloNode n)
	{
		this.node = n;
		results = new Vector<SingleResult>();
		/*
		 * Fetch the first batch of results.
		 */
		pos = 0;
		fetchResults(0);

		//		Random r = new Random();
		//		if (results.size() > 0)
		//		{
		//			int result = r.nextInt(results.size());
		//			skipTo(result);
		//			//			getThumbnailURL();
		////			getImageURL();
		//		}

	}


	public void loadThumbnailURL()
	{
		String imgT = node.getAnnotation(IMG_TAG);
		if (imgT != null && imgT.equals(getThumbnailURL()))
			return;
		node.setAnnotation(OLD_IMG_TAG, node.getAnnotation(IMG_TAG));
		node.setAnnotation(IMG_TAG, getThumbnailURL());
	}

	public String getThumbnailURL()
	{
		if (pos >= results.size())
			return null;
		SingleResult sr = results.get(pos);
		return sr.thumbURL;
	}
	
	public void loadFullImageURL()
	{
		String imgT = node.getAnnotation(IMG_TAG);
		if (imgT != null && imgT.equals(getFullImageURL()))
			return;
		node.setAnnotation(OLD_IMG_TAG, node.getAnnotation(IMG_TAG));
		node.setAnnotation(IMG_TAG, getFullImageURL());
	}
	
	public String getFullImageURL()
	{
		if (pos >= results.size())
			return null;
		SingleResult sr = results.get(pos);
		return sr.imgURL;
	}

	public void skipTo(int n)
	{
		pos = n;
		if (pos > results.size())
		{
			fetchResults(pos);
		}
	}

	public synchronized void next()
	{
		pos += 1;
		if (pos > results.size())
		{
			fetchResults(pos);
		}
	}

	synchronized void fetchResults(int startingPosition)
	{
		ArrayList[] arr = Google.imageSearch(node.getLabel(), startingPosition);
		ArrayList<String> thumbs = arr[0];
		ArrayList<String> urls = arr[1];
		for (int i = 0; i < thumbs.size(); i++)
		{
			SingleResult res = new SingleResult();
			res.thumbURL = thumbs.get(i);
			res.imgURL = urls.get(i);
			results.add(res);
		}
		//		System.out.println(thumbs.size());
	}

	public void clear()
	{
		pos = 0;
		results.clear();
	}

	public class SingleResult
	{
		public String thumbURL;
		public String imgURL;
	}

	static class Google
	{
		public static ArrayList[] imageSearch(String imageQuery,
				int startingIndex)
		{
			/*
			 * Format our query nicely.
			 */
			try
			{
				imageQuery = URLEncoder.encode(imageQuery, "UTF-8");
			} catch (UnsupportedEncodingException e1)
			{
				e1.printStackTrace();
				// WTF
			}

			/*
			 * Create the query string.
			 */
			String queryS = new String();
			queryS += "http://images.google.com/images?gbv=1&start="
					+ startingIndex + "&q=" + imageQuery;
			//			System.out.println(queryS);
			String result = "";

			/*
			 * Connect to Google and grab the response URL.
			 */
			try
			{
				URL query = new URL(queryS);
				HttpURLConnection urlc = (HttpURLConnection) query
						.openConnection();
				urlc.setInstanceFollowRedirects(true);
				urlc.setRequestProperty("User-Agent", "");
				urlc.connect();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						urlc.getInputStream()));
				StringBuffer response = new StringBuffer();
				char[] buffer = new char[1024];
				while (true)
				{
					int charsRead = in.read(buffer);
					if (charsRead == -1)
					{
						break;
					}
					response.append(buffer, 0, charsRead);
				}
				in.close();
				result = response.toString();
				//		        System.out.println(result);
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			/*
			 * Create the output ArrayLists.
			 */
			ArrayList<String> thumbs = new ArrayList<String>();
			ArrayList<String> imgs = new ArrayList<String>();

			/*
			 * Parse the results.
			 */
			Matcher m = imgBlock.matcher(result);
			while (m.find())
			{
				/*
				 * m.group() here contains the entire <a> element of each result.
				 */
				String s = m.group();

				// Get the destination image URL
				Matcher imgM = imgurl.matcher(s);
				imgM.find();
				String url = imgM.group(1);

				// Get the thumbnail URL
				Matcher srcM = imgsrc.matcher(s);
				srcM.find();
				String thumb = srcM.group(1);
				/*
				 * Just re-encode the "=" and ":" characters.
				 */
//				thumb = thumb.replaceAll("=", "");
//				thumb = thumb.replaceAll(":","&colon;");
//				System.out.println(thumb);

				thumbs.add(thumb);
				imgs.add(url);
			}
			return new ArrayList[] { thumbs, imgs };
		}

		static Pattern imgres = Pattern.compile("/imgres?");
		static Pattern imgBlock = RegexUtils.grabUntilClosingElement("a",
				imgres);
		static Pattern imgurl = Pattern.compile("imgurl=(.*?)&");
		static Pattern imgsrc = Pattern.compile("img src=(\\S*?) ");

	}
}
