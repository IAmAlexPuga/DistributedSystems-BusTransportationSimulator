/*
* (c) Copyright, Real-Time Innovations, 2012.  All rights reserved.
* RTI grants Licensee a license to use, modify, compile, and create derivative
* works of the software solely for use with RTI Connext DDS. Licensee may
* redistribute copies of the software provided that all such copies are subject
* to this license. The software is provided "as is", with no warranty of any
* type, including any warranty for fitness for any purpose. RTI is under no
* obligation to maintain or support the software. RTI shall not be liable for
* any incidental or consequential damages arising out of the use or inability
* to use the software.
*/

/* PositionPublisher.java

A publication of data of type Position

This file is derived from code automatically generated by the rtiddsgen 
command:

rtiddsgen -language java -example <arch> .idl

Example publication of type Position automatically generated by 
'rtiddsgen' To test it, follow these steps:

(1) Compile this file and the example subscription.

(2) Start the subscription on the same domain used for RTI Connext with the command
java PositionSubscriber <domain_id> <sample_count>

(3) Start the publication on the same domain used for RTI Connext with the command
java PositionPublisher <domain_id> <sample_count>

(4) [Optional] Specify the list of discovery initial peers and 
multicast receive addresses via an environment variable or a file 
(in the current working directory) called NDDS_DISCOVERY_PEERS.  

You can run any number of publisher and subscriber programs, and can 
add and remove them dynamically from the domain.

Example:

To run the example application on domain <domain_id>:

Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
Java.                       

On Unix: 
add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
variable

On Windows:
add %NDDSHOME%\lib\<arch> to the 'Path' environment variable

Run the Java applications:

java -Djava.ext.dirs=$NDDSHOME/lib/java PositionPublisher <domain_id>

java -Djava.ext.dirs=$NDDSHOME/lib/java PositionSubscriber <domain_id>        
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class PositionPublisher {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------
	// Sets the new Position
	public void SetPosition(int boarding, String time, Position p) {
		// TODO Auto-generated method stub
		this.p = p;
		this.boarding = boarding;
		this.timeStampBetween = time;
	}
	
	// Publishes data
	public static void PublishData(int domainId, int sampleCount)
	{
		publisherMain(domainId, sampleCount);
	}
	
	static Position p;
	static String timeStampBetween = "";
	static int boarding = 0;
	
    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */

    }

    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    // --- Constructors: -----------------------------------------------------

    PositionPublisher() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        PositionDataWriter writer = null;

        try {
            // --- Create participant --- //

            /* To customize participant QoS, use
            the configuration file
            USER_QOS_PROFILES.xml */

            participant = DomainParticipantFactory.TheParticipantFactory.
            create_participant(
                domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }        

            // --- Create publisher --- //

            /* To customize publisher QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   

            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = PositionTypeSupport.get_type_name();
            PositionTypeSupport.register_type(participant, typeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            topic = participant.create_topic(
                "P3464_EECS_apuga: PT/POS",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           

            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            writer = (PositionDataWriter)
            publisher.create_datawriter(
                topic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            // --- Write --- //

            /* Create data sample for writing */
            Position instance = new Position();

            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            /* For a data type that has a key, if the same instance is going to be
            written multiple times, initialize the key here
            and register the keyed instance prior to writing */
            //instance_handle = writer.register_instance(instance);

            final long sendPeriodMillis = 0 * 1000;

            for (int count = 0;
            (sampleCount == 0) || (count < sampleCount);
            ++count) {
                System.out.println("Writing Position, count " + count);

                /* Modify the instance to be written here */
                
                
                /* Write data */
                writer.write(p, instance_handle);
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }

            //writer.unregister_instance(instance, instance_handle);

        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Data Distribution Service provides finalize_instance()
            method for people who want to release memory used by the
            participant factory singleton. Uncomment the following block of
            code for clean destruction of the participant factory
            singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }

}

