package client;

import java.util.List;

import other.Event;
import other.Topic;
import other.Utils;

public class PubSubAgent extends Client{

	public PubSubAgent(int id, String ipAddress){
		super(id, ipAddress);
	}
	
	public boolean subscribe(String topicName) {
		return sendReqAndRecResp(Utils.CMD_SUB, topicName);
	}
	
	public boolean unsubscribe(String topicName) {
		return sendReqAndRecResp(Utils.CMD_UNSUB, topicName);
	}

	public boolean subscribeKW(String kw) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public List<Topic> listSubscribedTopics() {
		// TODO Auto-generated method stub
		return null;
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

	public boolean publish(String topicName, String eventTitle, String eventContent) {
		return sendReqAndRecResp(Utils.CMD_EVENT, Utils.gson.toJson(new Event(getId(), topicName, eventTitle, eventContent)));
	}

	public boolean advertise(String topicName) {
		return sendReqAndRecResp(Utils.CMD_TOPIC, Utils.gson.toJson(new Topic(getId(), topicName)));
	}

	public boolean unSubscribeKW(String keyword) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean unsubscribe() {
		return sendReqAndRecResp(Utils.CMD_UNSUBALL, null);
	}
}
