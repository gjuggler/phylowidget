package org.andrewberman.ui.test;

import org.andrewberman.ui.menu.CheckBox;
import org.andrewberman.ui.menu.NumberScroller;
import org.andrewberman.ui.menu.Toolbar;

import processing.core.PApplet;

public class UITest extends PApplet
{

	public boolean isEnabled = true;
	
	@Override
	public void setup()
	{
		super.setup();
		Toolbar t = new Toolbar(this);
		t.setOrientation(Toolbar.VERTICAL);
		CheckBox b = new CheckBox();
		b.setName("Hello");
		b.setProperty(this, "isEnabled");
		t.add(b);
		NumberScroller s = new NumberScroller();
		s.setName("ScrollMe");
		s.setDefault(50);
		t.add(s);
		t.add("Whatever");
		t.get("Whatever").add("Hey");
		t.get("Whatever").add("How");
		t.get("Whatever").add("Hoo");
	}
	
	public void draw()
	{
		background(255);
	}
	
}
