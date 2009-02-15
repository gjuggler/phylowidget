package org.phylowidget;

import org.andrewberman.ui.UIContext;
import org.phylowidget.net.JSClipUpdater;
import org.phylowidget.net.JSTreeUpdater;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloUI;

import processing.core.PApplet;

public class PWContext extends UIContext
{

	private PhyloConfig config;
	private TreeManager trees;
	private PhyloUI ui;
	
	private JSTreeUpdater treeUpdater;
	private JSClipUpdater clipUpdater;
	
	public PWContext(PhyloWidget p)
	{
		super(p);
	}

	@Override
	public void init()
	{
		super.init();
		
		this.config = new PhyloConfig();
		this.ui = new PhyloUI(getPW());
		this.trees = new TreeManager(getPW());
	}
	
	public PhyloWidget getPW()
	{
		return (PhyloWidget)getApplet();
	}
	
	public TreeManager trees()
	{
		return trees;
	}
	
	public PhyloUI ui()
	{
		return ui;
	}
	
	public PhyloConfig config()
	{
		return config;
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		
		trees.destroy();
		trees = null;
		ui.destroy();
		ui = null;
		config.destroy();
		config = null;
	}
}
