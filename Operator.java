

public class Operator
{

	
	public static void main(String[] args)
	{
		
		PositionSubThread pos = new PositionSubThread();
		AccidentSubThread acc = new AccidentSubThread();

		
		try
		{
			pos.start();
			acc.start();
			PrintChart();
			
		}catch (Exception e){
			e.printStackTrace();
		}
		try {
			
			pos.join();
			acc.join();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void PrintChart()
	{
		System.out.println("MessageType \t Route \t     Vehicle     Traffic Stop# #Stops   TimeBetweenStops \t Fill%    TimeStamps");
	}
	
}
