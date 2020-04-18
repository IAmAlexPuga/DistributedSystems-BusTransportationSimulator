import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Properties;

public class PubLauncher
{
	private int RouteCount,BusCount;
	private String Route1N, Route2N;
	private PubThread[] threads = new PubThread[6];
	
	public void Main(String[] args)
	{
		InitializeComponents();
		StartThreads();
	}
	
	public void StartThreads()
	{
		try
		{
			for(int i = 0; i < threads.length; i++)
			{
				threads[i].Start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	// Initializes feilds
	public void InitializeComponents()
	{
		try(InputStream file = new FileInputStream("path/pub.properties"))
		{
			// Create and open the prop file
			Properties propFile = new Properties();
			propFile.load(file);
			
			Enumeration e = propFile.propertyNames();
			
			// Init fields
			String key = (String)e.nextElement();
			RouteCount = (int)propFile.get(key);
			
			key = (String)e.nextElement();
			BusCount = (int)propFile.get(key);
			
			key = (String)e.nextElement();
			Route1N = (String)propFile.get(key);
			
			key = (String)e.nextElement();
			Route2N = (String)propFile.get(key);
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			int count = 0;
			Position p = new Position();
			p.timestamp = now.toString();
			p.trafficConditions = "Normal";
			p.fillInRation = 0;
			// Init the PubThreads
			while(e.hasMoreElements())
			{
				if(count == 0)
				{
					p.route = Route1N;
				}else {
					p.route = Route2N;
				}
				
				p.stopNumber = 0;
				
				key = (String)e.nextElement();
				p.numStops = (int)propFile.get(key);
				
				key = (String)e.nextElement();
				p.timeBetweenStops = (int)propFile.get(key);
				
				key = (String)e.nextElement();
				p.vehicle = (String)propFile.get(key);
				threads[count] = new PubThread(p);
				count++;
				key = (String)e.nextElement();
				p.vehicle = (String)propFile.get(key);
				threads[count] = new PubThread(p);
				count++;
				key = (String)e.nextElement();
				p.vehicle = (String)propFile.get(key);
				threads[count] = new PubThread(p);
				count++;
				
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
