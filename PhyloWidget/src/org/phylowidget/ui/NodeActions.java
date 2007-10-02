package org.phylowidget.ui;

public interface NodeActions
{

	public void addChild();
	public void addSister();
	
	public void deleteNode();
	public void deleteSubtree();
	
	public void cutNode();
	public void copyNode();
	public void pasteNode();
	public void clearClipboard();
	
	public void flipSubtree();
	public void switchChildren();
	public void reroot();
	
	public void editName();
	public void editBranchLength();
	
}
