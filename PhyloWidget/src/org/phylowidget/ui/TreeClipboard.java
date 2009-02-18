/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.ui;

import org.andrewberman.ui.AbstractUIObject;
import org.andrewberman.ui.StringClipboard;
import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.PhyloTree;
import org.phylowidget.tree.CachedRootedTree;
import org.phylowidget.tree.DefaultVertex;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

import processing.core.PApplet;

public class TreeClipboard extends AbstractUIObject
{
	String newickString;
	//	String fullNewickString;
	RootedTree origTree;
	PhyloNode origVertex;

	public TreeClipboard(PApplet p)
	{
	}

	public boolean isEmpty()
	{
		if (newickString == null)
			return true;
		else
			return (newickString.length() == 0);
	}

	public static final int CLIPBOARD_UPDATED = 123432;
	
	public void clearClipboard()
	{
		clearTree();
		newickString = "";
		fireEvent(CLIPBOARD_UPDATED);
	}

	void clearTree()
	{
		if (origTree != null)
		{
			setStateRecursive(origTree, (PhyloNode) origTree.getRoot(),
					PhyloNode.NONE);
			origTree = null;
		}
	}
	
	public synchronized void cut(RootedTree tree, PhyloNode cutMe)
	{
		clearClipboard();
		setClip(tree, cutMe);
		setStateRecursive(tree, cutMe, PhyloNode.CUT);
	}

	public synchronized void copy(RootedTree tree, PhyloNode copyMe)
	{
		clearClipboard();
		setClip(tree, copyMe);
		setStateRecursive(tree, copyMe, PhyloNode.COPY);
	}

	public String getClipboardText()
	{
		return newickString;
	}
	
	public void setClip(RootedTree tree, PhyloNode node)
	{
		setStateRecursive(tree,(PhyloNode) tree.getRoot(),PhyloNode.NONE);
		RootedTree clone = tree.cloneSubtree(node);
		newickString = TreeIO.createNHXString(clone);
		origTree = tree;
		origVertex = node;
		fireEvent(CLIPBOARD_UPDATED);
	}
	
	public void setClipFromJS(String newick)
	{
		clearTree();
		newickString = newick;
		origTree = null;
		origVertex = null;
	}

	PhyloTree loadClip()
	{
		if (newickString == null || newickString.length() == 0)
		{
			/*
			 * Try loading a tree from the system clipboard.
			 */
			newickString = StringClipboard.instance.fromClipboard();
		}
		if (newickString == null || newickString.length() == 0)
			throw new Error("Called TreeClipboard.paste() with empty clipboard");
		
		PhyloTree clipTree = new PhyloTree();
		TreeIO.parseNewickString(clipTree, newickString);
		
		if (origTree != null && origVertex != null)
		{
			setPositionRecursive(clipTree, (PhyloNode) clipTree.getRoot(),
					origVertex);
		}
		return clipTree;
	}

	public synchronized void swap(RootedTree destTree, PhyloNode destNode)
	{
		synchronized (destTree)
		{
			/*
			 * If we're swapping within the same tree, then it's easy:
			 */
			if (origTree == destTree && origVertex != null)
			{
				Object p1 = origTree.getParentOf(origVertex);
				Object p2 = origTree.getParentOf(destNode);
				if (p1 != null && p2 != null)
				{
					origTree.removeEdge(p1, origVertex);
					origTree.removeEdge(p2, destNode);
					origTree.addEdge(p1, destNode);
					origTree.addEdge(p2, origVertex);
				}
			} else
			{
				/*
				 * If we're swapping with an "external" clipboard, then it's also easy.
				 */
				PhyloTree clipTree = loadClip();
				setClip(destTree, destNode);
				setClipFromJS(newickString);
				
				Object p1 = destTree.getParentOf(destNode);
				destTree.deleteSubtree(destNode);
				Graphs.addGraph(destTree, clipTree);
				if (p1 == null)
				{
					destTree.setRoot(clipTree.getRoot());
				} else
				{
					destTree.addEdge(p1, clipTree.getRoot());
				}
			}
		}
	}

	public synchronized void paste(CachedRootedTree destTree, PhyloNode destNode)
	{
		// Translate the newick string into a RooteTree.
		PhyloTree tree = loadClip();
		// Add the clone's vertices and edges to the destination tree.
		
		synchronized (destTree)
		{
			destTree.setHoldCalculations(true);
			Graphs.addGraph(destTree, tree);
			// Insert the clone's root vertex into the midpoint above destNode.
			if (destTree.getParentOf(destNode) == null)
			{
				destTree.addEdge(destNode, tree.getRoot());
			} else
			{
				DefaultVertex internalVertex = destTree.createAndAddVertex();
				((PhyloNode) internalVertex).setPosition(origVertex);
				destTree.insertNodeBetween(destTree.getParentOf(destNode),
						destNode, internalVertex);
				destTree.addEdge(internalVertex, tree.getRoot());
			}
			destTree.setHoldCalculations(false);
			destTree.modPlus();
			
			clearCutNodes();
		}
	}

	void clearCutNodes()
	{
		if (origTree != null)
		{
			if (origVertex != null && origVertex.getState() == PhyloNode.CUT)
			{
				origTree.deleteSubtree(origVertex);
				origTree.removeElbowsBelow(origTree.getRoot());
				setStateRecursive(origTree, (PhyloNode) origTree.getRoot(),
						PhyloNode.NONE);
				origVertex.found = false;
				origVertex = null;
				origTree.modPlus();
			}
		}
	}

	void setPositionRecursive(RootedTree tree, PhyloNode base,
			PhyloNode positionToMe)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(tree, base);
		while (bfi.hasNext())
		{
			PhyloNode n = (PhyloNode) bfi.next();
			n.setPosition(positionToMe);
		}
	}

	void setStateRecursive(RootedTree tree, PhyloNode base, int state)
	{
		BreadthFirstIterator bfi = new BreadthFirstIterator(tree, base);
		while (bfi.hasNext())
		{
			PhyloNode n = (PhyloNode) bfi.next();
			n.setState(state);
		}
	}
}
