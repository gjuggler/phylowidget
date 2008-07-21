package org.andrewberman.ui.menu;

import java.awt.Image;
import java.net.URL;


public class RadialLinkItem extends RadialMenuItem
{
	String genericIcon = "links/generic.png";
	String url;

	public RadialLinkItem()
	{
		super();
//		setMinRadius(20);
		iconFile = genericIcon;
	}

	@Override
	protected void loadImage()
	{
		super.loadImage();
		/*
		 * We do this to make sure a web link always has an icon.
		 */
		if (icon == null)
			icon = menu.canvas.loadImage(genericIcon);
	}
	
	@Override
	protected boolean drawingHint()
	{
		return true;
	}
	
	@Override
	public void performAction()
	{
		try
		{
			String url = getUrl();
			URL realURL = new URL(url);
			menu.canvas.getAppletContext().showDocument(realURL,"_new");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		super.performAction();
		
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
	
}
