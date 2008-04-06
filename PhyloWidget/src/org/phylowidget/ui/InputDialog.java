package org.phylowidget.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class InputDialog extends Dialog implements WindowListener
{
	TextArea text;
	Button ok;
	Button cancel;

	public InputDialog(Frame owner, String title)
	{
		super(owner, title,true);
		setLayout(new BorderLayout());

		text = new TextArea("", 5, 40, TextArea.SCROLLBARS_BOTH);
		add(text,BorderLayout.CENTER);

		Panel p = new Panel(new FlowLayout(FlowLayout.RIGHT));
		
		Dimension buttonSize = new Dimension(50,30);
		
		ok = new Button("Ok");
		ok.setPreferredSize(buttonSize);
		ok.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}

		});
		cancel = new Button("Cancel");
		cancel.setPreferredSize(buttonSize);
		cancel.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				text.setText("");
				dispose();
			}

		});
		p.add(ok);
		p.add(cancel);
		
		add(p,BorderLayout.SOUTH);
		
		setSize(500, 200);
		
		validate();
		
		addWindowListener(this);
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowClosed(WindowEvent e)
	{
	}

	public void windowClosing(WindowEvent e)
	{
		dispose();
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}

}
