
 public class Passanger extends Thread{

	int waitingAtStop;
	String waitingAtRoute;
	int destination;
	Boolean onBoard = false;
	int ID;
	boolean leftBus = false;
	String onBus = "";
	
	
	AccidentFiltered myA = new AccidentFiltered(this);
	PositionFilter myP = new PositionFilter(this);
	
	public Passanger(int wat, String war, int d, int id)
	{
		waitingAtStop = wat;
		waitingAtRoute = war;
		destination = d;
		ID = id;
	}
	
	
	public static void main(String[] args)
	{
		Passanger p = new Passanger(2,"Express1",4, 1);
		
		try {
			p.myA.start();
			p.myP.start();
			
			//p.myA.subscriberMain(0, 0, 1);
		}catch (Exception e){
			System.out.println(e.toString());
		}
		
		try {
			p.myA.join();
			p.myP.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
