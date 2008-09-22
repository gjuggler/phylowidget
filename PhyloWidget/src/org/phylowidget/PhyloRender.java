package org.phylowidget;

public class PhyloRender
{
	
	public static void main(String[] args) throws Exception
	{
		PhyloWidget.main(null);
		PhyloWidget p = PhyloWidget.p;
		p.changeSetting("tree", "(a,(b,c))");
//		p.changeSetting("viewportX", "-200");
//		p.changeSetting("layout","unrooted");
		Thread.sleep(500);
		p.callMethod("selectNode(c)");
		p.callMethod("nodeReroot");
		Thread.sleep(5000);
		p.close();
	}

}
