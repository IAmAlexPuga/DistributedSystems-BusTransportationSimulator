import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;

public class PubThread extends Thread {
	Position p = new Position();
	LocalDateTime now;
	DateTimeFormatter dtf;
	int Cycles = 1;

	PositionPublisher pos;
	AccidentPublisher accident;

	// Positoins
	PositionDataWriter writer = null;

	// Accidents
	AccidentDataWriter a_writer = null;
	
	InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
	DomainParticipant participant = null;

	public PubThread(Position pos, LocalDateTime time) {

		p.fillInRation = pos.fillInRation;
		p.numStops = pos.numStops;
		p.route = pos.route;
		p.stopNumber = pos.stopNumber;
		p.timeBetweenStops = pos.timeBetweenStops;
		p.timestamp = pos.timestamp;
		p.trafficConditions = pos.trafficConditions;
		p.vehicle = pos.vehicle;

		this.now = time;
		this.dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		this.pos = new PositionPublisher();
		this.accident = new AccidentPublisher();

		InitPosVars();
		InitAccVars();

		System.out.println("Thread " + p.vehicle + " Started");
	}

	public void InitAccVars() {
		Publisher publisher = null;
		Topic topic = null;

		if (participant == null) {

			System.err.println("accident create_participant error\n");
			return;
		}

		publisher = participant.create_publisher(DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,StatusKind.STATUS_MASK_NONE);
		if (publisher == null) {
			System.err.println("accident create_publisher error\n");
			return;
		}

		String typeName = AccidentTypeSupport.get_type_name();
		AccidentTypeSupport.register_type(participant, typeName);


		topic = participant.create_topic("Example Accident", typeName, DomainParticipant.TOPIC_QOS_DEFAULT,null /* listener */, StatusKind.STATUS_MASK_NONE);
		if (topic == null) {
			System.err.println("accident create_topic error\n");
			return;
		}

		a_writer = (AccidentDataWriter) publisher.create_datawriter(topic, Publisher.DATAWRITER_QOS_DEFAULT,null /* listener */, StatusKind.STATUS_MASK_NONE);
		if (a_writer == null) {
			System.err.println("accident create_datawriter error\n");
			return;
		}

	}

	public void InitPosVars() {
		Publisher publisher = null;
		Topic topic = null;

		participant = DomainParticipantFactory.TheParticipantFactory.create_participant(0,DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, null /* listener */, StatusKind.STATUS_MASK_NONE);
		if (participant == null) {
			System.err.println("position create_participant error\n");
			return;
		}

		publisher = participant.create_publisher(DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
				StatusKind.STATUS_MASK_NONE);
		if (publisher == null) {

			System.err.println("position create_publisher error\n");
			return;
		}

		String typeName = PositionTypeSupport.get_type_name();
		PositionTypeSupport.register_type(participant, typeName);

		topic = participant.create_topic("Example Position", typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
				null /* listener */, StatusKind.STATUS_MASK_NONE);
		if (topic == null) {
			System.err.println("position create_topic error\n");
			return;
		}

		writer = (PositionDataWriter) publisher.create_datawriter(topic, Publisher.DATAWRITER_QOS_DEFAULT,null /* listener */, StatusKind.STATUS_MASK_NONE);
		if (writer == null) {
			System.err.println("position create_datawriter error\n");
			return;
		}
	}

	public void run() {

		try {
			Random rand = new Random();
			LocalDateTime old;
			int departing = 0;

			while (Cycles < 4) {
				if (p.stopNumber > p.numStops) {
					Cycles++;
					p.stopNumber = 1;
				}
				if(Cycles > 4)
				{
					break;
				}
				old = this.now;
				SetTraffic(rand.nextInt(101) + 1);
				CheckAccident(rand.nextInt(101) + 1);
				if (p.fillInRation > 0) {
					departing = rand.nextInt(p.fillInRation);
				} else {
					departing = 0;
				}
				
				Stop(rand.nextInt(100 - p.fillInRation) - departing, GetTimeStampBetween(old));
				p.stopNumber++;
			}
		} finally {
			
			DeleteParticipant(participant);
		}
	}
	
	// Removes a participant
	public void DeleteParticipant(DomainParticipant participant)
	{
		if (participant != null) {
			participant.delete_contained_entities();

			DomainParticipantFactory.TheParticipantFactory.delete_participant(participant);
		}
	}
	
	// Sets the traffic and ads the set time based on traffic
	public void SetTraffic(int chance) {
		if (chance < 26) {
			p.trafficConditions = "Light";
			SetTime((long) (p.timeBetweenStops - (.25 * p.timeBetweenStops)));
		} else if (chance > 89) {
			p.trafficConditions = "Heavy";
			SetTime((long) (1.5 * p.timeBetweenStops));
		} else {
			p.trafficConditions = "Normal";
			SetTime((long) (p.timeBetweenStops));
		}

	}

	// Adds nanosec to old time
	public void SetTime(long nanosec) {
		LocalDateTime time = now.plusNanos(1000000000 * nanosec);
		time.format(dtf);
		now = time;
	}

	// Checks if an accident has occured
	public void CheckAccident(int chance) {
		if (chance < 11) {
			SetTime(10);
			p.timestamp = now.toString();
			PostAccident();
		}
	}

	// Returns the time difference between the current and old time
	public String GetTimeStampBetween(LocalDateTime old) {
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
	synchronized public void PostAccident() {
		Accident acc = new Accident();
		acc.stopNumber = p.stopNumber;
		acc.route = p.route;
		acc.timestamp = p.timestamp;
		acc.vehicle = p.vehicle;

		System.out.println(p.vehicle + " published a accident message at stop # " + p.stopNumber + " on route "
				+ p.route + " at " + p.timestamp);
		
		a_writer.write(acc, instance_handle);
		try {
			this.currentThread().wait(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// post a stop
	synchronized public void Stop(int boarding, String time) {
		p.fillInRation += boarding;
		System.out.println(p.vehicle + " published a position message at stop # " + p.stopNumber + " on route "
				+ p.route + "at " + p.timestamp);
		
		writer.write(p, instance_handle);
		try {
			this.currentThread().wait(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
