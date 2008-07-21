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
package org.phylowidget.net;

import java.net.URL;

import org.andrewberman.ui.UIGlobals;
import org.andrewberman.ui.unsorted.DelayedAction;
import org.andrewberman.ui.unsorted.JSCaller;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.RootedTree;
import org.phylowidget.tree.TreeIO;

public class JSTreeUpdater extends DelayedAction
{
	JSCaller caller = new JSCaller(UIGlobals.g.getP());
//	String jsCall = "updateTree";

	public void triggerUpdate(RootedTree t)
	{
		trigger(200);
	}

	public void run()
	{
		PhyloWidget.ui.search();
		String s = TreeIO
				.createNewickString(PhyloWidget.trees.getTree(), false);
//		String cmd = jsCall;
		try
		{
//			System.out.println(caller.reflectionWorking);
//			caller.injectJavaScript(file)
			Object o = caller.getMember("PhyloWidget");
			caller.callWithObject(o, "updateTree", s);
//			caller.eval("PhyloWidget.updateTree(\""+s+"\");");
//			caller.eval("alert('hey!');");
		} catch (Exception e)
		{
			//			e.printStackTrace();
		}
	}

}
