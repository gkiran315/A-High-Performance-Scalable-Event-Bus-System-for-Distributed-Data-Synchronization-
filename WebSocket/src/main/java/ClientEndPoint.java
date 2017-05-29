/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * This Class act as the subscriber and executes all the behaviors related to subcriber  
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.glassfish.tyrus.client.ClientManager;

import com.Avro.BroadcastMessage;
import com.Avro.msgfmt;

public class ClientEndPoint {
    private static CountDownLatch messageLatch;
    private static CountDownLatch recvLatch;
    private static final String SENT_MESSAGE = "ACK";
    private static Timer timer;
    public static String port1="8081";
    public static void main(String [] args){
    		String arg1_option1=args[0];
    		final String arg2_topic=args[1];
    		final MessageFormat MF=new MessageFormat();
    		System.out.println("Connected to EvenBus at Port Number "+port1);
    		StartClient(arg1_option1,arg2_topic,port1);

    }    

    //This function is called to start the subscriber
	public static void StartClient(final String arg1_option,final String arg2_topic,final String port){
	    
		try {
	    	//Connecting to websocket at port# defined in "port"
	        String wsAddr = "ws://localhost:"+port+"/websockets/StringEndPoint";
	        
	        messageLatch = new CountDownLatch(10);
	        recvLatch = new CountDownLatch(10);
	        timer = new Timer();
	
	        final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
	
	        ClientManager client = ClientManager.createClient();
	        client.asyncConnectToServer(new Endpoint() {
	        	
	            @Override
	            public void onOpen(final Session session, EndpointConfig config) {
	            	System.out.println("Connected..." );
	            	System.out.println("Session ID "+session.getId() );
	            	int active_ind=1;
	                try {
	                		session.addMessageHandler(new MessageHandler.Whole<String>() {
	                    	//@Override
	                        public void onMessage(String message) {
	                        	File avroInput = new File("Receive.avro");
	                        	msgfmt mf_reader= null;
	                    		try {
	                    				DatumReader<msgfmt> messageformateDatumReader = new SpecificDatumReader<msgfmt>(msgfmt.class);
	                    				DataFileReader<msgfmt> dataFileReader = new DataFileReader<msgfmt>(avroInput, messageformateDatumReader);
	                    				mf_reader = dataFileReader.next(mf_reader);
	                    				//Check is the message is received is of type 3 i.e, change eventbus type
	                    				if((mf_reader.getType()).equals(3))
	                    				{
	                    					System.out.println("*********************************************Event Buss changed*********************************************");
	                    					System.out.println(mf_reader);
	                    					//Check the port# and select the other eventbus's port number
	                    					if(port1=="8081")
	                    					{	
	                    						port1="8080";
	                    						System.out.println("Connected to EvenBus at Port Number "+port1);
	                    						StartClient("2",arg2_topic,"8080");
	                    					}
	                    					else
	                    					{	
	                    						port1="8081";
	                    						System.out.println("Connected to EvenBus at Port Number "+port1);
	                    						StartClient("2",arg2_topic,"8081");  
	                    					}
	                    				}
	                    				else
	                    				{
	                    					System.out.println("Received: Topic "+mf_reader.getTopic()+" Message "+mf_reader.getMessage()+" Published at "+mf_reader.getTime()+" Received at "+System.nanoTime());
	                    				}
	                    			} 
	                    			catch(IOException e){}
	                                recvLatch.countDown();
	                        	}
	                			});
	                			// Register only once when the client is started
	                			if(active_ind==1)
	                			{
	                				System.out.println("Request Sent...");
			                		BroadcastMessage BM=new BroadcastMessage(session,"Register",arg2_topic,2,""+new Timestamp(System.currentTimeMillis()),"send.avro");
			                    	active_ind=0;
	                			}
	                    
	                		} 
	                		catch (Exception ex) 
	                		{
			                    Logger.getLogger(ClientEndPoint.class.getName()).log(Level.SEVERE, null, ex);
			                }
	            	}
	            	}, cec, new URI(wsAddr));
	        
	        		recvLatch.await(100, TimeUnit.SECONDS);
	    		} 
	    	catch (Exception e) 
	    	{
	    		e.printStackTrace();
	    	}
	}
}
