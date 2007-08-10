package org.phylowidget.ui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.andrewberman.ui.FocusManager;
import org.andrewberman.ui.Point;
import org.andrewberman.ui.TextField;
import org.andrewberman.ui.UIEvent;
import org.phylowidget.net.HintListener;
import org.phylowidget.net.UBioHintSource;
import org.phylowidget.render.NodeRange;

import processing.core.PApplet;

public class PhyloTextField extends TextField implements HintListener
{
	UBioHintSource hints;
	NodeRange curRange;
	String oldName;

	public PhyloTextField(PApplet p)
	{
		super(p);
		hidden = true;
		alwaysAnchorLeft = true;
		hints = new UBioHintSource(this);
	}

	public void draw()
	{
		if (curRange != null)
		{
			curRange.render.positionText(curRange.node, this);
			super.draw();
		}
	}

	protected void startEditing(NodeRange r)
	{
		curRange = r;
		oldName = r.node.getName();
		reset();
		// hide();
		text.replace(0, text.length(), r.node.getName());
		// r.render.positionText(r.node, this);
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
		curRange = null;
	}

	void hideAndReject()
	{
		hide();
		setName(oldName); // Set back to the old name.
		curRange = null;
	}

	void setName(String s)
	{
		synchronized (this)
		{
			curRange.node.setName(s);
			curRange.render.layout();
		}
	}

	public void fireEvent(int id)
	{
		super.fireEvent(id);

		if (id == UIEvent.TEXT_VALUE)
		{
			setName(getText());
			this.layout();
			// curRange.render.layout();
			// hints.getHints(getText());
		}
	}

	public void keyEvent(KeyEvent e)
	{
		super.keyEvent(e);

		switch (e.getKeyCode())
		{
			case (KeyEvent.VK_ESCAPE):
				hideAndReject();
				break;
			case (KeyEvent.VK_ENTER):
				hideAndCommit();
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

	public void createHints(String[] s)
	{
		// TODO Auto-generated method stub

	}

}
