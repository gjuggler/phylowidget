package org.andrewberman.unsorted;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class DelayedAction
{
	private boolean updating;
	boolean threaded;
	Timer timer;

	public void trigger(int delay)
	{
		if (timer != null)
		{
			timer.stop();
			timer.setInitialDelay(delay);
			timer.restart();
			return;
		} else
		{
			timer = new Timer(delay, new ActionListener()
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
					run();
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
