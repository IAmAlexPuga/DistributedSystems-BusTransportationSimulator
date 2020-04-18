

public class Operator
{

	
	public static void main(String[] args)
	{
		PrintChart();
		AccidentSubscriber acc = new AccidentSubscriber();
		PositionSubscriber pos = new PositionSubscriber();
		while(true)
		{
			acc.Invoke();
		}
		


	}
	
	
	public static void PrintChart()
	{
		System.out.println("MessageType \t Route \t Vehicle     Traffic    Stop#    #Stops    TimeBetweenStops \t Fill%    TimeStamps");
	}

}
