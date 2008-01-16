package org.phylowidget.tree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.tree.TreeNode;

import org.phylowidget.PhyloWidget;
import org.phylowidget.ui.PhyloNode;

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
		InputStream is = PhyloWidget.p.openStream("taxonomy.txt");
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
			PhyloWidget.trees.triggerMutation();
		}
	}

	private ArrayList allNodes = new ArrayList(100);

	public synchronized void randomlyMutateTree()
	{
		String taxonName = DEFAULT_NAME;
		// taxonName = getRemoteNCBITaxon();
		taxonName = getLocalNCBITaxon();
		 synchronized (tree)
		 {
		allNodes.clear();
		tree.getAll(tree.getRoot(), null, allNodes);
		int i = random.nextInt(allNodes.size());
		Object vertex = allNodes.get(i);
		PhyloNode sis = (PhyloNode) tree.createAndAddVertex(taxonName);
		tree.addSisterNode(vertex, sis);

		tree.setBranchLength(vertex, randomBranch());
		tree.setBranchLength(sis, randomBranch());
		tree.setBranchLength(tree.getParentOf(sis), randomBranch());
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
