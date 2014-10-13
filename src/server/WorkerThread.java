package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Queue;

import other.Event;
import other.Topic;
import other.Utils;

/**

 */
public class WorkerThread implements Runnable{

	Socket clientSocket;
	String serverText;
	int agentID;

	public WorkerThread(Socket clientSocket, String serverText) {
		this.clientSocket = clientSocket;
		this.serverText   = serverText;
	}

	public void run() {
		try {
			BufferedReader bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter pWriter = new PrintWriter(clientSocket.getOutputStream());
			agentID = Integer.parseInt(bReader.readLine());

			String cmd = bReader.readLine();
			String status = Utils.STATUS_NOKAY;

			switch (cmd){
			case Utils.CMD_TOPIC: 
				if (EventManager.addTopic(Utils.gson.fromJson(bReader.readLine(), Topic.class)) )
					status = Utils.STATUS_OKAY;
				break;

			case Utils.CMD_EVENT:
				if (EventManager.addEvent(Utils.gson.fromJson(bReader.readLine(), Event.class)))
					status = Utils.STATUS_OKAY;
				break;

			case Utils.CMD_SUB:
				if (EventManager.addSubscriber(bReader.readLine(), agentID, clientSocket.getInetAddress()))
					status = Utils.STATUS_OKAY;
				break;
				
			case Utils.CMD_SUBKW:
				if (EventManager.addSubscriberKW(bReader.readLine(), agentID, clientSocket.getInetAddress()))
					status = Utils.STATUS_OKAY;
				break;

			case Utils.CMD_UNSUB:
				if (EventManager.removeSubscriber(agentID, bReader.readLine()))
					status = Utils.STATUS_OKAY;
				break;
				
			case Utils.CMD_UNSUBKW:
				if (EventManager.removeSubscriberKW(agentID, bReader.readLine()))
					status = Utils.STATUS_OKAY;
				break;
				
			case Utils.CMD_UNSUBALL:
				if (EventManager.unSubscribeFromAll(agentID))
					status = Utils.STATUS_OKAY;
				break;
			
			case Utils.CMD_LISTSUBTOPICS:
				List<String> topicNames = EventManager.getSubscriptions(agentID);
				if(topicNames != null)
					pWriter.println(Utils.gson.toJson(topicNames));
				status = Utils.STATUS_OKAY;
				break;
				
			case Utils.CMD_LISTTOPICS:
				List<String> allTopics = EventManager.getAllTopics();
				if(allTopics != null)
					pWriter.println(Utils.gson.toJson(allTopics));
				status = Utils.STATUS_OKAY;
				break;
			
			case Utils.CMD_LISTSUBKWS:
				List<String> kwList = EventManager.getSubscribedKWs(agentID);
				if(kwList != null)
					pWriter.println(Utils.gson.toJson(kwList));
				status = Utils.STATUS_OKAY;
				break;
				
			case Utils.CMD_PULLNOTIFS:
				//System.out.println("received pull request from client "+ agentID);
				Queue<Event> eventsQ = EventManager.clearPendingEventsForClient(agentID);
				if (eventsQ == null) {status = Utils.STATUS_OKAY; break;}
				for (Event event : eventsQ){
					pWriter.println(Utils.gson.toJson(event));
				}
				//System.out.println("EventsQ: " + eventsQ );
				status = Utils.STATUS_OKAY;
				break;
				
			case "EXIT":
				//dont change the status
				break;

			default:
				//status = Utils.STATUS_NOKAY; //it's NOKAY by default
				System.out.println("Invalid cmd string from client");
			}

			pWriter.println(status);
			pWriter.flush();
		} catch (IOException e) { //If there's an IOException then there's no way to send back status
			System.out.println("IOException in WorkerThread:run()");
			System.out.println(e);
			//e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("IOException in WorkerThread: run() while closing socket");
				e.printStackTrace();
			} //this closes any associated readers, streams and thus the entire socket
		}
	}

	//assume the client checks for vaidity or args and strings. So the string which reaches here is good to parse
	/*public String handlePub(BufferedReader bReader) throws IOException{
		String jsonType = bReader.readLine();
		if(jsonType.equals("TOPIC")){
			if ( ! EventManager.addTopic(Utils.gson.fromJson(bReader.readLine(), Topic.class)) )
				return "The Topic already exists";
		}
		else if (jsonType.equals("EVENT"))
			if(! EventManager.addEvent(Utils.gson.fromJson(bReader.readLine(), Event.class)))
				return "Event not published: May be because there's no such topic";
			else {
				System.out.println("Unexpected JSON Type from Publisher");
				return "Bad string from publisher";
			}
		return ":)";
	}*/
}