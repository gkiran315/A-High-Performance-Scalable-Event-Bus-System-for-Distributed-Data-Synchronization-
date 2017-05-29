/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Open your web browser, open this link: 
 * http://www.websocket.org/echo.html
 * 
 * And if you see "This web browser support WebSocket", which means you can test
 * this websocket on this web page now. 
 * 
 * In the location part, input
 * ws://localhost:8080/websockets/StringEndPoint
 * 
 * In the message box, type:
 * 
 * Test WebSocket
 * 
 * Press send button, you should be able to see your message in your console. 
 * 
 * In the message box, type:
 * 
 * GET
 * 
 * Press send button, you should be able to see the random messages sent by your
 * java program to the web browser. 
 * 
 * @author arunjeg
 */
@ServerEndpoint("/StringEndPoint")
public class ServerEndPoint {
    private Timer timer;
    
    EventBus eb=new EventBus();
    @OnOpen
    public void onOpen(Session session) {
    	eb.clients.add(session);
    	System.out.println("[SERVER] Open session " + session.getId());
       
    }

    @OnMessage
    public void onMessage(String message, Session session) {
    	try{
    		eb.Handle_message(message,session);
        }
        catch(NullPointerException n){}
    	catch(IllegalStateException i)
        {
    		System.out.println(session.getId()+"  Subscriber Unreachable...");
        }
        catch(Exception e){}
    	
        
    }

    @OnClose
    public void onClose(Session session) {
        try{
        	timer.cancel();
        	eb.clients.remove(session);
        	eb.map.remove(session.getId());
            System.out.println("[SERVER] Session " + session.getId() + " is closed.");
        }
        catch(NullPointerException n)
        {
        	System.out.println("Aborted...");
        }
        catch(Exception e)
        {
        	System.out.println("Aborted...");
        }
        finally
        {
            System.out.println("[SERVER] Session " + session.getId() + " is closed.");
        }
    	
    }
    
    public void ReadFile(Session session){
    	String FILENAME = "D:\\TTU\\Spring 2016\\AOS\\Project 3\\Peer1\\F18.txt";

    		BufferedReader br = null;
    		FileReader fr = null;

    		try {

    			fr = new FileReader(FILENAME);
    			br = new BufferedReader(fr);

    			String sCurrentLine;

    			br = new BufferedReader(new FileReader(FILENAME));

    			while ((sCurrentLine = br.readLine()) != null) {
    				//System.out.println(sCurrentLine+"\n");
    				session.getBasicRemote().sendText(sCurrentLine);
    			}

    		}
    		
    		catch (IOException e) {

    			e.printStackTrace();

    		} finally {

    			try {

    				if (br != null)
    					br.close();

    				if (fr != null)
    					fr.close();

    			} catch (IOException ex) {

    				ex.printStackTrace();

    			}

    		}

    }
    
    public void RandomGen(Session session){
    	try {
            String msg = "Message " + UUID.randomUUID();
            System.out.println("[SERVER SEND] " + msg);
            session.getBasicRemote().sendText(msg);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
