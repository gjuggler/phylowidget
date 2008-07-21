package org.phylowidget.ui;

import org.andrewberman.ui.menu.MenuIO;

public class PhyloMenuIO extends MenuIO
{
	public PhyloMenuIO()
	{
		String phyloPackage = PhyloUI.class.getPackage().getName();
		menuPackages.add(phyloPackage);
	}
	
}
