package org.andrewberman.ui.menu;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;

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
	 * @param actionHolder (optional) the object which will contain the action methods for this menu set.
	 * @return
	 */
	public static ArrayList loadFromXML(PApplet p, String filename, Object actionHolder)
	{
		ArrayList menus = new ArrayList();
		app = p;
		actionObject = actionHolder;
		InputStream in = p.openStream(filename);
		try
		{
			XMLElement el = new XMLElement(in);
			Enumeration en = el.enumerateChildren();
			while (en.hasMoreElements())
			{
				XMLElement child = (XMLElement) en.nextElement();
				menus.add(menuElement(null, child));
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
		System.out.println(el);
		String elName = el.getName();
		String itemName = el.getStringAttribute("name");
		if (elName.equalsIgnoreCase("menu"))
		{
			String type = el.getStringAttribute("type");
			newItem = createMenu(type);
			newItem.setName(itemName);
		} else if (elName.equalsIgnoreCase("item"))
		{
			if (parent != null)
			{
				newItem = parent.add(itemName);
			} else
			{
				throw new RuntimeException(
						"XML parsing error: Cannot have a parent-less 'menuitem' element!");
			}
			Enumeration attrs = el.enumerateAttributeNames();
			while (attrs.hasMoreElements())
			{
				String attr = (String) attrs.nextElement();
				/*
				 * Skip the "name" attribute -- we already used it to create
				 * this menuitem.
				 */
				if (attr.equalsIgnoreCase("name"))
					continue;
				/*
				 * For all other attributes, call the set[Attribute] method of
				 * the MenuItem.
				 */
				setAttribute(newItem, attr, el.getStringAttribute(attr));
			}
		}

		if (newItem != null)
		{
			XMLElement[] els = el.getChildren();
			for (int i = 0; i < els.length; i++)
			{
				XMLElement child = els[i];
				menuElement(newItem, child);
			}
		}

		return newItem;
	}

	static final String menuPackage = Menu.class.getPackage().getName();

	private static Menu createMenu(String classType)
	{
		String fullClass = menuPackage + "." + classType;
		Class c = null;
		try
		{
			c = Class.forName(fullClass);
		} catch (ClassNotFoundException e)
		{
			/*
			 * If we couldn't find a class for this name, see if we don't have a
			 * fully-qualified class name.
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
			e.printStackTrace();
			return null;
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
			/*
			 * Exception: setAction(Object,String)
			 */
			if (attr.equalsIgnoreCase("action"))
			{
				argC = new Class[] { Object.class, String.class };
				args = new Object[] { actionObject, value };
			} else if (attr.equalsIgnoreCase("tool"))
			{
				argC = new Class[] { String.class };
				args = new Object[] { upperFirst(value) };
			} else
			{
				argC = new Class[] { String.class };
				args = new Object[] { value };
			}
			Method m = item.getClass().getMethod(upperFirst, argC);
			m.invoke(item, args);
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
