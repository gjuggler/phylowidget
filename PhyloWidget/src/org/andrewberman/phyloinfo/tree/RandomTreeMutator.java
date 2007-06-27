package org.andrewberman.phyloinfo.tree;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.andrewberman.phyloinfo.PhyloWidget;

public class RandomTreeMutator implements Runnable
{
	private PhyloWidget p;
	private Tree tree;
	private Thread wrapper;
	private java.util.Random random;
	private static String DEFAULT_NAME = "PhyloWidget";
	
	public int delay = 1000;
	
	public int mutations = 0;
	
	public RandomTreeMutator(PhyloWidget p, Tree t) {
		this.p = p;
		tree = t;
		wrapper = new Thread(this);
		wrapper.setName("PhyloWidget-tree-mutator");
		wrapper.start();
		random = new Random();
		
		InputStream is = p.openStream("taxonomy.txt");
		InputStreamReader read = new InputStreamReader(is);
		in = new BufferedReader(read);
	}
	
	public void run()
	{
		Thread thisThread = null;
		try {
			thisThread = Thread.currentThread();
		} catch (Exception e) {
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

	private ArrayList allNodes = new ArrayList(100);
	public void randomlyMutateTree() {
		String taxonName = DEFAULT_NAME;
//		taxonName = getRemoteNCBITaxon();
		taxonName = getLocalNCBITaxon();
		
		synchronized (tree)
		{
			allNodes.clear();
			tree.getAllNodes(allNodes);
			int i = random.nextInt(allNodes.size());
			TreeNode n = (TreeNode) allNodes.get(i);
			tree.addSisterNode(n,new TreeNode(taxonName));
//			tree.sortAllChildren();
		}
		mutations++;
//		p.camera.zoomCenterTo(p.render.getRect());
	}
	
	private String getRemoteNCBITaxon() {
		// Retreive a random taxon name from NCBI:
		int taxID = random.nextInt(100000);
		String taxonName = DEFAULT_NAME;
		
		URL url;
		try
		{
			url = new URL("http://www.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=taxonomy&id="+String.valueOf(taxID));
			XMLInputFactory f = XMLInputFactory.newInstance();
			XMLStreamReader r = f.createXMLStreamReader(url.openStream());
			while (r.hasNext())
			{
				if (r.getEventType() == XMLStreamConstants.START_ELEMENT)
				{
					if (r.getAttributeCount() > 0 && r.getAttributeValue(0).equals("ScientificName"))
					{
						r.next();
						taxonName = r.getText();
					}
				}
				r.next();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return taxonName;
		}
		return taxonName;
	}
	
	BufferedReader in;
	public String getLocalNCBITaxon() {
		int taxID = random.nextInt(4000);
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
	
	public void setTree(Tree t) {
		tree = t;
	}
	
	public void stop() {
		wrapper = null;
	}
	
}
