package org.andrewberman.phyloinfo;

import org.andrewberman.camera.Camera;
import org.andrewberman.util.Position;

import processing.core.PApplet;

public class PWCamera extends Camera
{

	private PApplet p;
	
	public PWCamera(PApplet app) {
		p = app;
	}
	
	public float getStageHeight()
	{
		// TODO Auto-generated method stub
		return (float)p.getHeight();
	}

	public float getStageWidth()
	{
		// TODO Auto-generated method stub
		return (float)p.getWidth();
	}
	
	public void screenToStage(Position p)
	{
		p.x = p.x / getScale() - getTranslationX();
		p.y = p.y / getScale() - getTranslationY();
	}
	
	public void stageToScreen(Position p)
	{
		p.x = (p.x + getTranslationX()) * getScale();
		p.y = (p.y + getTranslationY()) * getScale();
	}
	
	public void updateStage()
	{
		updatePosition();
		// TODO Auto-generated method stub
		p.translate(getTranslationX(), getTranslationY());
		p.scale(getScale());
	}

}
