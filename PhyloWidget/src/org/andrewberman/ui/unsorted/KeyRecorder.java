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
package org.andrewberman.ui.unsorted;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.andrewberman.sortedlist.ItemI;

public class KeyRecorder
{
	HashMap events = new HashMap(50);

	public void clear()
	{
		events.clear();
	}
	
	public ArrayList getEventsForFrame(int frame)
	{
		System.out.println(frame);
		Integer frInt = new Integer(frame);
		if (events.containsKey(frInt))
		{
			return (ArrayList) events.get(frInt);
		} else
			return null;
	}
	
	public void recordEvent(KeyEvent e, int frame)
	{
		System.out.println(frame);
		Integer frInt = new Integer(frame);
//		FrameKeyEvent event = new FrameKeyEvent(e,frame);
		if (events.containsKey(frInt))
		{
			ArrayList fEvents = (ArrayList) events.get(frInt);
			fEvents.add(e);
		} else
		{
			ArrayList fEvents = new ArrayList();
			fEvents.add(e);
			events.put(frInt,fEvents);
		}
	}
	
//	class FrameKeyEvent
//	{
//		public KeyEvent event;
//		public int frame;
//		
//		public FrameKeyEvent(KeyEvent event, int frame)
//		{
//			this.event = event;
//			this.frame = frame;
//		}
//	}
}
