package org.andrewberman.phyloinfo.tree;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.andrewberman.phyloinfo.PhyloWidget;

import processing.core.PApplet;

public class RandomTreeMutator implements Runnable
{
	private PhyloWidget p;
	
	private Tree tree;
	private Thread wrapper;
	private java.util.Random random;
	private static String DEFAULT_NAME = "PhyloWidget";
	
	public RandomTreeMutator(Tree t) {
		p = PhyloWidget.instance;
		tree = t;
		wrapper = new Thread(this);
		wrapper.setName("NCBI-Taxonomy-fetcher");
		wrapper.start();
		random = new Random();
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
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			randomlyMutateTree();
		}
	}

	public void randomlyMutateTree() {
		String taxonName = DEFAULT_NAME;
//		taxonName = getRemoteNCBITaxon();
		taxonName = getLocalNCBITaxon();
		
		ArrayList all = tree.getAllNodes();
		int i = random.nextInt(all.size());
		TreeNode n = (TreeNode) all.get(i);
		synchronized (tree)
		{
			tree.addSisterNode(n,new TreeNode(taxonName));
			tree.sortAllChildren();
		}
		p.camera.zoomCenterTo(p.render.getRect());
	}
	
	private String getRemoteNCBITaxon() {
		// Retreive a random taxon name from NCBI:
		int taxID = random.nextInt(10000);
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
	
	public String getLocalNCBITaxon() {
		int taxID = random.nextInt(2000);
		String taxonName = DEFAULT_NAME;
		
		try
		{
			URL url = new URL("http://pantheon.yale.edu/~gej5/SoC/week-1-phylowidget/taxonomy.txt");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			for (int i=0; i < taxID; i++) {
				in.readLine();
			}
			String s = in.readLine();
			taxonName = s;
			in.close();
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
	
	public void stop() {
		wrapper = null;
	}
	
}
