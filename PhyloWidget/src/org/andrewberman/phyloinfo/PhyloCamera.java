package org.andrewberman.phyloinfo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import org.andrewberman.camera.Camera;
import org.andrewberman.camera.PMovableCamera;
import org.andrewberman.ui.ProcessingUtils;

import processing.core.PApplet;

public class PhyloCamera extends PMovableCamera
{

	public PhyloCamera()
	{
		super(PhyloWidget.p);
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