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

	public static ArrayList loadFromXML(PApplet p, String filename)
	{
		ArrayList menus = new ArrayList();
		app = p;
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
		} else if (elName.equalsIgnoreCase("menuitem"))
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
				// System.out.println(attr);
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

		return null;
	}

	static final String menuPackage = Menu.class.getPackage().getName();

	private static Menu createMenu(String classType)
	{
		String fullClass = menuPackage + "." + classType;
		try
		{
			Class c = Class.forName(fullClass);
			Constructor construct = c
					.getConstructor(new Class[] { PApplet.class });
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
		String upperFirst = "set" + attr.substring(0, 1).toUpperCase()
				+ attr.substring(1, attr.length()).toLowerCase();
		try
		{
			/*
			 * Exception: setAction(Object,String)
			 */
			Class[] argC = null;
			Object[] args = null;
			if (attr.equalsIgnoreCase("action"))
			{
				argC = new Class[] {Object.class, String.class};
				args = new Object[] {PhyloWidget.ui, value};
			} else
			{
				argC = new Class[] {String.class};
				args = new Object[] {value};
			}
			Method m = item.getClass().getMethod(upperFirst,
					argC);
			m.invoke(item,args);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
