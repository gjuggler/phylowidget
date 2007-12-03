package org.phylowidget.ui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIEvent;
import org.phylowidget.render.NodeRange;
import org.phylowidget.render.TreeRenderer;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class PhyloTextField extends TextField
{
	NodeRange curRange;
	String oldValue;

	int editMode;
	static final int LABEL = 0;
	static final int BRANCH_LENGTH = 1;

	public PhyloTextField(PApplet p)
	{
		super(p);
		hidden = true;
		alwaysAnchorLeft = true;
	}

	public void draw()
	{
		if (!hidden)
		{
			curRange.render.positionText(curRange.node, this);
			super.draw();
		}
	}

	protected void startEditing(NodeRange r, int editMode)
	{
		this.editMode = editMode;
		curRange = r;
		RootedTree t = r.render.getTree();
		reset();
		String oldValue = null;
		switch (editMode)
		{
			case (LABEL):
				oldValue = t.getLabel(r.node);
				break;
			case (BRANCH_LENGTH):
				oldValue = String.valueOf(t.getBranchLength(r.node));
				break;
		}
		text.replace(0, text.length(), oldValue);
		show();
		FocusManager.instance.setModalFocus(this);
	}

	public void hide()
	{
		super.hide();
		FocusManager.instance.removeFromFocus(this);
	}

	void hideAndCommit()
	{
		hide();
		RootedTree t = curRange.render.getTree();
		if (t instanceof PhyloTree)
		{
			PhyloTree pt = (PhyloTree) t;
			pt.updateNewick();
		}
	}

	void hideAndReject()
	{
		hide();
		updateValue(oldValue); // Set back to the old name.
	}

	void updateValue(String s)
	{
		synchronized (this)
		{
			TreeRenderer r = curRange.render;
			switch (editMode)
			{
				case (LABEL):
					r.getTree().setLabel(curRange.node, s);
					break;
				case (BRANCH_LENGTH):
					try
					{
						double value = Double.parseDouble(s);
						r.getTree().setBranchLength(curRange.node, value);
					} catch (Exception e)
					{
						e.printStackTrace();
						r.layout();
						return;
					}
			}
			r.layout();
		}
	}

	public void fireEvent(int id)
	{
		super.fireEvent(id);

		if (id == UIEvent.TEXT_VALUE)
		{
			updateValue(getText());
			this.layout();
		}
	}

	public void keyEvent(KeyEvent e)
	{
		super.keyEvent(e);
		if (hidden)
			return;
		switch (e.getKeyCode())
		{
			case (KeyEvent.VK_ESCAPE):
				hideAndReject();
				e.consume();
				break;
			case (KeyEvent.VK_ENTER):
				hideAndCommit();
				e.consume();
				break;
		}
	}

	public void mouseEvent(MouseEvent e, Point screen, Point model)
	{
		super.mouseEvent(e, screen, model);

		if (hidden)
			return;
		if (e.getID() != MouseEvent.MOUSE_PRESSED)
			return;

		Point p1;
		if (useCameraCoordinates)
			p1 = model;
		else
			p1 = screen;

		if (!withinOuterRect(p1))
		{
			hideAndCommit();
		}
	}

}
