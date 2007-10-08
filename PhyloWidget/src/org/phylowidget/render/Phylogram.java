package org.phylowidget.render;

import processing.core.PApplet;

/**
 * A version of the Cladogram renderer that uses real branch lengths in
 * displaying the phylogeny. Note that most of the actual branch length handling
 * is done from within the most general Cladogram class; this class merely sets
 * the useBranchLengths variable to true.
 * 
 * @author Greg
 * 
 */
public class Phylogram extends Cladogram
{

	public Phylogram()
	{
		super();
	}

	protected void setOptions()
	{
		super.setOptions();
		useBranchLengths = true;
		keepAspectRatio = true;
	}
	
}
