import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderListener;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.ContentFilteredTopic;
import com.rti.dds.topic.Topic;

class PositionFilter extends Thread{
	
	Passanger p;
	
	public PositionFilter(Passanger p)
	{
		super();
		this.p = p;
	}
	
	
	public void run()
	{
		subscriberMain(0,0,1);
	}
	
	public void subscriberMain(int domainId, int sampleCount, int filterC) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Topic topic = null;
        DataReaderListener listener = null;
        PositionDataReader reader = null;

        try {

            // --- Create participant --- //

            /* To customize participant QoS, use
            the configuration file
            USER_QOS_PROFILES.xml */

            participant = DomainParticipantFactory.TheParticipantFactory.create_participant(0, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }                         

            // --- Create subscriber --- //

            /* To customize subscriber QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            subscriber = participant.create_subscriber(DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,StatusKind.STATUS_MASK_NONE);
            if (subscriber == null) {
                System.err.println("create_subscriber error\n");
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
            
            String param_list[] = {"vehicle"};
            /* Sequence of parameters for the content filter expression */
            StringSeq parameters = new StringSeq(java.util.Arrays.asList(param_list));
            
            ContentFilteredTopic cft = null;
            if (filterC == 1) {
                cft = participant.create_contentfilteredtopic_with_filter("ContentFilteredTopic", topic, "route MATCH "+p.waitingAtRoute+"", parameters, DomainParticipant.STRINGMATCHFILTER_NAME);
                //cft = participant.create_contentfilteredtopic_with_filter("ContentFilteredTopic", topic, "stopNumber MATCH %0", parameters, DomainParticipant.STRINGMATCHFILTER_NAME);

                if (cft == null) {
                    System.err.println("create_contentfilteredtopic error\n");
                    return;
                }
            }

            // --- Create reader --- //

            listener = new PositionListener();

            if (filterC == 1) {
                reader = (PositionDataReader)
                    subscriber.create_datareader(
                        cft, Subscriber.DATAREADER_QOS_DEFAULT, listener,
                        StatusKind.STATUS_MASK_ALL);
            } else {
            	reader = (PositionDataReader)
                        subscriber.create_datareader(
                            topic, Subscriber.DATAREADER_QOS_DEFAULT, listener,
                            StatusKind.STATUS_MASK_ALL);
            }
            if (reader == null) {
                System.err.println("create_datareader error\n");
                return;
            }                         

            // --- Wait for data --- //
            if(p.leftBus == false)
            {
            	if(p.onBoard == true)
                {
                	//filterC = 0;
                	try {
                		cft.remove_from_expression_parameter(0, p.waitingAtRoute);
                        cft.append_to_expression_parameter(0, p.onBus);
                    } catch (Exception e) {
                        System.err.println("append_to_expression_parameter "
                                + "error");
                        return;
                    }
                }
            	
            }else {
            	if(participant != null) {
                    participant.delete_contained_entities();

                    DomainParticipantFactory.TheParticipantFactory.
                    delete_participant(participant);
                }
            }

            final long receivePeriodSec = 0;

            for (int count = 0;
            (sampleCount == 0) || (count < sampleCount);
            ++count) {
                //System.out.println("Position subscriber sleeping for "
                //+ receivePeriodSec + " sec...");
            	
            	if(p.leftBus == true)
            	{
            		break;
            	}
            	
                try {
                    Thread.sleep(receivePeriodSec * 1000);  // in millisec
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }
        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Data Distribution Service provides the finalize_instance()
            method for users who want to release memory used by the
            participant factory singleton. Uncomment the following block of
            code for clean destruction of the participant factory
            singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
	

    // -----------------------------------------------------------------------
    // Private Types
    // -----------------------------------------------------------------------

    // =======================================================================

    private class PositionListener extends DataReaderAdapter {

        PositionSeq _dataSeq = new PositionSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        synchronized public void on_data_available(DataReader reader) {
            PositionDataReader PositionReader =
            (PositionDataReader)reader;

            try {
                PositionReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        Position po = ((Position)_dataSeq.get(i));
                        
                        if(p.onBoard == false)
                        {
                        	PrintInfo(po);
                        	
                        	if(po.stopNumber == p.waitingAtStop)
                            {
                            	p.onBus = po.vehicle;
                            	p.onBoard = true;
                            	
                            	System.out.println("Passanger # "+p.ID +" is on board bus " + po.vehicle);
                            	PrintInfo(po);
                            }
                        	
                        }else if(po.vehicle.equals(p.onBus))
                        {
                        	 PrintInfo(po);
                        	 if(po.stopNumber == p.destination && p.onBoard == true)
                             {
                             	System.out.println("Passanger # "+p.ID +" is getting off bus " + po.vehicle);
                             	p.leftBus = true;
                             }
                        }
                  
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                PositionReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }

    
    private void PrintInfo(Position po)
    {
    	if(po.trafficConditions.equals("Light") || po.trafficConditions.equals("Heavy") )
        {
        	System.out.println("Position \t"+ po.route + "\t" + po.vehicle+"     "+po.trafficConditions+ "     "+po.stopNumber+"   "+po.numStops+"         "+ po.timeBetweenStops+" \t          "+po.fillInRation+"              "+po.timestamp);     

        }else {
        	System.out.println("Position \t"+ po.route + "\t" + po.vehicle+"     "+po.trafficConditions+ "    "+po.stopNumber+"   "+po.numStops+"         "+ po.timeBetweenStops+" \t          "+po.fillInRation+"               "+po.timestamp);     
        }
    }
}
