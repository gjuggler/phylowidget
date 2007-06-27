package org.andrewberman.ui;

public class FocusManager
{

	public static FocusManager instance;
	private static Object focusedObject = null;
	private static boolean isModal = false;
	
	public static boolean setFocus(Object o)
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
	
	public static boolean setModalFocus(Object o)
	{
		focusedObject = o;
		isModal = true;
		return true;
	}
	
	/*
	 * Removes the object from focus. Returns true if the object WAS in focus and was removed,
	 * false if the object WAS NOT in focus to begin with.
	 */
	public static boolean removeFromFocus(Object o)
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
	
	public static boolean isFocused(Object o)
	{
		if (o == null) return false;
		return (o == focusedObject);
	}
	
	public static Object getFocusedObject()
	{
		return focusedObject;
	}
	
	public static boolean isModal()
	{
		return isModal;
	}
	
}
