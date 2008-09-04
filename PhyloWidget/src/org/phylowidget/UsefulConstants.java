package org.phylowidget;

public interface UsefulConstants
{
	/**
	 * NHX Annotation variables. (defined at http://www.phylosoft.org/forester/NHX.html).
	 */
	public static final String GENE_NAME = "GN";
	public static final String BOOTSTRAP = "B";
	public static final String DUPLICATION = "D";
	public static final String SPECIES_NAME = "S";
	public static final String TAXON_ID = "T";
	// Some of these are commented out; they are part of the NHX definition, but PW doesn't use them.
//	public static final String ACCESSION = "AC";		
//	public static final String NODE_IDENTIFIER = "ND";
//	public static final String EC_NUMBER = "EC";
//	public static final String FUNCTION = "FU";
//	public static final String DOMAIN_STRUCTURE = "DS";
//	public static final String BRANCH_WIDTH = "W";		// TODO: Implement this, it should be easy!
//	public static final String COLOR = "C";				// This is the original color annotation, but PW instead uses the more flexible 3-color annotation seen below. 
//	public static final String COLLAPSE_NODE = "CO";	// TODO: Implement this, even though it may be tough...
//	public static final String EXTRA_BRANCH_DATA = "XB";
//	public static final String EXTRA_NODE_DATA = "XN";
//	public static final String ORTHOLOGOUS_TO = "O";
//	public static final String SUBTREE_NEIGHBORS = "SN";
//	public static final String SUPER_ORTHOLOGOUS = "SO";
	
	 
	// Below are some of our own juicy extras, stacked on top of the plain vanilla NHX.
	public static final String IMG_TAG = "IMG";			// a URL to an image that will be displayed. 
	public static final String OLD_IMG_TAG = "OLD_IMG";	// Used for internal caching purposes by PhyloWidget.
	
	public static final String LABEL_COLOR = "LCOL";	// Color of a node's label
	public static final String NODE_COLOR = "NCOL";		// Color of a node's dot marker
	public static final String BRANCH_COLOR = "BCOL";	// Color of a node's branch (the branch leading to its parent)
	// NB: The values of the color annotations should be in R,G,B format, either with or without parentheses (your choice!)
	//     [Alternatively, if you specify a single integer value, it will be interpreted as a single grayscale value]
	// NB: The above color annotations take precedence over any other auto-coloring that is usually done.
	public static final String LABEL_SIZE = "LSZ"; 		// Multiplier for label text size.
	public static final String NODE_SIZE = "NSZ";		// Multiplier for node marker size.
	public static final String BRANCH_SIZE = "BSZ";		// Multiplier for branch thickness size.
	public static final String IMAGE_SIZE = "ISZ";		// Multiplier for photo size.
	public static final String LABEL_ALWAYSSHOW = "PW_ALWAYS";    // Pretty hacky, but this is a tag to make sure a label is ALWAYS shown, no matter what.
}
