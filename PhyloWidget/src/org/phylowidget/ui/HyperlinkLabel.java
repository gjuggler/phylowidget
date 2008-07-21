package org.phylowidget.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.JLabel;

import org.andrewberman.ui.UIGlobals;

public class HyperlinkLabel extends JLabel implements MouseListener
{

	String url;
	
	public HyperlinkLabel(String label, String url)
	{
		super(label);
		this.url = url;
		
		setForeground(Color.BLUE);
		addMouseListener(this);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String url)
	{
		this.url = url;
	}

	public void mouseClicked(MouseEvent e)
	{
		try
		{
			String url = getUrl();
			URL realURL = new URL(url);
			UIGlobals.g.getP().getAppletContext().showDocument(realURL,"_new");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}
	
}
