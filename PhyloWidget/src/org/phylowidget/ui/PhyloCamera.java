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
package org.phylowidget.ui;

import org.andrewberman.ui.camera.MovableCamera;
import org.phylowidget.PhyloWidget;

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