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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIEvent;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloTree;
import org.phylowidget.render.BasicTreeRenderer;
import org.phylowidget.render.NodeRange;
import org.phylowidget.tree.RootedTree;

import processing.core.PApplet;

public class PhyloTextField extends TextField
{
	PWContext context;
	NodeRange curRange;
	String oldValue;

	int editMode;
	static final int LABEL = 0;
	static final int BRANCH_LENGTH = 1;

	public PhyloTextField(PApplet p)
	{
		super(p);
		this.context = PWPlatform.getInstance().getThisAppContext();
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
		context.getPW().setMessage("Enter to commit, Esc to revert.");
		this.editMode = editMode;
		curRange = r;
		RootedTree t = r.render.getTree();
		reset();
		oldValue = null;
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
		selectAll();
		context.focus().setModalFocus(this);
	}

	public void hide()
	{
		super.hide();
		context.focus().removeFromFocus(this);
		context.getPW().setMessage("");
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
			BasicTreeRenderer r = curRange.render;
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
//						e.printStackTrace();
						context.ui().layout();
						return;
					}
			}
			r.layoutTrigger();
			context.ui().updateNodeInfo(r.getTree(), curRange.node);
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
