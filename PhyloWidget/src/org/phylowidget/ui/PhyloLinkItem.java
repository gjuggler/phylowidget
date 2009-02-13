package org.phylowidget.ui;

import org.andrewberman.ui.menu.RadialLinkItem;
import org.phylowidget.PWPlatform;
import org.phylowidget.tree.PhyloNode;

public class PhyloLinkItem extends RadialLinkItem
{
	String annotation;

	@Override
	public String getUrl()
	{
		String s = super.getUrl();
		String value = getAnnotationValue();
		return s.replace("%s", value);
	}

	@Override
	public boolean checkCondition()
	{
//		return super.checkCondition();
		return getAnnotationValue() != null;
	}
	
	boolean requiresAnnotation()
	{
		if (annotation == null)
			return false;
		else if (annotation.length() == 0)
			return false;
		else
			return true;
	}

	public String getAnnotationValue()
	{
		/*
		 * Try and get the annotation for the current node.
		 */
		PhyloNode n = PWPlatform.getInstance().getThisAppContext().ui().getCurNode();
		if (n == null)
			return null;
		String value = n.getLabel();
		if (requiresAnnotation())
		{
			/*
			 * If we require an annotation, get the annotation or bust (return null)
			 */
			String annot = n.getAnnotation(annotation);
			if (annot != null)
				value = annot;
			else
				value = null;
		}
		return value;
	}

	public String getAnnotation()
	{
		return annotation;
	}

	public void setAnnotation(String annotation)
	{
		this.annotation = annotation;
	}

}
