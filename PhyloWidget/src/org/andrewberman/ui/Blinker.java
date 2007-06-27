package org.andrewberman.ui;

import java.util.ArrayList;


public class Blinker extends Thread
{
	public static Blinker instance;
	
	static {
		new Blinker();
	}
	
	private int delay;
	public boolean isOn;
	public boolean interrupted;
	
	private ArrayList listeners = new ArrayList(1);
	
	private Blinker()
	{
		this(750);
	}

	public Blinker(int delay)
	{
		this.delay = delay;
		this.isOn = true;
		this.interrupted = false;
		this.start();
		Blinker.instance = this;
	}

	public void run()
	{
		while (this.isAlive())
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
				e.printStackTrace();
				return;
			}
		}
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