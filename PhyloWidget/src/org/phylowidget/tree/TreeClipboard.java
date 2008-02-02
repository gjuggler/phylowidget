/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.tree;

import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.PhyloWidget;
import org.phylowidget.net.JSClipUpdater;
import org.phylowidget.ui.PhyloNode;
import org.phylowidget.ui.PhyloTree;

public class TreeClipboard
{
	public static TreeClipboard instance;

	String newickString;
	RootedTree origTree;
	PhyloNode origVertex;

	JSClipUpdater updater;

	private TreeClipboard()
	{
		updater = new JSClipUpdater();
	}

	public static TreeClipboard instance()
	{
		if (instance == null)
			instance = new TreeClipboard();
		return instance;
	}

	boolean isEmpty()
	{
		return (newickString.length() == 0);
	}

	public void clearClipboard()
	{
		setClip("");
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

	public synchronized void setClip(RootedTree tree, PhyloNode node)
	{
		RootedTree clone = tree.cloneSubtree(node);
		setClip(TreeIO.createNewickString(clone));
		origTree = tree;
		origVertex = node;
	}

	public synchronized void setClip(String newick)
	{
		newickString = newick;
		updater.triggerUpdate(newickString);
	}

	public synchronized void paste(RootedTree destTree, PhyloNode destNode)
	{
		if (isEmpty())
			throw new Error("Called TreeClipboard.paste() with empty clipboard");
		// Translate the newick string into a RooteTree.
		PhyloTree tree = new PhyloTree();
		TreeIO.parseNewickString(tree, newickString);
		// Add the clone's vertices and edges to the destination tree.
		synchronized (destTree)
		{
			Graphs.addGraph(destTree, tree);
			// Insert the clone's root vertex into the midpoint above destNode.
			if (destTree.getParentOf(destNode) == null)
			{
				destTree.addEdge(destNode, tree.getRoot());
			} else
			{
				Object internalVertex = destTree.createAndAddVertex("");
				destTree.insertNodeBetween(destTree.getParentOf(destNode),
						destNode, internalVertex);
				destTree.addEdge(internalVertex, tree.getRoot());
			}

			if (origTree != null)
			{
				if (origVertex.getState() == PhyloNode.CUT)
				{
					origTree.deleteSubtree(origVertex);
					origTree.cullElbowsBelow(origTree.getRoot());
					setStateRecursive(origTree, (PhyloNode) origTree.getRoot(),
							PhyloNode.NONE);
					origVertex = null;
				}
			}
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
