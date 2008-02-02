/**************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PhyloWidget.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.andrewberman.sortedlist;

public class XYRange implements ItemI
{
	/**
	 * A reference back to an object. For convenience.
	 */
	public Object parent;
	/**
	 * An ID, also for convenience. Could be used to signify what "type" of
	 * range this is. i.e. a node or a label.
	 */
	public int id;
	
	public float loX, hiX, loY, hiY = 0;
	

	
	public XYRange(Object parent)
	{
		this(parent,0,0,0,0);
	}
	
	public XYRange(Object parent, float lox, float hix, float loy, float hiy)
	{
		this.parent = parent;
		this.loX = lox;
		this.hiX = hix;
		this.loY = loy;
		this.hiY = hiy;
	}
	
	public float get(int what)
	{
		switch (what)
		{
			case LO_X:
				return loX;
			case HI_X:
				return hiX;
			case LO_Y:
				return loY;
			case HI_Y:
				return hiY;
			default:
				return -1;
		}
	}
}
