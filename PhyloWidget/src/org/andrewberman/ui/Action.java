package org.andrewberman.ui;

import java.lang.reflect.Method;

/**
 * The <code>Action</code> class provides a simple facility for creating and
 * deploying actions in a UI-type setting. It uses Java's Reflection API to
 * store a method call, which is performed when the <code>performAction()</code>
 * method is called on the Action object.
 * <p>
 * Generally, you probably shouldn't be creating your own Action methods unless
 * you're extending some of the built-in Menu stuff. Instead, an Action should
 * be created via a call to a MenuItem's <code>create()</code> or
 * <code>add()</code> method.
 * <p>
 * Shortcuts (which are a simple facility for keyboard shortcuts) generally
 * contain a reference to an Action object, which is called when the keyboard
 * shortcut is pressed.
 * <p>
 * TODO: Split the current <code>Action</code> class into an interface and a
 * default implementation. Similar to Swing's Action class, an
 * <code>Action</code> object should simply be a guarantee that the object has
 * an <code>actionPerformed</code> method. Then, the default implementation,
 * <code>MethodAction</code>, would have the functionality that is currently
 * within the <code>Action</code> class, and we could create other, new types
 * of actions: <code>ThreadAction</code>, <code>CalculationAction</code>,
 * <code>ListenableAction</code>... I'm out of ideas, but come up with some
 * yourself!
 * 
 * @author Gregory Jordan
 * @see org.andrewberman.ui.MenuItem
 * @see org.andrewberman.ui.Shortcut
 * @see org.andrewberman.ui.menu.MenuItem
 */
public class Action
{
	public Object o;
	public String s;
	public Method m;

	public Action(Object o, String s)
	{
		this.o = o;
		this.s = s;
		loadMethod();
	}

	public void loadMethod()
	{
		if (s != null && !s.equals("") && o != null)
		{
			try
			{
				m = o.getClass().getMethod(s, null);
			} catch (SecurityException e)
			{
				e.printStackTrace();
			} catch (NoSuchMethodException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void performAction()
	{
		try
		{
			m.invoke(o, null);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

}
