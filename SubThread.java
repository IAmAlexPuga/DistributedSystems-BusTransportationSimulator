import com.rti.dds.subscription.Subscriber;

class PositionSubThread extends Thread
{
	PositionSubscriber sub = new PositionSubscriber();
	
	public void run()
	{
		String[] empty = {};
		sub.main(empty);
	}

}

class AccidentSubThread extends Thread
{
	AccidentSubscriber sub = new AccidentSubscriber();
	
	public void run()
	{
		String[] empty = {};
		sub.main(empty);
	}
}