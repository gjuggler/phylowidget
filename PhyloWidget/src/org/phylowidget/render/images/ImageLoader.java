package org.phylowidget.render.images;

import java.awt.Image;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class ImageLoader implements Runnable
{
	static Hashtable<String, Image> imageMap;
	static Hashtable<String, Integer> loadedImageURLs;
	static Hashtable<String, PhyloNode> urlsToNodes;
	static Thread thread;

	PWContext context;
	
	public ImageLoader()
	{
		context = PWPlatform.getInstance().getThisAppContext();
	}

	boolean loadingImg = false;
	PhyloNode loadingNode = null;
	public synchronized Image getImageForNode(PhyloNode n)
	{
		if (thread == null)
		{
			loadedImageURLs = new Hashtable<String, Integer>();
			imageMap = new Hashtable<String, Image>();
			urlsToNodes = new Hashtable<String,PhyloNode>();
			thread = new Thread(this);
			thread.start();
		}

		String imgS = n.getAnnotation(ImageSearcher.IMG_TAG);
		if (imgS != null)
		{
			addImage(imgS,n);
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
					n.clearAnnotation(ImageSearcher.OLD_IMG_TAG);
				}
				return img;
			}
			context.getPW().setMessage("Loading image...");
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
		Image i = imageMap.get(imageURL);
		if (i != null)
		{
			i.flush();
			i = null;
		}
		imageMap.remove(imageURL);
		loadedImageURLs.remove(imageURL);
	}

	static final Integer integer = new Integer(0);

	synchronized void addImage(String imageURL,PhyloNode n)
	{
//		if (!PhyloWidget.ui.canAccessInternet())
//		{
//			context.getP().setMessage("Image loading failed: requires PhyloWidget full!");
//			return;
//		}
		if (!loadedImageURLs.containsKey(imageURL))
		{
			loadedImageURLs.put(imageURL, integer);
			imagesToLoad.add(imageURL);
			urlsToNodes.put(imageURL,n);
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
					URL url = null;
					try {
						url = new URL(imgS);
					} catch (Exception e) {
						RootedTree t = context.trees().getTree();
						PhyloTree pt = (PhyloTree) t;
						if (pt.getBaseURL().length() > 0)
						{
							imgS = imgS.replaceAll("\"", "");
							File f = new File(pt.getBaseURL(),imgS);
							url = f.toURL();
						}
					}
					
					PApplet p = context.getPW();
					//					InputStream in = p.openStream(imgS);
					//					byte[] bytes = PApplet.loadBytes(in);
					//					if (bytes == null)
					//					{
					//						in.close();
					//						continue;
					//					}
//					Image img = p.getImage(new URL(imgS));
					Image img = ImageIO.read(url);
					context.getPW().setMessage("Finished loading image!");
					//					System.out.println(img);
					//					Image img = Toolkit.getDefaultToolkit().createImage(bytes);
					//					bytes = null;
					imageMap.put(imgS, img);
					
					PhyloNode n = urlsToNodes.get(imgS);
					if (n != null)
					{
						n.setAnnotation("img_a", 0);
					}
					
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
