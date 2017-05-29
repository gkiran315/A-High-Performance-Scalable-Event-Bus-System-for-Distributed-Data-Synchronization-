package com.Avro;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
/*
 * This is Reuable class which is used by Eventbus, subscriber and publisher for publishing the message
 */
public class BroadcastMessage {
	Session session;
	String msg;
	String Topic;
	int type;
	String time;
	String file;
	public BroadcastMessage(Session session,String msg,String Topic,int type,String time,String file){
		this.session=session;
		this.msg=msg;
		this.Topic=Topic;
		this.type=type;
		this.time=time;
		this.file=file;
		send();
	}
	/*function is declared as synchronized because at a time only one can access this among Eventbus, subscriber and publisher*/
	public synchronized void send()
	{
		msgfmt mf_writer1=new msgfmt();
		mf_writer1.setMessage(msg);
		mf_writer1.setTopic(Topic);
		mf_writer1.setType(type);	
		mf_writer1.setTime(time);
		File avroOutput = new File(file);
			try {
					//Serialize data into bytestream using Avro
					DatumWriter<msgfmt> messageformateDatumWriter = new SpecificDatumWriter<msgfmt>(msgfmt.class);
					DataFileWriter<msgfmt> dataFileWriter = new DataFileWriter<msgfmt>(messageformateDatumWriter);
					dataFileWriter.create(mf_writer1.getSchema(), avroOutput);
					synchronized(dataFileWriter)
					{
						dataFileWriter.append(mf_writer1);
					}
					session.getBasicRemote().sendObject(dataFileWriter);
					//System.out.println(mf_writer1);
					dataFileWriter.close();
				} 
			catch (IOException e) 
			{
				System.out.println("Error writing Avro");
			} catch (EncodeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
}
