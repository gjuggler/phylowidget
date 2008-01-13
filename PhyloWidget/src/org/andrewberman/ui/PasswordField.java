package org.andrewberman.ui;

import processing.core.PApplet;

public class PasswordField extends TextField
{

	StringBuffer modelText = new StringBuffer();
	
	public PasswordField(PApplet p)
	{
		super(p);
	}

	String stars(String s)
	{
		return s.replaceAll(".", "*");
	}
	
	@Override
	protected void insert(String s, int pos)
	{
		super.insert(stars(s), pos);
		modelText.insert(pos,s);
	}
	
	@Override
	protected void deleteAt(int pos)
	{
		super.deleteAt(pos);
		if (pos < 0 || pos >= modelText.length())
			return;
		modelText.deleteCharAt(pos);
	}
	
	@Override
	public String getText(int lo, int hi)
	{
//		return super.getText(lo, hi);
		return modelText.substring(lo, hi);
	}
	
}
