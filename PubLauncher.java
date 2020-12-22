import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Properties;


public class PubLauncher
{
	private static int RouteCount,BusCount;
	private static String Route1N, Route2N;
	private static int hTraffic = 0;
	private static int lTraffic = 0;
	private static int nTraffic = 0;
	private static int acc = 0;
	private static  PubThread[] threads = new PubThread[6];
	
	public static void main(String[] args)
	{
		InitializeComponents();
		StartThreads();
	}
	
	// Starst all the threads/buses and waits for threads to terminate
	public static void StartThreads()
	{
		try
		{
			System.out.println("All busues have started. Waiting for them to finish...");
			for(int i = 0; i < threads.length; i++)
			{
				threads[i].start();
			}
			for(int i = 0; i < threads.length; i++)
			{
				threads[i].join();
			}
			System.out.println("All busues have finished.");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	// Initializes feilds
	public static void InitializeComponents()
	{
		String path = FileSystems.getDefault().getPath("pub.properties").toAbsolutePath().toString();
		try(InputStream file = new FileInputStream(path))
		{
			// Create and open the prop file
			Properties propFile = new Properties();
			propFile.load(file);
			
			
			// Init fields
			RouteCount = Integer.parseInt((String)propFile.get("numRoutes"));
			
			BusCount = Integer.parseInt((String)propFile.get("numVehicles"));
			
			Route1N = (String)propFile.get("route1");
			
			Route2N = (String)propFile.get("route2");
			
			lTraffic = Integer.parseInt((String)propFile.get("ltraf"));
			hTraffic = Integer.parseInt((String)propFile.get("htraf"));
			nTraffic = Integer.parseInt((String)propFile.get("ntraf"));
			acc = Integer.parseInt((String)propFile.get("acc"));
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			Position p = new Position();
			
			// Common attr
			p.timestamp = now.toString();
			p.trafficConditions = "Normal";
			p.fillInRation = 0;
			p.route = Route1N;
			p.stopNumber = 1;
			
			
			// Route 1 common attr
			p.numStops = Integer.parseInt((String)propFile.get("route1numStops"));
			p.timeBetweenStops = Integer.parseInt((String)propFile.get("route1TimeBetweenStops"));
			p.vehicle = (String)propFile.get("route1Vehicle1");
			threads[0] = new PubThread(p,now,lTraffic,hTraffic,nTraffic,acc);
			
			p.vehicle = (String)propFile.get("route1Vehicle2");
			threads[1] = new PubThread(p,now,lTraffic,hTraffic,nTraffic,acc);
			
			p.vehicle = (String)propFile.get("route1Vehicle3");
			threads[2] = new PubThread(p,now,lTraffic,hTraffic,nTraffic,acc);
			
			// Route 2 common attr
			p.numStops = Integer.parseInt((String)propFile.get("route2numStops"));
			p.timeBetweenStops = Integer.parseInt((String)propFile.get("route2TimeBetweenStops"));
			p.route = Route2N;
			
			p.vehicle = (String)propFile.get("route2Vehicle1");
			threads[3] = new PubThread(p,now,lTraffic,hTraffic,nTraffic,acc);


			p.vehicle = (String)propFile.get("route2Vehicle2");
			threads[4] = new PubThread(p,now,lTraffic,hTraffic,nTraffic,acc);
			
			p.vehicle = (String)propFile.get("route2Vehicle3");
			threads[5] = new PubThread(p,now,lTraffic,hTraffic,nTraffic,acc);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Path: " + path);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
