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
		// Use the base UIPlatform method to register the current app, creating a new context if necessary.
		UIContext context = super.registerApp(app);
		// Register the created context with the base UIPLatform class.
		UIPlatform.getInstance().registerAppWithContext(app,context);
		return context;
	}
	
}
