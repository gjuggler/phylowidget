package org.phylowidget.ui;

import org.jgrapht.Graphs;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.phylowidget.tree.RootedTree;

public class TreeClipboard
{
	public static TreeClipboard instance;

	RootedTree clone;
	RootedTree origin;
	PhyloNode origVertex;

	private TreeClipboard()
	{
	}

	public static TreeClipboard instance()
	{
		if (instance == null)
			instance = new TreeClipboard();
		return instance;
	}

	boolean isEmpty()
	{
		return (clone == null);
	}

	void clearClipboard()
	{
		if (clone != null)
		{
			setStateRecursive(origin, (PhyloNode) origin.getRoot(),
					PhyloNode.NONE);
			clone = null;
			origin = null;
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
		clone = tree.cloneSubtree(node);
		origin = tree;
		origVertex = node;
	}

	public synchronized void paste(RootedTree destTree, PhyloNode destNode)
	{
		if (isEmpty())
			throw new Error("Called TreeClipboard.paste() with empty clipboard");
		// Add the clone's vertices and edges to the destination tree.
		Graphs.addGraph(destTree, clone);

		// Insert the clone's root vertex into the midpoint above destNode.
		if (destTree.parentOf(destNode) == null)
		{
			destTree.addEdge(destNode, clone.getRoot());
		} else
		{
			Object internalVertex = destTree.createAndAddVertex("[internal vertex]");
			destTree.insertNodeBetween(destTree.parentOf(destNode), destNode,
					internalVertex);
			destTree.addEdge(internalVertex, clone.getRoot());
		}

		if (origin != null)
		{
			if (origVertex.getState() == PhyloNode.CUT)
			{
				origin.deleteSubtree(origVertex);
				origin.cullElbowsBelow(origin.getRoot());
				setStateRecursive(origin, (PhyloNode) origin.getRoot(),
						PhyloNode.NONE);
				origVertex = null;
			}
		}
		clone = clone.cloneSubtree(clone.getRoot());
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
