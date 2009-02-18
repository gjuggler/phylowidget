package org.phylowidget;

import org.andrewberman.ui.UIContext;
import org.phylowidget.ui.PhyloConfig;
import org.phylowidget.ui.PhyloUI;

public class PWContext extends UIContext
{

	private PhyloConfig config;
	private TreeManager trees;
	private PhyloUI ui;
	
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
