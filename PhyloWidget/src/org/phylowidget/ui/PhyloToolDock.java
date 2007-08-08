package org.phylowidget.ui;

import org.andrewberman.camera.Camera;
import org.andrewberman.ui.Shortcut;
import org.andrewberman.ui.ToolManager;
import org.andrewberman.ui.UIEvent;
import org.andrewberman.ui.menu.Dock;
import org.andrewberman.ui.tools.ScrollTool;
import org.andrewberman.ui.tools.Tool;
import org.andrewberman.ui.tools.ZoomTool;
import org.phylowidget.TreeManager;

import processing.core.PApplet;

public class PhyloToolDock extends Dock
{
	Tool normal;
	ZoomTool zoom;
	ScrollTool scroll;

	final static String ARROW = "Arrow (a)";
	final static String ZOOM = "Zoom (z)";
	final static String SCROLL = "Scroll (s)";
	
	public PhyloToolDock(PApplet app)
	{
		super(app);
		normal = new Tool(canvas){
			public Shortcut getShortcut()
			{
				return new Shortcut("a");
			}
		};
		zoom = new ZoomTool(canvas){
			public Camera getCamera()
			{
				return TreeManager.camera;
			}
		};
		scroll = new ScrollTool(canvas){
			public Camera getCamera()
			{
				return TreeManager.camera;
			}
		};

		this.add(ARROW, "dock/arrow.png");
		this.add(ZOOM, "dock/zoom.png");
		this.add(SCROLL, "dock/move.png");

		setWidth(30);
		this.dimAlpha = 0.6f;
		
		ToolManager.instance.setListener(this);
		
		switchTool(normal);
		toolChanged();
	}

	public void toolChanged()
	{
		Tool t = ToolManager.instance.curTool;
		if (t == normal)
			lastPressed = this.get(ARROW);
		else if (t == zoom)
			lastPressed = this.get(ZOOM);
		else if (t == scroll)
			lastPressed = this.get(SCROLL);
		layout();
	}
	
	public void fireEvent(int id)
	{
		super.fireEvent(id);
		if (id == UIEvent.DOCK_ITEM_SELECTED)
		{
			/*
			 * A new item has been selected, so change tools.
			 */
			String label = lastPressed.label;
			if (label.equals(ARROW))
			{
				switchTool(normal);
			} else if (label.equals(ZOOM))
			{
				switchTool(zoom);
			} else if (label.equals(SCROLL))
			{
				switchTool(scroll);
			}
		}
	}

	void switchTool(Tool t)
	{
		ToolManager.instance.switchTool(t);
	}

}
