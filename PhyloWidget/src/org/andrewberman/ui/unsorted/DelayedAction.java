/**************************************************************************
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
package org.andrewberman.ui.unsorted;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class DelayedAction
{
	private boolean updating;
	boolean threaded;
	Timer timer;

	boolean needsNewTimer = true;

	public synchronized void trigger(int delay)
	{
		if (needsNewTimer)
		{
			timer = new Timer(delay, new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (!updating)
					{
						doUpdate();
						needsNewTimer = true;
					} else
					{
						if (timer != null)
						{
							timer.start();
						}
					}
				}
			});
			timer.setRepeats(false);
			timer.start();
			needsNewTimer = false;
		}
		timer.stop();
		timer.setInitialDelay(delay);
		timer.restart();
	}

	private void doUpdate()
	{
		if (threaded)
		{
			new Thread()
			{
				public void run()
				{
					updating = true;
					super.run();
					updating = false;
				}
			}.start();
		} else
		{
			updating = true;
			run();
			updating = false;
		}
	}

	protected void run()
	{
		// Subclasses subclass this and do stuff here.
	}

	public boolean isThreaded()
	{
		return threaded;
	}

	public void setThreaded(boolean threaded)
	{
		this.threaded = threaded;
	}
}
