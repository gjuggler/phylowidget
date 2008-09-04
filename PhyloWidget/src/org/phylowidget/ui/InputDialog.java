package org.phylowidget.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.phylowidget.PhyloWidget;
import org.phylowidget.net.SecurityChecker;
import org.phylowidget.tree.TreeIO;

public class InputDialog extends Dialog implements WindowListener, KeyListener
{
	TextArea text;
	Button ok;
	Button cancel;
	public String str = null;

	public InputDialog(Frame owner, String title)
	{
		super(owner, title, false);
//		setModal(false);

		addKeyListener(this);
		
		setLayout(new BorderLayout());

		text = new TextArea("", 5, 40, TextArea.SCROLLBARS_BOTH);
		String s = TreeIO.createNewickString(
			PhyloWidget.trees.getTree(), false);
		text.setText(s);
		add(
			text, BorderLayout.CENTER);
		text.addKeyListener(this);

		Panel p = new Panel(new FlowLayout(FlowLayout.RIGHT));

		Dimension buttonSize = new Dimension(50, 30);

		ok = new Button("Ok");
		ok.setPreferredSize(buttonSize);
		ok.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				str = text.getText();
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
				str = "";
				dispose();
			}

		});

		p.add(new Label("Esc to cancel, Ctrl-Enter to commit.     "));
		p.add(ok);
		p.add(cancel);
		add(
			p, BorderLayout.SOUTH);

		setSize(
			500, 200);

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

	public void keyPressed(KeyEvent e)
	{
		//		System.out.println(e);
		int code = e.getKeyChar();
		if (code == KeyEvent.VK_ENTER)
		{
			if (e.isControlDown())
				dispose();
		} else if (code == KeyEvent.VK_ESCAPE)
		{
			text.setText("");
			dispose();
		}
	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyTyped(KeyEvent e)
	{
	}

}
