package org.andrewberman.ui.ifaces;

/**
 * Represents an object that has a float-precision width and height.
 * @author Greg
 *@see		org.andrewberman.ui.ifaces.Positionable
 */
public interface Sizable
{
	public void setSize(float w, float h);
	public void setWidth(float w);
	public void setHeight(float h);
	public float getWidth();
	public float getHeight();
}
