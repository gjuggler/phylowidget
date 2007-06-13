package org.andrewberman.ui;

public class FocusManager
{

	public static FocusManager instance;
	private Object focusedObject;
	
	static {
		instance = new FocusManager();
	}
	
	private FocusManager()
	{
	}
	
	public void setFocus(Object o)
	{
		focusedObject = o;
	}
	
	public boolean isFocused(Object o)
	{
		return (o == focusedObject);
	}
	
	public Object getFocusedObject()
	{
		return focusedObject;
	}
	
}
