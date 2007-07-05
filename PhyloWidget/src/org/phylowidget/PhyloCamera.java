package org.phylowidget;

import org.andrewberman.camera.MovableCamera;

public class PhyloCamera extends MovableCamera
{

	public PhyloCamera(PhyloWidget p)
	{
		super(p);
	}

	public float getStageHeight()
	{
		// TODO Auto-generated method stub
		return (float) p.getHeight();
	}

	public float getStageWidth()
	{
		// TODO Auto-generated method stub
		return (float) p.getWidth();
	}

	public void update()
	{
		super.update();
		
		/*
		 * Translate by half the stage width and height to re-center the stage
		 * at (0,0).
		 */
		p.translate(getStageWidth()/2.0f,getStageHeight()/2.0f);
		/*
		 * Now scale.
		 */
		p.scale(getZ());
		/*
		 * Then translate.
		 */
		p.translate(-getX(),-getY());
	}	
}