package org.phylowidget.ui;

public interface TreeActions
{

	public void newTree();
	public void outputTree();
	
	public void flipTree();
	public void sortTree();
	public void removeElbows();
	
	public void phyloView();
	public void cladoView();
	public void diagonalView();
	
	public void zoomToFull();
	
	public void mutateOnce();
	public void mutateFast();
	public void mutateSlow();
	public void stopMutating();
	
}
