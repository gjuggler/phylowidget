package org.phylowidget.net;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.ui.PhyloTree;

public class DelayedAction
{
	private boolean updating;
	Timer timer;
	
	public void trigger(int delay)
	{	
		if (timer != null)
		{
			timer.stop();
		}
		timer = new Timer(delay,new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!updating)
				{
					timer = null;
					doUpdate();
				} else
				{
					timer.start();
				}
			}
		});
		timer.setRepeats(false);
		timer.start();
	}
	
	private void doUpdate()
	{
		updating = true;
		run();
		updating = false;
	}
	
	protected void run()
	{
		// Subclasses subclass this and do stuff here.
	}
}
