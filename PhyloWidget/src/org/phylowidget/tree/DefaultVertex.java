/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget.tree;

public class DefaultVertex implements Labelable, Cloneable
{
	public String label;

	public DefaultVertex()
	{
		label = new String();
	}

	@Override
	protected Object clone()
	{
		try
		{
			DefaultVertex clone = (DefaultVertex) super.clone();
			clone.setLabel(label);
			return clone;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String s)
	{
		label = s;
	}

	public String toString()
	{
		return label;
	}
}
