import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.apache.commons.collections.map.MultiValueMap;


public class UnicastPreviousMessages extends Thread{
	static MultiValueMap message_datamap = new MultiValueMap();
	Session session;
	static List list;
	Timestamp t;
	MessageFormat MF=new MessageFormat();
	UnicastPreviousMessages(MultiValueMap message_datamap,Timestamp t,MessageFormat MF,Session session)
    {
		this.message_datamap=message_datamap;
		this.session=session;
    	this.t=t;
    	this.MF=MF;
    }
	public void run()
	{
    	Set entrySet1 = message_datamap.entrySet();
        Iterator it1 = entrySet1.iterator();
        System.out.println("*********************"+session.getId());
    	while (it1.hasNext()) {
            Map.Entry mapEntry_temp = (Map.Entry) it1.next();
            list = (List) message_datamap.get(mapEntry_temp.getKey());
            if(list!=null)
            {
	            MessageFormat M=new MessageFormat();
	            if(t.after((Timestamp) mapEntry_temp.getKey()))
	            {
	            	for (int j = 0; j < list.size(); j++) {
	                	M=(MessageFormat) list.get(j);
	                	try {
							//System.out.println("------------------- "+session.getId());
	                		//session.getBasicRemote().sendText("----------------------------"+M.Message);
	                		OutputStream file2 = new FileOutputStream("quarks1.ser");
	        	            OutputStream buffer2 = new BufferedOutputStream(file2);
	        	        	//ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	        	        	ObjectOutputStream out1 = new ObjectOutputStream(buffer2);
	        	        	out1.writeObject(MF);
	        	        	session.getBasicRemote().sendObject(out1);
	        	        	out1.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                   // System.out.println("\t" + mapEntry_temp.getKey() + "\t  " +M.Message+"   "+M.Topic+ "   ");
	                	catch (EncodeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                }
	            }
            }
            
        }
	}
}
