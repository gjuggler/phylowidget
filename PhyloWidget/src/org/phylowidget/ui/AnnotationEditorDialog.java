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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Set;

import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class AnnotationEditorDialog extends Dialog implements ActionListener,
		KeyListener
{
	PApplet p;
	PhyloNode node;
	private Button apply;
	private Button ok;
	private Button cancel;
	private TextArea text;

	public AnnotationEditorDialog(Frame owner,PApplet p)
	{
		super(owner,
				"Please enter your annotations. One per line, \"key=value\" format");
		setLayout(new BorderLayout());
		this.p = p;
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				cancelAnnotations();
			}
		});

		addKeyListener(this);

		Container c = new Container();
		c.setLayout(new FlowLayout());
		Label l = new Label(
				"Example annotations: D=Y, B=100, or S=Homo sapiens");
		c.add(l);
		HyperlinkLabel hl = new HyperlinkLabel("(More Info)","http://www.phylowidget.org");
		c.add(hl);
		add(c,BorderLayout.NORTH);

		text = new TextArea();
		text.addKeyListener(this);
		add(text, BorderLayout.CENTER);

		addButtons();

		setSize(new Dimension(400, 250));
		pack();
//		setVisible(true);
	}

	public void setNode(PhyloNode n)
	{
		this.node = n;
		initText();
	}
	
	void addButtons()
	{
		Panel p = new Panel(new FlowLayout(FlowLayout.RIGHT));

		Dimension buttonSize = new Dimension(50, 30);

		apply = new Button("Apply");
		apply.setPreferredSize(buttonSize);
		apply.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				commitAnnotations();
			}
		});
		
		ok = new Button("Ok");
		ok.setPreferredSize(buttonSize);
		ok.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				commitAnnotations();
				setVisible(false);
			}

		});
		cancel = new Button("Cancel");
		cancel.setPreferredSize(buttonSize);
		cancel.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				cancelAnnotations();
			}

		});
		p.add(new Label("Esc to cancel, Ctrl-Enter to commit.     "));
		p.add(apply);
		p.add(ok);
		p.add(cancel);
		add(p, BorderLayout.SOUTH);
	}

	String origText;

	void cancelAnnotations()
	{
		/*
		 * Do nothing. Just quit.
		 */
		setVisible(false);
	}

	void commitAnnotations()
	{
		node.clearAnnotations();
		String textS = text.getText();
		String[] lines = textS.split("\n");
		for (String line : lines)
		{
			if (line.length() < 2)
				continue;
			try
			{
				int firstInd = line.indexOf('=');
				if (firstInd == -1)
					continue;
				String key = line.substring(0, firstInd);
				String val = line.substring(firstInd+1, line.length());
				node.setAnnotation(key, val);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		/*
		 * Need to re-layout so that the tree renderer has a chance to re-assign
		 * node colors and whatnot... There should be a better way to avoid having to 
		 * trigger a layout from here, but frankly I'm too lazy to think of one!
		 */
		PWPlatform.getInstance().getThisAppContext().ui().layout();
//		PhyloWidget.ui.layout();
		node.getTree().modPlus();
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		text.requestFocus();
	}
	
	void initText()
	{
		String textS = new String();

		HashMap<String, String> map = node.getAnnotations();
		if (map == null)
		{
			text.setText(new String());
			return;
		}
		Set<String> set = map.keySet();
		for (String s : set)
		{
			textS += s + "=" + map.get(s) + "\n";
		}
		origText = textS;
		text.setText(textS);
	}

	public void actionPerformed(ActionEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		//		System.out.println(e);
		int code = e.getKeyChar();
		if (code == KeyEvent.VK_ENTER)
		{
			if (e.isControlDown())
			{
				commitAnnotations();
				setVisible(false);
			}
		} else if (code == KeyEvent.VK_ESCAPE)
			cancelAnnotations();
	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyTyped(KeyEvent e)
	{
	}

}
