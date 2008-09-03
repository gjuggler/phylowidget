package org.phylowidget.render.images;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.andrewberman.ui.UIGlobals;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class ImageLoader implements Runnable
{
	static Hashtable<String, Image> imageMap;
	static Hashtable<String, Integer> loadedImageURLs;
	static Thread thread;

	public ImageLoader()
	{
	}

	public Image getImageForNode(PhyloNode n)
	{
		if (thread == null)
		{
			loadedImageURLs = new Hashtable<String, Integer>();
			imageMap = new Hashtable<String, Image>();
			thread = new Thread(this);
			thread.start();
		}

		String imgS = n.getAnnotation(ImageSearcher.IMG_TAG);
		if (imgS != null)
		{
			addImage(imgS);
			Image img = imageMap.get(imgS);
			if (img != null)
			{
				/*
				 * The latest image is here, so unload the old image from the hashtable and the annotation map.
				 */
				String oldImgS = n.getAnnotation(ImageSearcher.OLD_IMG_TAG);
				if (oldImgS != null)
				{
					removeImage(oldImgS);
				}
				n.clearAnnotation(ImageSearcher.OLD_IMG_TAG);
				return img;
			}
			String oldImgS = n.getAnnotation(ImageSearcher.OLD_IMG_TAG);
			if (oldImgS != null)
			{
				img = imageMap.get(oldImgS);
				return img;
			} else
				return null;
		} else
			return null;
	}

	synchronized void removeImage(String imageURL)
	{
		imageMap.remove(imageURL);
		loadedImageURLs.remove(imageURL);
//		System.out.println(imageMap.keySet());
	}

	static final Integer integer = new Integer(0);

	synchronized void addImage(String imageURL)
	{
		if (!PhyloWidget.ui.canAccessInternet())
		{
			PhyloWidget.setMessage("Image loading failed: requires PhyloWidget full!");
			return;
		}
		if (!loadedImageURLs.containsKey(imageURL))
		{
			loadedImageURLs.put(imageURL, integer);
			imagesToLoad.add(imageURL);
			notifyAll();
		}
	}

	Queue<String> imagesToLoad = new LinkedBlockingQueue<String>();

	public void run()
	{
		while (true)
		{
			if (!imagesToLoad.isEmpty())
			{
				String imgS = null;
				try
				{
					imgS = imagesToLoad.remove();
					PApplet p = UIGlobals.g.getP();
//					InputStream in = p.openStream(imgS);
//					byte[] bytes = PApplet.loadBytes(in);
//					if (bytes == null)
//					{
//						in.close();
//						continue;
//					}
					Image img = p.getImage(new URL(imgS));
//					System.out.println(img);
//					Image img = Toolkit.getDefaultToolkit().createImage(bytes);
//					bytes = null;
					imageMap.put(imgS, img);
				} catch (Exception e)
				{
					e.printStackTrace();
					loadedImageURLs.remove(imgS);
				}
			}
			if (imagesToLoad.isEmpty())
			{
				synchronized (this)
				{
					try
					{
						wait();
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	

	
	public void dispose()
	{
		if (imageMap != null)
		{
			imageMap.clear();
		}
		if (imagesToLoad != null)
		{
			imagesToLoad.clear();
		}
		if (loadedImageURLs != null)
		{
			loadedImageURLs.clear();
		}
		thread = null;
	}
	
}
