package client;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import other.Event;
import other.Topic;
import other.Utils;

import com.google.gson.JsonSyntaxException;

/**
 * This class implements all the functionalities that support the agent's UI. 
 * @author kchandan
 *
 */
public class PubSubAgent extends Client{
	
	private RecNotifThread recNotThread; // Reference to the Notification Thread 
	
	public PubSubAgent(int id, String ipAddress){
		super(id, ipAddress);
		//this.eventsQ = new LinkedList<>();
		recNotThread = new RecNotifThread(null);
		recNotThread.start();
	//pullNotifications();
	}
	
	public void stopNotifThread(){
		recNotThread.stopRecNotif();
	}
	
	public Queue<Event> pullNotifications(){
		openSocket();
		printWriter.println(Utils.CMD_PULLNOTIFS);
		printWriter.flush();
		
		Queue<Event> missedEvents = new LinkedList<>();
		
		String responseStr;
		while(true){
			try{
				responseStr = bReader.readLine();
				missedEvents.add(Utils.gson.fromJson(responseStr, Event.class));
				//System.out.println("\n" + "Notification --> " + responseStr);
			} catch (JsonSyntaxException | IOException exception){
				//System.out.println("IO or JsonSyntax Exception in Client:pullNotifications()");
				//System.out.println(exception);
				//e.printStackTrace();
				break;
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("IOException in Client:pullNotifications() while closing socket");
					System.out.println(e);
					//e.printStackTrace();
				}
			}
		}
		return missedEvents;
	}
	
	public boolean subscribe(String topicName) {
		return sendReqAndRecResp(Utils.CMD_SUB, topicName);
	}
	
	public boolean unsubscribe(String topicName) {
		return sendReqAndRecResp(Utils.CMD_UNSUB, topicName);
	}
	
	private List<String> getList(String cmd){
		openSocket();
		printWriter.println(cmd);
		printWriter.flush();
		
		List<String> list = null;
		try {
			list = (List<String>)Utils.gson.fromJson(bReader.readLine(), List.class);
			bReader.readLine();
		} catch (JsonSyntaxException e) {
			// reaches here only when the sent string is not a gson list
			//e.printStackTrace();
		} catch (IOException e) {
			list = null;
			System.out.println("IOException in PubSubAgent: getList()");
			//System.out.println(e);
			//e.printStackTrace();
		}
		finally{
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("IOException in PubSubAgent: getList() while closing socket");
				//e.printStackTrace();
			}
		}
		return list;
	}
	
	public List<String> getSubscriptions(){
		return getList(Utils.CMD_LISTSUBTOPICS);
	}

	public boolean subscribeKW(String kw) {
		return sendReqAndRecResp(Utils.CMD_SUBKW, kw);
	}
	
	public List<String> getAdvertisements(){
		return getList(Utils.CMD_LISTTOPICS);
	}
	
	public List<String> getSubscribedKWs(){
		return getList(Utils.CMD_LISTSUBKWS);
	}

	/*public boolean closeConn(){
		send(Utils.CMD_EXIT); 
		flush();
		if(readLine().equals(Utils.STATUS_OKAY)) //server sends back a smiley if it created the topic successfully.
		{	
			if(super.closeConn()){
				System.out.println("Connections closed ...");
				return true;
			} else { 
				System.out.println("Problem closing socket at client ...");
				return false;
			}
		}
		System.out.println("Problem closing socket at server ...");
		return false;
	}*/

	public boolean publish(String topicName, String title, String content, String[] kws) {
		return sendReqAndRecResp(Utils.CMD_EVENT, 
				Utils.gson.toJson(new Event(getId(), topicName, title, content, kws)));
	}

	public boolean advertise(String topicName) {
		return sendReqAndRecResp(Utils.CMD_TOPIC, Utils.gson.toJson(new Topic(getId(), topicName)));
	}

	public boolean unSubscribeKW(String kw) {
		return sendReqAndRecResp(Utils.CMD_UNSUBKW, kw);
	}

	public boolean unsubscribe() {
		return sendReqAndRecResp(Utils.CMD_UNSUBALL, null);
	}
}
