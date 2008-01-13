package org.andrewberman.ui.menu;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;

import org.phylowidget.PhyloWidget;

import processing.core.PApplet;
import processing.xml.XMLElement;

public class MenuIO
{
	static PApplet app;
	static Object actionObject;

	/**
	 * 
	 * @param p
	 * @param filename
	 * @param actionHolder
	 *            (optional) the object which will contain the action methods
	 *            for this menu set.
	 * @return
	 */
	public static ArrayList loadFromXML(PApplet p, String filename,
			Object actionHolder)
	{
		ArrayList menus = new ArrayList();
		app = p;
		actionObject = actionHolder;
		InputStream in = p.openStream(filename);
		/*
		 * Search depth-first through the XML tree, adding the highest-level
		 * menu elements we can find.
		 */
		Stack s = new Stack();
		try
		{
			s.push(new XMLElement(in));
			while (!s.isEmpty())
			{
				XMLElement curEl = (XMLElement) s.pop();
				if (curEl.getName().equalsIgnoreCase("menu"))
				{
					// If curEl is a menu, parse it and add it to the ArrayList.
					menus.add(menuElement(null, curEl));
				} else
				{
					// If not, keep going through the XML tree and search for
					// more <menu> elements.
					Enumeration en = curEl.enumerateChildren();
					while (en.hasMoreElements())
					{
						s.push(en.nextElement());
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return menus;
	}

	public static MenuItem menuElement(MenuItem parent, XMLElement el)
	{
		MenuItem newItem = null;
		String elName = el.getName();
		String itemName = el.getStringAttribute("name");
		if (el.hasAttribute("type"))
		{
			/*
			 * If this element has the "type" attribute, then we use that to
			 * create a new Menu or MenuItem from scratch.
			 */
			String type = el.getStringAttribute("type");
			newItem = createMenu(type);
			// Set this Menu's name.
			if (itemName != null)
				newItem.setName(itemName);
			else
				newItem.setName("");
		}
		if (elName.equalsIgnoreCase("item"))
		{
			/*
			 * If this is any other element (I expect it to be <item>), then
			 * let's make sure it has a parent Menu or MenuItem:
			 */
			if (parent != null)
			{
				/*
				 * If all is well, then we use the parent item's add() method to
				 * create this new Item element.
				 */
				if (newItem != null)
					newItem = parent.add(newItem);
				else
					newItem = parent.add(itemName);
			} else
			{
				throw new RuntimeException(
						"[MenuIO] XML menu parsing error on "
								+ elName
								+ " element: <item> requires a parent <menu> or <item>!");
			}
		}
		/*
		 * At this point, we have a good "newItem" MenuItem. Now we need to
		 * populate its attributes using a bean-like Reflection scheme. Every
		 */
		Enumeration attrs = el.enumerateAttributeNames();
		while (attrs.hasMoreElements())
		{
			String attr = (String) attrs.nextElement();
			/*
			 * Skip the "name" and "type" attributes -- we already used them to
			 * create this menuitem.
			 */
			if (attr.equalsIgnoreCase("name"))
				continue;
			if (attr.equalsIgnoreCase("type"))
				continue;
			/*
			 * For all other attributes, call the set[Attribute] method of the
			 * MenuItem.
			 */
			setAttribute(newItem, attr, el.getStringAttribute(attr));
		}
		/*
		 * Now, keep the recursion going: go through the current XMLElement's
		 * children and call menuElement() on each one.
		 */
		XMLElement[] els = el.getChildren();
		for (int i = 0; i < els.length; i++)
		{
			XMLElement child = els[i];
			menuElement(newItem, child);
		}
		return newItem;
	}

	static final String menuPackage = Menu.class.getPackage().getName();

	/**
	 * Uses Reflection to create a Menu of the given class type.
	 * 
	 * @param classType
	 *            The desired Menu class to create, either as a simple class
	 *            name (if the class resides within the base Menu package) or as
	 *            the fully-qualified Class name (i.e.
	 *            org.something.SomethingElse).
	 * @return
	 */
	private static MenuItem createMenu(String classType)
	{
		/*
		 * We need to give the complete package name of the desired Class, so we
		 * need to assume that the desired class resides within the base Menu
		 * package.
		 */
		String fullClass = menuPackage + "." + classType;
		Class c = null;
		try
		{
			c = Class.forName(fullClass);
		} catch (ClassNotFoundException e)
		{
			/*
			 * If we couldn't find a class for this name, see if we don't have a
			 * fully-qualified class name already.
			 */
			try
			{
				c = Class.forName(classType);
			} catch (ClassNotFoundException e1)
			{
				e1.printStackTrace();
			}
		}

		Constructor construct;
		try
		{
			construct = c.getConstructor(new Class[] { PApplet.class });
			Object newMenu = construct.newInstance(new Object[] { app });
			return (Menu) newMenu;
		} catch (Exception e)
		{
//			 e.printStackTrace();
			try
			{
				construct = c.getConstructor(new Class[] {});
				Object newMenu = construct.newInstance(new Object[] {});
				return (MenuItem) newMenu;
			} catch (Exception e2)
			{
				e2.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Use reflection to call the setXXX function for the given attribute and
	 * value.
	 * 
	 * All setXXX methods should just take a String argument, except for the
	 * defined exceptions.
	 * 
	 * @param item
	 * @param attr
	 * @param value
	 */
	private static void setAttribute(MenuItem item, String attr, String value)
	{
		attr = attr.toLowerCase();
		String upperFirst = "set" + upperFirst(attr);
		try
		{
			Class[] argC = null;
			Object[] args = null;
			if (attr.equalsIgnoreCase("action"))
			{
				/*
				 * If this attribute is an Action, then we need to include a
				 * reference to our actionObject so the correct method can be
				 * called by this menuItem's action.
				 */
				argC = new Class[] { Object.class, String.class };
				args = new Object[] { actionObject, value };
			} else if (attr.equalsIgnoreCase("tool"))
			{
				/*
				 * If this attribute is a Tool, then we need to set the first
				 * letter to upper-case.
				 */
				argC = new Class[] { String.class };
				args = new Object[] { upperFirst(value) };
			} else if (attr.equalsIgnoreCase("property"))
			{
				/*
				 * If this is a setProperty command, include a reference to the
				 * actionObject.
				 */
				argC = new Class[] { Object.class, String.class };
				args = new Object[] { actionObject, value };
			} else
			{
				/*
				 * EVERYTHING ELSE: we simply call the setXXX(value) method
				 * using Java's reflection API.
				 */
				argC = new Class[] { String.class };
				args = new Object[] { value };
			}
			Method curMethod = null;
			try
			{
				/*
				 * First, try it with the straight String parameter.
				 */
				Method[] methods = item.getClass().getMethods();
				for (int i = 0; i < methods.length; i++)
				{
					if (methods[i].getName().equalsIgnoreCase(upperFirst))
					{
						curMethod = methods[i];
						curMethod.invoke(item, args);
						break;
					}
				}
			} catch (Exception e)
			{
				/*
				 * If the String didn't work, try parsing the String to a float.
				 */
				curMethod.invoke(item, new Object[] { new Float(value) });
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String upperFirst(String s)
	{
		String upper = s.substring(0, 1).toUpperCase();
		String orig = s.substring(1, s.length());
		return upper + orig;
	}
}
