package org.phylowidget.ui;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.phylowidget.PWContext;
import org.phylowidget.PWPlatform;
import org.phylowidget.render.RenderOutput;
import org.phylowidget.render.TreeRenderer;

import processing.core.PApplet;

public class ImageExportDialog extends Dialog implements ActionListener
{

	public Checkbox zoomToFull;
	
	Font f;

	private CheckboxGroup nodeLabelOptions;

	private Choice fileFormat;

	private JComboBox imageSize;

	private CheckboxGroup viewportOptions;

	private Button ok;

	private Button cancel;

	private Checkbox vo_entireTree;

	private Checkbox vo_currentView;

	private Checkbox node_showAll;

	private Checkbox node_useMinText;
	
	public ImageExportDialog(Frame owner)
	{
		super(owner,"Image Export Options",true);
		
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});		
		f = owner.getFont();

		Panel p = new Panel();
		GridBagLayout gb = new GridBagLayout();
		p.setLayout(gb);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		
		c.fill = c.BOTH;
		c.anchor = c.NORTH;
		
		fileFormat = new Choice();
		fileFormat.add("PDF");
		fileFormat.add("PNG");
		fileFormat.add("JPG");
		fileFormat.add("TIF");
		

		PApplet applet = PWPlatform.getInstance().getThisAppContext().getApplet();
		int w = applet.width;
		int h = applet.height;
		String small = w+"x"+h;
		String med = w*2+"x"+h*2;
		String large = 4*w+"x"+4*h;
		String[] sizes = new String[]{small,med,large};
		long mem = Runtime.getRuntime().freeMemory();
		System.out.println(mem);
//		String huge = 8*w+"x"+8*h;
		imageSize = new JComboBox(sizes);
		imageSize.setEditable(true);
		
		Component boundariesL2 = sectionLabel("<html><b>1) Output Format");
		Component boundariesInfo2 = infoLabel("<html><b>Note:</b> the size parameter is ignored for PDF output, which is resolution independent.<br><b>Hint:</b>You can specify arbitrary output dimensions by entering a width and height in the same format as the defaults. But be warned: large sizes may cause memory errors and program crashes!");
		
		c.fill = c.BOTH;
		c.anchor = c.NORTH;
		c.gridx = 0;
		c.gridy = 0;
		
		p.add(boundariesL2,c);
		
		c.gridy++;
		c.anchor = c.NORTH;
		p.add(boundariesInfo2,c);
		
		c.gridy++;
		c.fill = c.NONE;
		c.anchor = c.CENTER;
		p.add(fileFormat,c);
		
		c.gridy++;
		p.add(imageSize,c);
		
		viewportOptions = new CheckboxGroup();
		vo_entireTree = new Checkbox("Render the entire tree",true,viewportOptions);
		vo_currentView = new Checkbox("Use the current viewport",false,viewportOptions);
		Component boundariesL = sectionLabel("<html><b>2) Rendering Boundaries");
		Component boundariesInfo = infoLabel("<html><b>Note:</b> if you choose the \"entire tree\" option, PhyloWidget may cut off parts" +
				" of the tree, especially with a large minimum text size or branch scaling. If that is the case, just manually zoom to the entire tree and choose the \"current viewport\" option.");
		
		c.fill = c.BOTH;
		c.gridx = 1;				// CHANGE THIS TO RE-ORDER THE COLUMNS
		c.gridy = 0;
		
		p.add(boundariesL,c);
		
		c.gridy++;
		c.anchor = c.NORTH;
		p.add(boundariesInfo,c);
		
		c.gridy++;
		c.anchor = c.CENTER;
		p.add(vo_entireTree,c);
		
		c.gridy++;
		p.add(vo_currentView,c);
		
		c.gridx = 1;
		c.gridy = 0;
		
		nodeLabelOptions = new CheckboxGroup();
		node_showAll = new Checkbox("Show ALL node labels",true,nodeLabelOptions);
		node_useMinText = new Checkbox("Use the minimum text size setting",false,nodeLabelOptions);
		Component boundariesL1 = sectionLabel("<html><b>3) Node Label Display");
		Component boundariesInfo1 = infoLabel("<html>Please choose whether PhyloWidget should display <i>all</i> node labels or respect the minimum text size setting.");
		
		c.gridx = 2;
		c.gridy = 0;
		
		p.add(boundariesL1,c);
		
		c.gridy++;
		c.anchor = c.NORTH;
		p.add(boundariesInfo1,c);
		
		c.gridy++;
		c.anchor = c.CENTER;
		p.add(node_showAll,c);
		
		c.gridy++;
		p.add(node_useMinText,c);
	
		c.gridx = 1;
		c.gridy = 7;
		
		Panel p2 = new Panel();
		
		ok = new Button("Ok");
		ok.addActionListener(this);
		cancel = new Button("Cancel");
		cancel.addActionListener(this);
		
		p2.add(ok);
		p2.add(cancel);
		
		p.add(p2,c);
		
		/*
		 * Add the panel, and we're done!
		 */
		add(p);
		setSize(780,425);
		pack();
		validate();
		setVisible(true);
	}

	Component infoLabel(String s)
	{
		JLabel j = new JLabel(s);
		j.setAlignmentY(JLabel.TOP_ALIGNMENT);
		j.setFont(f.deriveFont(Font.PLAIN,11));
		j.setPreferredSize(new Dimension(200,150));
		return j;
	}
	
	Component sectionLabel(String s)
	{
		JLabel l = new JLabel(s);
		l.setFont(f.deriveFont(16));
		return l;
	}

	void ok()
	{
		/*
		 * Let's do it!
		 */
		
		boolean zoomToFit = (viewportOptions.getSelectedCheckbox() == vo_entireTree);
		boolean showAll = (nodeLabelOptions.getSelectedCheckbox() == node_showAll);
		
		String format = fileFormat.getSelectedItem();
		String size = imageSize.getSelectedItem().toString();
		String[] s = size.split("x");
		int w = Integer.parseInt(s[0]);
		int h = Integer.parseInt(s[1]);
	
		PWContext context = PWPlatform.getInstance().getThisAppContext();
		TreeRenderer r = context.trees().getRenderer();
		PApplet p = context.getPW();
		
//		setEnabled(false);
		ok.setEnabled(false);
		cancel.setEnabled(false);
		if (format.toLowerCase().equals("pdf"))
		{
			RenderOutput.savePDF(p, r, zoomToFit, showAll);
		} else
		{
			RenderOutput.save(p, r, zoomToFit, showAll, format, w, h);
		}
		dispose();
	}
	
	void cancel()
	{
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					ok();
				}
					
			});
		} else if (e.getSource() == cancel)
		{
			dispose();
		}
	}
	
}
