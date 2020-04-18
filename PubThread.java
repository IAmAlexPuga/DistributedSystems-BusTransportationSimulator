
public class PubThread extends Thread
{
	Position p;
	
	public PubThread(Position pos)
	{
		this.p = pos;
	}
	
	public void Run()
	{
		System.out.println("Thread " + p.vehicle + "Started");
		this.start();
		
	}
}
