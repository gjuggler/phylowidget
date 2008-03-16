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
package org.phylowidget.net;

import org.andrewberman.ui.unsorted.DelayedAction;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.TreeClipboard;

public class PWClipUpdater extends DelayedAction
{
	String parseMe;
	public void triggerUpdate(String s)
	{
		parseMe = s;
		trigger(100);
	}
	
	public void run()
	{
		TreeClipboard.instance().setClipFromJS(parseMe);
		PhyloWidget.setMessage("Clipboard text set to: "+parseMe);
	}
}
