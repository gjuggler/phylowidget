package org.phylowidget.net;

import java.applet.Applet;
import java.awt.Toolkit;

public class SecurityChecker
{
	SecurityManager sm;
	Applet a;
	
	boolean everythingTrue;
	
	public SecurityChecker(Applet a)
	{
		this.a = a;
//		if (a.getCodeBase().toString().contains("full"))
//			everythingTrue = true;
		sm = System.getSecurityManager();
		if (sm == null)
			everythingTrue = true;
	}

	public boolean canReadFiles()
	{
		if (everythingTrue)
			return true;
		try {
			sm.checkRead("someFile.txt");
			return true;
		} catch (Exception e)
		{
//			e.printStackTrace();
			return false;
		}
	}
	
	public boolean canWriteFiles()
	{
		if (everythingTrue)
			return true;
		try {
			sm.checkWrite("someFile.txt");
			return true;
		} catch (Exception e)
		{
//			e.printStackTrace();
			return false;
		}
	}

	public boolean canAccessInternet()
	{
		if (everythingTrue)
			return true;
		try {
			sm.checkConnect("http://www.google.com/", 80);
			return true;
		} catch (Exception e)
		{
			return false;
		}
	}
	
}
