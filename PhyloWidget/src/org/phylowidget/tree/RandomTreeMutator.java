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

	private RootedTree			tree;
	private Thread				wrapper;
	private java.util.Random	random;
	private static String		DEFAULT_NAME	= "PhyloWidget";

	public int					delay			= 1000;

	public int					mutations		= 0;

	public RandomTreeMutator(RootedTree t)
	{
		tree = t;
		random = new Random();

		// InputStream is = new FileInputStream("taxonomy.txt");
		InputStream is = PhyloWidget.p.openStream("taxonomy.txt");
		InputStreamReader read = new InputStreamReader(is);
		in = new BufferedReader(read);
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
			randomlyMutateTree();
		}
	}

	private ArrayList	allNodes	= new ArrayList(100);

	public synchronized void randomlyMutateTree()
	{
		String taxonName = DEFAULT_NAME;
		// taxonName = getRemoteNCBITaxon();
		taxonName = getLocalNCBITaxon();
//		synchronized (tree)
//		{
			allNodes.clear();
			tree.getAll(tree.getRoot(), null, allNodes);
			int i = random.nextInt(allNodes.size());
			Object vertex = allNodes.get(i);
			PhyloNode sis = (PhyloNode) tree.createAndAddVertex(taxonName);
			tree.addSisterNode(vertex,sis);
			mutations++;
//		}
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

	BufferedReader	in;

	public String getLocalNCBITaxon()
	{
		String taxonName = DEFAULT_NAME;

		try
		{
			String s = in.readLine();
			taxonName = s;
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return taxonName;
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return taxonName;
		} catch (Exception e)
		{
			e.printStackTrace();
			return taxonName;
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

}
