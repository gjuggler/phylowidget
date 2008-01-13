package org.andrewberman.unsorted;

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
