import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class PubThread extends Thread
{
	Position p;
	LocalDateTime now;
	DateTimeFormatter dtf;
	int Cycles = 1;
	boolean Accident = false;
	
	PositionPublisher pos;
	AccidentPublisher accident;
	
	public PubThread(Position pos, LocalDateTime time)
	{
		this.p = pos;
		this.now = time;
		this.dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		this.pos = new PositionPublisher();
		this.accident = new AccidentPublisher();
	}
	
	public void Run()
	{
		System.out.println("Thread " + p.vehicle + " Started");
		this.start();
		Random rand = new Random();
		LocalDateTime old;
		int departing = 0;

		while(Cycles < 4)
		{
			if(p.stopNumber > p.numStops)
			{
				Cycles ++;
				p.stopNumber = 1;
			}
			old = this.now;
			SetTraffic(rand.nextInt(101) + 1);
			CheckAccident(rand.nextInt(101) + 1);
			
			departing = rand.nextInt(p.fillInRation);
			Stop(rand.nextInt(100 - p.fillInRation) - departing, GetTimeStampBetween(old));
			
			p.stopNumber ++;
		}
		
	}
	
	// Sets the traffic and ads the set time based on traffic
	public void SetTraffic(int chance)
	{
		if(chance < 26)
		{
			p.trafficConditions = "Light";
			SetTime((long) (p.timeBetweenStops - (.25 * p.timeBetweenStops)));
		}else if(chance >= 25 && chance < 90) {
			p.trafficConditions = "Normal";
			SetTime((long) (p.timeBetweenStops));
		}else {
			p.trafficConditions = "Heavy";
			SetTime((long) (1.5 * p.timeBetweenStops));
		}
	}
	
	// Adds nanosec to old time
	public void SetTime(long nanosec)
	{
		LocalDateTime time = now.plusNanos(1000000000*nanosec);
		time.format(dtf);
		now = time;
	}
	
	// Checks if an accident has occured
	public void CheckAccident(int chance)
	{
		if(chance < 11)
		{
			System.out.println("An accident occured");
			SetTime(10);
			p.timestamp = now.toString();
			Accident = true;
		}
	}
	
	// Returns the time difference between the current and old time
	public String GetTimeStampBetween(LocalDateTime old)
	{
		String res = "";
		LocalDateTime temp = LocalDateTime.from(old);
		
		long time = temp.until(this.now, ChronoUnit.HOURS);
		temp = temp.plusHours(time);
		res += time + ":";
		
		time = temp.until(this.now, ChronoUnit.MINUTES);
		temp = temp.plusMinutes(time);
		res += time + ":";
		
		time = temp.until(this.now, ChronoUnit.SECONDS);
		temp = temp.plusSeconds(time);
		res += time;
		
		return res;
	}
	
	// Posts an accident
	public void PostAccident()
	{
		Accident acc = new Accident();
		acc.stopNumber = p.stopNumber;
		acc.route = p.route;
		acc.timestamp = p.timestamp;
		acc.vehicle = p.vehicle;
		
		accident.SetAccident(acc);
		System.out.println(p.vehicle + " published a accident message at stop # " + p.stopNumber + " on route " + p.route + "at " + p.timestamp);
		Accident = false;
	}
	
	// post a stop
	public void Stop(int boarding, String time)
	{
		pos.SetPosition(boarding, time, p);
		System.out.println(p.vehicle + " published a position message at stop # " + p.stopNumber + " on route " + p.route + "at " + p.timestamp);
		
	}
	
}
