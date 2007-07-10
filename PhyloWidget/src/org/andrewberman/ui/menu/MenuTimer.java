package org.andrewberman.ui.menu;


public final class MenuTimer extends Thread
{
	public static MenuTimer instance;
	
	static {
		instance = new MenuTimer();
		instance.start();
	}
	
	MenuItem item;
	MenuItem parent;
	MenuItem lastSet;
	static final int delay = 100;
	
	boolean unset;
	
	int tick;
	boolean interrupted;
	
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
						if (parent == null)
							wait();
						else if (unset)
						{
							item.hideAllChildren();
							item = null;
							parent = null;
						} else
						{
							parent.setOpenItem(item);
							parent = null;
							item = null;
						}
					} else
					{
						interrupted = false;
					}
					wait(delay);
//					System.out.println("waiting");
				}
			} catch (InterruptedException e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	
	public void setMenuItem(MenuItem setMe)
	{
		if (setMe == null) return;
		if (setMe == lastSet) return;
//		if (item == setMe) return;
//		System.out.println("Set item:"+setMe);
		item = setMe;
		parent = item.parent;
		lastSet = item;
		unset = false;
		triggerDelay();
	}
	
	public void unsetMenuItem(MenuItem unsetMe)
	{
		if (unsetMe == item || (unsetMe == lastSet))
		{
//			System.out.println("Unset item:"+unsetMe);
//			parent = unsetMe.nearestMenu;
//			parent = unsetMe.parent;
			item = unsetMe;
			lastSet = null;
			unset = true;
			triggerDelay();
		}
	}
	
	public synchronized void triggerDelay()
	{
		interrupted = true;
		notifyAll();
	}
}