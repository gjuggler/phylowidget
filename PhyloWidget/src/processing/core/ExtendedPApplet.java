package processing.core;

public class ExtendedPApplet extends PApplet
{

	public Thread getThread()
	{
		return thread;
	}
	
	public void setThread(Thread t)
	{
		this.thread = t;
	}
}
