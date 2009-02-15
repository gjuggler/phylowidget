package org.phylowidget.ui;

public interface JavascriptAccessibleMethods
{

	// Returns the tree in Newick format.
	public String getTreeNewick();
	
	// Returns the tree in NHX format.
	public String getTreeNHX();
	
//	public String getTreePhyloXML();
	
	// Returns the total number of nodes in the tree.
	public int getNodeCount();
	
	// Returns the total number of leaves in the tree.
	public int getLeafCount();
	
	// Returns the total tree length, defined as the sum of all branches in the entire tree.
	public double getTotalTreeLength();
	
	// Returns the *max* tree length, defined as the length from the root to the most distant branch.
	public double getMaxTreeLength();
	
	// Returns an array of leaf names, as Strings.
	public String[] getLeafNames();
	
}
