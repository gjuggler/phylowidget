package org.andrewberman.ui;

import java.util.ArrayList;

/**
 * The <code>Blinker</code> object is a simple thread that simply blinks on and off,
 * in a (hopefully) fairly computationally cheap fashion. It is used by the <code>
 * TextField</code> class to draw the caret on the screen.
 * @author Greg
 * @see		org.andrewberman.ui.TextField
 */
public class Blinker extends Thread
{
	public static Blinker instance;
	
	private int delay;
	public boolean isOn;
	public boolean interrupted;
	
	private ArrayList listeners;
	
	private Blinker(int delay)
	{
		this.delay = delay;
		listeners = new ArrayList(1);
		isOn = true;
		interrupted = false;
	}

	/**
	 * Retrieves the current Blinker instance. Multiple TextFields can share the same
	 * Blinker instance, because only one object can ever own the keyboard focus.
	 * @return the current Blinker instance
	 */
	public static void lazyLoad()
	{
		if (instance == null || !instance.isAlive())
		{
			instance = new Blinker(750);
			instance.start();
		}
	}
	
	public void run()
	{
		while (!Thread.currentThread().isInterrupted())
		{
			try
			{
				synchronized (this)
				{
					if (!interrupted)
					{
						setState(!isOn);
					} else
					{
						interrupted = false;
					}
					this.wait(delay);
				}
			} catch (InterruptedException e)
			{
//				e.printStackTrace();
				break;
			}
		}
		yield();
	}

	public synchronized void setState(boolean state)
	{
		if (state == isOn) return;
		else isOn = state;
		notifyListeners(); // Notify listeners of the state change.
	}
	
	public void notifyListeners()
	{
		for (int i=0; i < listeners.size(); i++)
		{
			BlinkListener b = (BlinkListener)listeners.get(i);
			b.onBlink(this);
		}
	}
	
	public void addBlinkListener(BlinkListener b)
	{
		listeners.add(b);
	}
	
	public void removeBlinkListener(BlinkListener b)
	{
		listeners.remove(b);
	}
	
	public synchronized void reset()
	{
		setState(true);
		interrupted = true;
		this.notifyAll();
	}

	interface BlinkListener
	{
		public void onBlink(Blinker b);
	}
	
}