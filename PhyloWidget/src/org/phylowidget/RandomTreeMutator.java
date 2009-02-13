/*******************************************************************************
 * Copyright (c) 2007, 2008 Gregory Jordan
 * 
 * This file is part of PhyloWidget.
 * 
 * PhyloWidget is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * PhyloWidget is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * PhyloWidget. If not, see <http://www.gnu.org/licenses/>.
 */
package org.phylowidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import org.phylowidget.tree.DefaultVertex;
import org.phylowidget.tree.PhyloNode;
import org.phylowidget.tree.RootedTree;

public class RandomTreeMutator implements Runnable
{
	private RootedTree tree;
	private Thread wrapper;
	private java.util.Random random;
	private static String DEFAULT_NAME = "PhyloWidget";

	public int delay = 1000;

	public int mutations = 0;

	public RandomTreeMutator(RootedTree t)
	{
		tree = t;
		random = new Random(System.currentTimeMillis());

		// InputStream is = new FileInputStream("taxonomy.txt");
		InputStream is = PWPlatform.getInstance().getThisAppContext().getApplet().openStream("taxonomy.txt");
		InputStreamReader read = new InputStreamReader(is);
		in = new BufferedReader(read);
		try
		{
			in.mark(10);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void start()
	{
		wrapper = new Thread(this);
		wrapper.setName("PhyloWidget-tree-mutator");
		wrapper.start();
	}

	public void run()
	{
		Thread thisThread = null;
		try
		{
			thisThread = Thread.currentThread();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		while (wrapper == thisThread)
		{
			try
			{
				Thread.sleep(delay);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PWPlatform.getInstance().getThisAppContext().trees().triggerMutation();
		}
	}

	private ArrayList allNodes = new ArrayList(100);

	public void randomlyMutateTree()
	{
		String taxonName = DEFAULT_NAME;
		// taxonName = getRemoteNCBITaxon();
		taxonName = getLocalNCBITaxon();
		synchronized (tree)
		{
			allNodes.clear();
			tree.getAll(tree.getRoot(), null, allNodes);
			int i = random.nextInt(allNodes.size());
			DefaultVertex vertex = (DefaultVertex)allNodes.get(i);
			PhyloNode sis = (PhyloNode) tree.createAndAddVertex();
			tree.setLabel(sis, taxonName);
			
			tree.addSisterNode(vertex, sis);

			tree.setBranchLength(vertex, randomBranch());
			tree.setBranchLength(sis, randomBranch());
			tree.setBranchLength(tree.getParentOf(sis), randomBranch());
//			sis.searchForImages();
			mutations++;
		}
		//		PhyloWidget.trees.fforward(true, true);
	}

	private double randomBranch()
	{
		double val = Math.random();
		val *= 100;
		val = Math.round(val);
		val /= 100;
		return val;
	}

	private String getRemoteNCBITaxon()
	{
		// Retreive a random taxon name from NCBI:
		final int taxID = random.nextInt(100000);
		String taxonName = DEFAULT_NAME;

		URL url;
		try
		{
			// url = new
			// URL("http://www.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=taxonomy&id="+String.valueOf(taxID));
			// final XMLInputFactory f = XMLInputFactory.newInstance();
			// final XMLStreamReader r =
			// f.createXMLStreamReader(url.openStream());
			// while (r.hasNext())
			// {
			// if (r.getEventType() == XMLStreamConstants.START_ELEMENT)
			// {
			// if (r.getAttributeCount() > 0 &&
			// r.getAttributeValue(0).equals("ScientificName"))
			// {
			// r.next();
			// taxonName = r.getText();
			// }
			// }
			// r.next();
			// }
		} catch (final Exception e)
		{
			e.printStackTrace();
			return taxonName;
		}
		return taxonName;
	}

	BufferedReader in;

	public String getLocalNCBITaxon()
	{
		String taxonName = DEFAULT_NAME;
		try
		{
			in.reset();
			int limit = random.nextInt(200);
			for (int i = 0; i < limit; i++)
			{
				taxonName = in.readLine();
			}
			if (taxonName == null)
				taxonName = DEFAULT_NAME;
		} catch (Exception e)
		{
			e.printStackTrace();
			return DEFAULT_NAME;
		}
		return taxonName;
	}

	public void setTree(RootedTree t)
	{
		tree = t;
	}

	public void stop()
	{
		wrapper = null;
	}

	public int getDelay()
	{
		return delay;
	}

	public void setDelay(int delay)
	{
		this.delay = delay;
	}

}
