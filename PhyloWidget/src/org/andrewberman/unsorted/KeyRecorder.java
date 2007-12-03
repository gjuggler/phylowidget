package org.andrewberman.unsorted;

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
