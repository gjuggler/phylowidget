package org.andrewberman.ui;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class UIPlatform
{
	protected static final UIPlatform INSTANCE = new UIPlatform();

	private UIContext mainContext = null;
	private List<UIContext> allContexts = null;
	private UIContext initContext = null;
	private Thread initContextThread = null;

	public static UIPlatform getInstance()
	{
		return INSTANCE;
	}
	
	public UIContext getThisAppContext()
	{
		// In most cases, there will be only one registered App. In that case, this method
		// returns as quickly as possible.
		if (allContexts == null)
		{
//			System.out.println(mainContext);
			return mainContext;
		}

		synchronized (this)
		{
			// Double check inside the lock
			if (allContexts == null)
			{
				return mainContext;
			}

			// Look through all registered apps and find the context for this ThreadGroup
			// TODO: implement as ThreadLocal instead? (This implementation was Java 1.1 compatible)
			ThreadGroup currentThreadGroup = Thread.currentThread().getThreadGroup();
			for (int i = 0; i < allContexts.size(); i++)
			{
				UIContext context = allContexts.get(i);
				ThreadGroup contextThreadGroup = context.getThreadGroup();
				if (contextThreadGroup == currentThreadGroup || contextThreadGroup.parentOf(currentThreadGroup))
				{
					return context;
				}
			}

			if (initContext != null && Thread.currentThread() == initContextThread)
			{
				// We're initializing the context from the system thread
				return initContext;
			}

			throw new Error("No context found for thread");
		}
	}

	private synchronized UIContext getAppContext(PApplet app)
	{
		if (mainContext != null && mainContext.getApplet() == app)
		{
			return mainContext;
		}

		if (allContexts != null)
		{
			for (int i = 0; i < allContexts.size(); i++)
			{
				UIContext context = allContexts.get(i);
				if (context.getApplet() == app)
				{
					return context;
				}
			}
		}

		return null;
	}

	private synchronized int getNumRegisteredApps()
	{
		if (allContexts == null)
		{
			if (mainContext == null)
			{
				return 0;
			} else
			{
				return 1;
			}
		} else
		{
			return allContexts.size();
		}
	}

	public UIContext createNewContext(PApplet app)
	{
		return new UIContext(app);
	}

	public synchronized UIContext registerAppWithContext(PApplet app, UIContext newContext)
	{
		if (mainContext == null)
		{
			mainContext = newContext;
		} else
		{
			if (allContexts == null)
			{
				allContexts = new ArrayList<UIContext>();
				allContexts.add(mainContext);
			}
			allContexts.add(newContext);
		}
		// Setup a temporary context for init
		initContext = newContext;
		initContextThread = Thread.currentThread();
		
		if (!newContext.isInited())
			newContext.init();
		return newContext;
	}
	
	public synchronized UIContext registerApp(PApplet app)
	{
		if (app == null)
		{
			return null;
		}

		UIContext context = getAppContext(app);
		if (context != null)
		{
			return context;
		}
		boolean wasEmpty = (getNumRegisteredApps() == 0);

		UIContext newContext = createNewContext(app);
		
//		initContext = null;
//		initContextThread = null;

		return registerAppWithContext(app, newContext);
	}

	private synchronized boolean isRegistered(PApplet app)
	{
		return (getAppContext(app) != null);
	}

	public synchronized void unregisterApp(PApplet app)
	{
		if (app == null || !isRegistered(app))
		{
			return;
		}

		if (mainContext != null && mainContext.getApplet() == app)
		{
			mainContext.destroy();
			mainContext = null;
		}

		if (allContexts != null)
		{
			for (int i = 0; i < allContexts.size(); i++)
			{
				UIContext context = (UIContext) allContexts.get(i);
				if (context.getApplet() == app)
				{
					context.destroy();
					allContexts.remove(i);
					break;
				}
			}

			if (mainContext == null)
			{
				mainContext = allContexts.get(0);
			}

			if (allContexts.size() == 1)
			{
				allContexts = null;
			}
		}

		if (getNumRegisteredApps() == 0)
		{
			// Nothing to do here.

		}
	}

}
