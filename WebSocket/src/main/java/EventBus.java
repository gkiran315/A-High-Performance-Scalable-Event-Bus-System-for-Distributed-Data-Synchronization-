
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.map.MultiValueMap;
import org.glassfish.tyrus.server.Server;

import com.Avro.BroadcastMessage;
import com.Avro.msgfmt;


//Used to generate the current timestamp
class GetTimestamp  extends TimerTask{
	java.util.Date date;
	//EventBus EB=new EventBus();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		date=new java.util.Date();
		EventBus.time=new Timestamp(date.getTime());
	}
	
}

//This class is called every 5sec to calculate the throughput of the EventBus
class DisplayMsgCount1 extends TimerTask {
	static int c=0,p=0;
	
	public void run() {
		System.out.println("-----Event-----------------Number of Publication in 5sec "+c+" - "+p+"="+(c-p));
		//Send the eventbus change signal if throughtput reach 7000 
		if((c-p)>=7000)
		{
			System.out.println("Connected to another Bus...");
			EventBus.ChangeEventBus();
		}
		p=c;
    }
}

public class EventBus 
{
    static Server server = null;
    static Options options = new Options(); 
    static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
    static Set<Session> clients_pub = Collections.synchronizedSet(new HashSet<Session>());
    private static List list;
    static MultiValueMap map = new MultiValueMap();
    static MultiValueMap map1 = new MultiValueMap();
    static MultiValueMap Clients_list = new MultiValueMap();
    private static MultiValueMap message_datamap = new MultiValueMap();
    static Timestamp time;
    public static int msg_count=0;
	public int prev_msg_count=0;
	static int port_num;
    public EventBus()
    {
    	Timer sche = new Timer();
		sche.schedule(new DisplayMsgCount1(), 0, 5000);
    }
    private static void buildOptions() {
        // build option tables
        options.addOption(new Option("help", "print this message"));
        options.addOption(Option.builder("port").hasArg()
                .desc("port number")
                .build());
        options.addOption(Option.builder("ip").hasArg()
                .desc("external ip address")
                .build());
    }
    public static String[] parseArgs(String[] args) {
        String[] rst = new String[2];
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("indexing server", options);
                System.exit(0);
            }
            rst[0] = line.getOptionValue("ip", "127.0.0.1");
            rst[1] = line.getOptionValue("port", "8080");
        } catch (ParseException exp) {
            System.out.println("Arguments Error:" + exp.getMessage());
            System.exit(-1);
        }
        return rst;
    }


    //add the subscribers to the list
    public void add_subscriber(Session session)
    {
    	
    	if(!clients.contains(session))
    	{
    		System.out.println("Added Client "+session.getId());
        	clients.add(session);
    	}

    }
    //This is called when onMessage event is triggered
    public void Handle_message(String message, Session session){
    	//Read message from the send buffer and deserializ it
    	File avroOutput = new File("send.avro");
    	msgfmt mf_reader= null;
		try 
		{
			DatumReader<msgfmt> messageformateDatumReader = new SpecificDatumReader<msgfmt>(msgfmt.class);
			DataFileReader<msgfmt> dataFileReader = new DataFileReader<msgfmt>(avroOutput, messageformateDatumReader);
			mf_reader = dataFileReader.next(mf_reader);
		} 
		catch(NoSuchElementException nse){}
		catch(IOException e) {}
		msg_count=msg_count+1;
        DisplayMsgCount1.c=DisplayMsgCount1.c+1;
        try
			{
        		//Check if the message is for registration
				if(mf_reader.getType().equals(2))
				{
					//Register multiple times of the client is requested for multiple topic's
					String[] split=((String) mf_reader.getTopic()).split(",");
			    	for (String m: split)
		            {
			    		if((mf_reader.getType()).equals(2))
						{
			    			Add_to_Subscriber_list(session.getId(),m);
						}
		            }
				}
				if(mf_reader.getType().equals(1))
				{
			    	String message_topic=(String)mf_reader.getTopic();
			    	String msg=(String)mf_reader.getMessage();
			    	String[] split=message_topic.split(",");
			    	Add_Message_to_list(mf_reader);
			    	//Loop for each topic the publisher has published the message
		        	for (String m: split)
		            {
				    	synchronized(clients)
				    	{
				    		//Loop for each client that are registred for that topic
				    		for(Session client : clients)
				    		{
				    			Set entrySet = map.entrySet();
				    			Iterator it = entrySet.iterator();
			    	            list = (List) map.get(client.getId());
			    	            if(list!=null)
			    	            {
			    	            	for (int j = 0; j < list.size(); j++) 
			    	            	{
				    	                if (!client.equals(session) && list.get(j).equals(m))
				      	    	        {
				    	                	//Serialize the message and write into receiver buffer and sent message to the subxcriber
				    	                	File avroinput = new File("Receive.avro");
				                    		try {
			                    					DatumWriter<msgfmt> messageformateDatumWriter = new SpecificDatumWriter<msgfmt>(msgfmt.class);
			                    					DataFileWriter<msgfmt> dataFileWriter = new DataFileWriter<msgfmt>(messageformateDatumWriter);
			                    					dataFileWriter.create(mf_reader.getSchema(), avroinput);
			                    					dataFileWriter.append(mf_reader);
			                    					//System.out.println(mf_reader);
			                    					client.getBasicRemote().sendObject(dataFileWriter);
			                    					dataFileWriter.close();
			                    				} 
				                    			catch (IOException e){}
				                    			catch (EncodeException e) 
				                    			{
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
				                    		}
				    	            	}
			    	            	}
				    			}
				    		}
		            	}
					}
				}
				catch(NullPointerException ne){}
	}
    //Add the client session information into the hashmap
    public void Add_Clients_list(Session session,String m)
    {
    	if(Clients_list.containsValue(m, session))
    		System.out.println("--");
    	else
    		Clients_list.put(m, session.getId());
    }
    //Add the message to the hashmap to handle fault tolerance
    public void Add_Message_to_list(msgfmt mf_reader)
    {
    	 
		//try 
		{
			java.sql.Timestamp ts = java.sql.Timestamp.valueOf((String) mf_reader.getTime() );
	    	message_datamap.put(ts ,mf_reader.getMessage());
	    } //catch (java.text.ParseException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
        
    }
    //Get all the message from the list which are publisher between timestamp t and current timestamp
    public void Get_Message_to_list(Timestamp t,MessageFormat MF,Session session)
    {
    	Set entrySet = message_datamap.entrySet();
        Iterator it = entrySet.iterator();
        System.out.println("*********************"+session.getId());
    	while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
            list = (List) message_datamap.get(mapEntry.getKey());
            if(list!=null)
            {
	            MessageFormat M=new MessageFormat();
	            if(t.after((Timestamp) mapEntry.getKey()))
	            {
	            	for (int j = 0; j < list.size(); j++) {
	                	M=(MessageFormat) list.get(j);
	                	try {
							//System.out.println("------------------- "+session.getId());
	                		//session.getBasicRemote().sendText("----------------------------"+M.Message);
	                		OutputStream file_Receive = new FileOutputStream("Receive.ser");
	        	            OutputStream buffer_Receive = new BufferedOutputStream(file_Receive);
	        	        	//ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	        	        	ObjectOutputStream out_Receive = new ObjectOutputStream(buffer_Receive);
	        	        	out_Receive.writeObject(MF);
	        	        	session.getBasicRemote().sendObject(out_Receive);
	        	        	out_Receive.flush();
	        	        	out_Receive.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                   // System.out.println("\t" + mapEntry.getKey() + "\t  " +M.Message+"   "+M.Topic+ "   ");
	                	catch (EncodeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                }
	            }
            }
            
        }
    	
    }
    //Remove sessio information from the list once the client connect to other eventbus
    public static void remove_session(Session session)
    {
    	clients.remove(session);
    	map.remove(session);
    	Clients_list.remove(session.getId());
    	System.out.println("Removed Session "+session.getId());
    }
    //Broadcast change eventbus to client with message type code 3
    public static void ChangeEventBus()
    {
    	synchronized(clients){
    		for(Session client : clients){
  	    	  System.out.println(client.getId());
  	    	  BroadcastMessage BM=new BroadcastMessage(client,"Change Bus","Topic",3,""+new Timestamp(System.currentTimeMillis()),"Receive.avro");
    		}
  	     }
    }
    //Adding Client and topic to the list
    public void Add_to_Subscriber_list(String session,String topic)
    {
    	if(!map.containsValue(session, topic))
    	{
    		System.out.println("[SERVER RECV] Session " + session + " Registred Topic : " + topic);
    		System.out.println("Added "+session);
    		map.put(session, topic);
    	}
		
    	/*Set entrySet = map.entrySet();
        Iterator it = entrySet.iterator();
        
    	while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
            list = (List) map.get(mapEntry.getKey());
            for (int j = 0; j < list.size(); j++) {
                //System.out.println("\t" + mapEntry.getKey() + "\t  " + list.get(j));
            }
        }*/
    	
    }
    
    //public static String[] args1;
    public static void main( String[] args )
    {
    	EventBusConfig config = EventBusConfig.getInstance();
        buildOptions();
        //The port that we should run on can be set into an environment variable
        //Look for that variable and default to 8080 if it isn't there.
        String[] argValues = parseArgs(args);
        config.setIp_addr(argValues[0]);
        config.setPort_num(Integer.valueOf(argValues[1]));
        port_num=Integer.parseInt(args[0]);
        server = new Server(config.getIp_addr(), port_num, "/websockets", null, ServerEndPoint.class);
        
        try {
            server.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Event Bus Started..");
            System.out.println("Please press a key to stop");
            reader.readLine();
        }
        catch(DeploymentException DE)
        {
        	DE.printStackTrace();
        	System.out.println("Another instance of Event Bus is running");
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}
