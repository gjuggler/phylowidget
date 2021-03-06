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
package org.andrewberman.ui.unsorted;

public class PausibleThread extends Thread
{

	Thread myThread = this;
	int state;

		public static final int RUNNING = 0;
	public static final int PAUSED = 1;

	public void run()
	{
		state = RUNNING;
		notify();
	}

	public synchronized void waitIfPaused()
	{
		try {
		synchronized (this)
		{
			while (state != RUNNING)
			{
				wait();
			}
		}
		} catch(InterruptedException e)
		{
			e.printStackTrace();
//			return;
		}
	}

	public boolean isPaused()
	{
		return (state == PAUSED);
	}
	
	public boolean isStopped()
	{
		return (myThread == null);
	}
	
	public void dieIfStopped() throws Exception
	{
		if (myThread == null)
		{
			throw new Exception();
		}
	}

	public void waitOrExit() throws Exception
	{
		dieIfStopped();
		waitIfPaused();
	}

	public synchronized void pauseThread()
	{
		state = PAUSED;
		notify();
	}

	public synchronized void resumeThread()
	{
		state = RUNNING;
		notify();
	}

	public synchronized void stopThread()
	{
		myThread = null;
		notify();
	}
}
