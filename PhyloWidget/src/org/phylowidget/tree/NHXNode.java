package org.phylowidget.tree;

import java.util.HashMap;

public class NHXNode extends PhyloNode
{

	HashMap<String,String> annotations;
	
	public static final String DUPLICATION = "D";
	public static final String GENE_NAME = "GN";
	public static final String TAXON_ID = "T";
	public static final String BOOTSTRAP = "B";
	public static final String SPECIES_NAME = "S";
	public static final String ORTHOLOGOUS_TO = "O";
	
	public NHXNode()
	{
		super();
		annotations = new HashMap<String,String>();
	}

	public void setAnnotation(String key,String value)
	{
		annotations.put(key, value);
	}
	
	public String getAnnotation(String key)
	{
		return annotations.get(key);
	}
	
}
