import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;

import com.Avro.BroadcastMessage;
import com.Avro.msgfmt;

// This class is called every 5sec to calculate the throughput 
class DisplayMsgCount extends TimerTask {
	static int c=0,p=0;
	
	public void run() {
		
       System.out.println("----------------------Number of Publication in 5sec "+c+" - "+p+"="+(c-p));
       p=c;
    }
}



public class Publisher extends Thread{
	 Timer timer;
	 Session session;
	 CountDownLatch messageLatch;
	 String Topic;
	 public static int msg_count=0;
	 public int prev_msg_count=0;
	 //Constructor to assign all the value to the local variables
	Publisher(Session session,CountDownLatch messageLatch,String topic,int Type)
	{
		this.timer=timer;
		this.session=session;
		this.messageLatch=messageLatch;
		Topic=topic;
		Timer sche = new Timer();
		sche.schedule(new DisplayMsgCount(), 0, 5000);
	}
	//Start the publisher thread
	public void run()
	{
		Timestamp tt=new Timestamp(System.currentTimeMillis());
		MessageFormat MF=new MessageFormat();
		java.util.Date date;
		while(true)
		{
			try {
	            	//Populate the random message and call the BroadcastMessage class to send the message 
					BroadcastMessage BM=new BroadcastMessage(session,""+UUID.randomUUID(),Topic,1,""+System.nanoTime(),"send.avro");
					msg_count=msg_count+1;
    	            DisplayMsgCount.c=DisplayMsgCount.c+1;
    	          
	        	} 
			//catch when faild to reach eventbus 
	        catch(IllegalStateException i)
			{
	        	this.stop();
				System.out.println("Event bus Unreachable...");
			}
		}
		
	}
}