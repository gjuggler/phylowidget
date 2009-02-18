package org.phylowidget.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.andrewberman.ui.FontLoader;
import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.PhyloWidget;
import org.phylowidget.tree.PhyloNode;

import processing.core.PApplet;

public class FontChooserDialog extends Dialog implements ActionListener, KeyListener
{
	PWContext pwc;
	PhyloWidget p;
	private Button apply;
	private Button ok;
	private Button cancel;

	private String originalFontName;

	protected InputList fontNameInputList;
	public String[] fontNames;

	public FontChooserDialog(Frame owner, PWContext pwc)
	{
		super(owner, "Please choose a font for the tree labels.");
		setLayout(new BorderLayout());
		this.pwc = pwc;
		this.p = pwc.getPW();

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
//				cancel();
				setVisible(false);
			}
		});

		addKeyListener(this);

		Container c = new Container();
		c.setLayout(new FlowLayout());
		Label l = new Label("Select a font.");
		c.add(l);
		add(c, BorderLayout.NORTH);

		if (fontNames == null)
		{
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			fontNames = ge.getAvailableFontFamilyNames();
		}
		fontNameInputList = new InputList(fontNames, "Font name:");
		add(fontNameInputList, BorderLayout.CENTER);
		ListSelectionListener listSelectListener = new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				//				if (e.getValueIsAdjusting())
				//					return;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						updatePreview();
					}
				});

			}
		};
		fontNameInputList.addListSelectionListener(listSelectListener);

		addButtons();
		setPreferredSize(new Dimension(250, 500));
		pack();
	}

	void updatePreview()
	{
		commit();
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
				commit();
			}
		});

		ok = new Button("Ok");
		ok.setPreferredSize(buttonSize);
		ok.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				commit();
				setVisible(false);
			}

		});
		cancel = new Button("Cancel");
		cancel.setPreferredSize(buttonSize);
		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		});
		//		p.add(new Label("Esc to cancel, \nCtrl-Enter to commit.     "));
		//		p.add(apply);
		p.add(ok);
		p.add(cancel);
		add(p, BorderLayout.SOUTH);
	}

	String origText;

	void cancel()
	{
		p.changeSetting("font", originalFontName);
		setVisible(false);
	}

	void commit()
	{
		p.changeSetting("font", fontNameInputList.getSelected());
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);

		if (visible == true)
		{
			// Reset the original font name.
			this.originalFontName = pwc.trees().getRenderer().getFontLoader().getFontName();
		}
	}

	public void actionPerformed(ActionEvent e)
	{
	}

	public void keyPressed(KeyEvent e)
	{
		//		System.out.println(e);
		//		int code = e.getKeyChar();
		//		if (code == KeyEvent.VK_ENTER)
		//		{
		//			if (e.isControlDown())
		//			{
		//				commit();
		//				setVisible(false);
		//			}
		//		} else if (code == KeyEvent.VK_ESCAPE)
		//			cancel();
	}

	public void keyReleased(KeyEvent e)
	{
	}

	public void keyTyped(KeyEvent e)
	{
	}

	class InputList extends JPanel implements ListSelectionListener, ActionListener
	{
		protected JLabel label = new JLabel();

		protected JTextField textfield;

		protected JList list;

		protected JScrollPane scroll;

		public InputList(String[] data, String title)
		{
			setLayout(null);

			add(label);
			textfield = new OpelListText();
			textfield.addActionListener(this);
			label.setLabelFor(textfield);
			add(textfield);
			list = new OpelListList(data);
			list.setVisibleRowCount(10);
			list.addListSelectionListener(this);
			scroll = new JScrollPane(list);
			add(scroll);
		}

		public InputList(String title, int numCols)
		{
			setLayout(null);
			label = new OpelListLabel(title, JLabel.LEFT);
			add(label);
			textfield = new OpelListText(numCols);
			textfield.addActionListener(this);
			label.setLabelFor(textfield);
			add(textfield);
			list = new OpelListList();
			list.setVisibleRowCount(4);
			list.addListSelectionListener(this);
			scroll = new JScrollPane(list);
			add(scroll);
		}

		public void setToolTipText(String text)
		{
			super.setToolTipText(text);
			label.setToolTipText(text);
			textfield.setToolTipText(text);
			list.setToolTipText(text);
		}

		public void setDisplayedMnemonic(char ch)
		{
			label.setDisplayedMnemonic(ch);
		}

		public void setSelected(String sel)
		{
			list.setSelectedValue(sel, true);
			textfield.setText(sel);
		}

		public String getSelected()
		{
			return textfield.getText();
		}

		public void setSelectedInt(int value)
		{
			setSelected(Integer.toString(value));
		}

		public int getSelectedInt()
		{
			try
			{
				return Integer.parseInt(getSelected());
			} catch (NumberFormatException ex)
			{
				return -1;
			}
		}

		public void valueChanged(ListSelectionEvent e)
		{
			Object obj = list.getSelectedValue();
			if (obj != null)
				textfield.setText(obj.toString());
		}

		public void actionPerformed(ActionEvent e)
		{
			ListModel model = list.getModel();
			String key = textfield.getText().toLowerCase();
			for (int k = 0; k < model.getSize(); k++)
			{
				String data = (String) model.getElementAt(k);
				if (data.toLowerCase().startsWith(key))
				{
					list.setSelectedValue(data, true);
					break;
				}
			}
		}

		public void addListSelectionListener(ListSelectionListener lst)
		{
			list.addListSelectionListener(lst);
		}

		public Dimension getPreferredSize()
		{
			Insets ins = getInsets();
			Dimension labelSize = label.getPreferredSize();
			Dimension textfieldSize = textfield.getPreferredSize();
			Dimension scrollPaneSize = scroll.getPreferredSize();
			int w = Math.max(Math.max(labelSize.width, textfieldSize.width), scrollPaneSize.width);
			int h = labelSize.height + textfieldSize.height + scrollPaneSize.height;
			return new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom);
		}

		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}

		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}

		public void doLayout()
		{
			Insets ins = getInsets();
			Dimension size = getSize();
			int x = ins.left;
			int y = ins.top;
			int w = size.width - ins.left - ins.right;
			int h = size.height - ins.top - ins.bottom;

			Dimension labelSize = label.getPreferredSize();
			label.setBounds(x, y, w, labelSize.height);
			y += labelSize.height;
			Dimension textfieldSize = textfield.getPreferredSize();
			textfield.setBounds(x, y, w, textfieldSize.height);
			y += textfieldSize.height;
			scroll.setBounds(x, y, w, h - y);
		}

		public void appendResultSet(ResultSet results, int index, boolean toTitleCase)
		{
			textfield.setText("");
			DefaultListModel model = new DefaultListModel();
			try
			{
				while (results.next())
				{
					String str = results.getString(index);
					if (toTitleCase)
					{
						str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
					}

					model.addElement(str);
				}
			} catch (SQLException ex)
			{
				System.err.println("appendResultSet: " + ex.toString());
			}
			list.setModel(model);
			if (model.getSize() > 0)
				list.setSelectedIndex(0);
		}

		class OpelListLabel extends JLabel
		{
			public OpelListLabel(String text, int alignment)
			{
				super(text, alignment);
			}

			public AccessibleContext getAccessibleContext()
			{
				return InputList.this.getAccessibleContext();
			}
		}

		class OpelListText extends JTextField
		{
			public OpelListText()
			{
			}

			public OpelListText(int numCols)
			{
				super(numCols);
			}

			public AccessibleContext getAccessibleContext()
			{
				return InputList.this.getAccessibleContext();
			}
		}

		class OpelListList extends JList
		{
			public OpelListList()
			{
			}

			public OpelListList(String[] data)
			{
				super(data);
			}

			public AccessibleContext getAccessibleContext()
			{
				return InputList.this.getAccessibleContext();
			}
		}

		// Accessibility Support

		public AccessibleContext getAccessibleContext()
		{
			if (accessibleContext == null)
				accessibleContext = new AccessibleOpenList();
			return accessibleContext;
		}

		protected class AccessibleOpenList extends AccessibleJComponent
		{

			public String getAccessibleName()
			{
				System.out.println("getAccessibleName: " + accessibleName);
				if (accessibleName != null)
					return accessibleName;
				return label.getText();
			}

			public AccessibleRole getAccessibleRole()
			{
				return AccessibleRole.LIST;
			}
		}
	}

}
