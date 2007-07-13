package org.andrewberman.ui;


public class FocusManager
{	
	private Object focusedObject = null;
	private boolean isModal = false;
	
	public static FocusManager instance = new FocusManager();
	
	public FocusManager(){}
	
	public void setup(){}
	
	public boolean setFocus(Object o)
	{
		if (isModal && !isFocused(o))
		{
			return false;
		} else
		{
			focusedObject = o;
			return true;
		}
		
	}
	
	public boolean setModalFocus(Object o)
	{
		focusedObject = o;
		isModal = true;
		return true;
	}
	
	/*
	 * Removes the object from focus. Returns true if the object WAS in focus and was removed,
	 * false if the object WAS NOT in focus to begin with.
	 */
	public boolean removeFromFocus(Object o)
	{
		if (focusedObject == o)
		{
//			System.out.println("Remove!");
			focusedObject = null;
			isModal = false;
			return true;
		} else
		{
			return false;
		}
	}
	
	public boolean isFocused(Object o)
	{
		if (o == null) return false;
		return (o == focusedObject);
	}
	
	public Object getFocusedObject()
	{
		return focusedObject;
	}
	
	public boolean isModal()
	{
		return isModal;
	}

}
