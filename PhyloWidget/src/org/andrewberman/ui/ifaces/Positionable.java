package org.andrewberman.ui.ifaces;

/**
 * Represents an object that has a float-precision location in 2D space.
 * @author Greg
 * @see		org.andrewberman.ui.ifaces.Sizable
 */
public interface Positionable
{
	public void setPosition(float x, float y);
	public float getX();
	public float getY();
	public void setX(float f);
	public void setY(float f);
}
