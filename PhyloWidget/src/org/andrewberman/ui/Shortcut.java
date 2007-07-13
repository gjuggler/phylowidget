package org.andrewberman.ui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;
import java.util.regex.Pattern;


public class Shortcut
{
	public Action action;
	public int keyMask;
	public int keyCode;
	public String label;
	
	static String control = "(control|ctrl|meta|cmd|command|apple)";
	static String alt = "(alt)";
	static String shift = "(shift|shft)";
	
	static int shortcutMask;
	
	static
	{
		shortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		if (shortcutMask == KeyEvent.CTRL_MASK)
			shortcutMask |= KeyEvent.CTRL_DOWN_MASK;
		else if (shortcutMask == KeyEvent.ALT_MASK)
			shortcutMask |= KeyEvent.ALT_DOWN_MASK;
		else if (shortcutMask == KeyEvent.META_MASK)
			shortcutMask |= KeyEvent.META_DOWN_MASK;
	}
	
	public Shortcut(String s)
	{
		ShortcutManager.instance.add(this);
		parseString(s);
	}
	
	public void parseString(String s)
	{
		s = s.toLowerCase();
		StringTokenizer st = new StringTokenizer(s,"+-. ");
		int modifiers = 0;
		int code = 0;
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (Pattern.matches(control, token))
			{
				modifiers = modifiers | shortcutMask;
			} else if (Pattern.matches(shift, token))
			{
				modifiers = modifiers | KeyEvent.SHIFT_DOWN_MASK;
			} else
			{
//				code = token.charAt(0);
				String keyCodeName = "VK_" + token.toUpperCase();
				try
				{
					code = KeyEvent.class.getField(keyCodeName).getInt(KeyEvent.class);
				} catch (Exception e){
					throw new RuntimeException("Error parsing shortcut text. The offending token: "+token);
				}
			}
		}
		keyMask = modifiers;
		keyCode = code;
		label = new String();
		if (keyMask != 0)
			label += KeyEvent.getModifiersExText(keyMask)+"+";
		if (keyCode != 0)
			label += KeyEvent.getKeyText(keyCode);
//		System.out.println(KeyEvent.getModifiersExText(keyMask));
//		System.out.println(KeyEvent.getKeyText(keyCode));
	}
	
	public void performAction()
	{
		if (action != null)
			action.performAction();
	}
	
}
