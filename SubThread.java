import com.rti.dds.subscription.Subscriber;

class PositionSubThread extends Thread
{
	PositionSubscriber sub = new PositionSubscriber();
	
	public void run()
	{
		
		sub.RunMain();
		
	}

}

class AccidentSubThread extends Thread
{
	AccidentSubscriber sub = new AccidentSubscriber();
	
	public void run()
	{
		sub.RunMain();
	}

}