package org.phylowidget;


import org.andrewberman.ui.UIContext;
import org.andrewberman.ui.UIPlatform;

import processing.core.PApplet;

public class PWPlatform extends UIPlatform
{
	private static final PWPlatform INSTANCE = new PWPlatform();
	
	public static PWPlatform getInstance()
	{
		return INSTANCE;
	}

	public PWContext getThisAppContext()
	{
		PWContext pw = (PWContext) super.getThisAppContext();
//		return (PWContext)super.getThisAppContext();
//		System.out.println(pw);
		return pw;
	}
	
	@Override
	public UIContext createNewContext(PApplet app)
	{
//		return super.createNewContext(app);
		return new PWContext((PhyloWidget)app);
	}
	
	@Override
	public synchronized UIContext registerApp(PApplet app)
	{
		UIContext context = super.registerApp(app);
		UIPlatform.getInstance().registerAppWithContext(app,context);
		return context;
	}
	
}
