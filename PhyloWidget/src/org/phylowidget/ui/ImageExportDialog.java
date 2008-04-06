package org.phylowidget.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.andrewberman.ui.UIGlobals;
import org.phylowidget.PhyloWidget;
import org.phylowidget.render.RenderOutput;
import org.phylowidget.render.TreeRenderer;

import processing.core.PApplet;

public class ImageExportDialog extends Dialog implements ActionListener
{

	public Checkbox zoomToFull;
	
	Font f;

	private CheckboxGroup nodeLabelOptions;

	private Choice fileFormat;

	private Choice imageSize;

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
		

		int w = UIGlobals.g.getP().width;
		int h = UIGlobals.g.getP().height;
		String small = w+"x"+h;
		String med = w*2+"x"+h*2;
		String large = 4*w+"x"+4*h;
		imageSize = new Choice();
		imageSize.add(small);
		imageSize.add(med);
		imageSize.add(large);
		
		Component boundariesL2 = sectionLabel("<html><b>1) Output Format");
		Component boundariesInfo2 = infoLabel("<html>Note: the size parameter is ignored for PDF output, which is resolution-independent.");
		
		c.fill = c.BOTH;
		c.anchor = c.NORTH;
		c.gridx = 0;
		c.gridy = 0;
		
		p.add(boundariesL2,c);
		
		c.gridy++;
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
		Component boundariesInfo = infoLabel("<html><b>Note:</b> if you choose the entire tree, sometimes PhyloWidget will cut off parts" +
				" of the node labels. If that is the case, just zoom out further and render the current view.");
		
		c.fill = c.BOTH;
		c.gridx = 1;				// CHANGE THIS TO RE-ORDER THE COLUMNS
		c.gridy = 0;
		
		p.add(boundariesL,c);
		
		c.gridy++;
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
		setSize(700,320);
		pack();
		validate();
		setVisible(true);
	}

	Component infoLabel(String s)
	{
		JLabel j = new JLabel(s);
		j.setFont(f.deriveFont(10));
		j.setPreferredSize(new Dimension(200,100));
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
		String size = imageSize.getSelectedItem();
		String[] s = size.split("x");
		int w = Integer.parseInt(s[0]);
		int h = Integer.parseInt(s[1]);
	
		TreeRenderer r = PhyloWidget.trees.getRenderer();
		PApplet p = UIGlobals.g.getP();
		
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
