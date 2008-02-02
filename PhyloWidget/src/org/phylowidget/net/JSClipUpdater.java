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

import org.andrewberman.unsorted.DelayedAction;
import org.andrewberman.unsorted.JSCaller;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;
import org.phylowidget.ui.PhyloUI;

public class JSClipUpdater extends DelayedAction
{
	
	JSCaller caller = new JSCaller(PhyloWidget.p);
	
	String jsCall;
	String newClip;

	public void triggerUpdate(String s)
	{
		trigger(200);
		jsCall = PhyloWidget.ui.clipJavascript;
		newClip = s;
	}

	public void run()
	{
		String cmd = jsCall;
		caller.reflectJS(cmd, newClip);
	}
}
