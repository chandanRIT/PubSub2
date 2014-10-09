package server;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import other.Event;
import other.Topic;
import other.Utils;
import client.PubSubAgent;

public class EventManager{
	static Map<String, Topic> topics = new HashMap<>(); //map between topicNames and Topic obkects
	static Map<String, List<Integer>> topicsSubsMap = new HashMap<>(); //map between topicNames and their Subscriber clientIds
	//static Map<String, List<Event>> topicsEventsMap = new HashMap<>();
	static BlockingQueue<Event> blockingEventsQ = new LinkedBlockingQueue<>(); // the events queue to which events are added and dispatched from continuously
	static Map<Integer, InetAddress> idIpMap = new HashMap<>(); //map between clientIds and their ipaddresses 
	static Map<Integer, Queue<Event>> pendingNotifMap = new HashMap<>(); //map between clientIds and their associated pending EventsQ 
	
	/*
	 * Start the repo service
	 */
	private void startService() {
		Runnable mts = new MultiThreadedServer();
		new Thread(mts).start();
		new Thread(){ //start the notification thread right away
			public void run(){
				while(true){
					try {
						Event event = blockingEventsQ.take();
						notifySubscribers(event);
						
					} catch (InterruptedException e) {
						System.out.println("InterrupedExcep in EventManager.Anonymousthread:run()");
						System.out.println(e);
						//e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/*
	 * notify all subscribers of new event 
	 */
	private static void notifySubscribers(Event event) {
		String topicName = event.getItsTopic();
		List<Integer> tsubs;
		synchronized (topicsSubsMap) {
			tsubs = topicsSubsMap.get(topicName);
			if(tsubs==null) return;
			tsubs = new ArrayList<Integer>(tsubs); //get the list of associated subs
		}
		System.out.println("Subscribers list for topic: "+ event.getItsTopic()+ " are "+ tsubs);
		
		for(Integer i : tsubs){
			try {
				Socket notifSocket = new Socket(idIpMap.get(i),Utils.CLIENT_NOTIF_PORT);
				PrintWriter pw = new PrintWriter(notifSocket.getOutputStream());
				pw.println(Utils.gson.toJson(event));
				pw.flush();
				notifSocket.close(); //closes the EM's end of the socket
			} catch (IOException e) {
				Queue<Event> pendingQ;
				if((pendingQ = pendingNotifMap.get(i)) == null){
					pendingNotifMap.put(i, pendingQ = new LinkedList<>());
				}
				pendingQ.add(event);
				//e.printStackTrace();
			}
		}
	}
	
	/*
	 * add new topic when received advertisement of new topic
	 */
	public static boolean addTopic(Topic topic){
		if( topics.containsKey(topic.getName()) ) return false; //Topic already exists. 
		topics.put(topic.getName(), topic);
		//topicsSubsMap.put(topic, null);
		//topicsEventsMap.put(topic.getName(), new ArrayList<Event>());
		Utils.debug(Utils.gson.toJson(topic));
		return true;
	}
	
	// this method would probably be not needed if we notify subscribers on every event (maybe via callbacks)
	public static boolean addEvent(Event event){
		if(! topics.containsKey(event.getItsTopic())) return false; //Invalid topic 
		blockingEventsQ.add(event);
		/*
		List<Event> eventsList = topicsEventsMap.get(event.getItsTopic()); 
		if(eventsList == null) {
			eventsList = new ArrayList<>();
			topicsEventsMap.put(event.getItsTopic(), eventsList);
		}
		eventsList.add(event);
		*/
		Utils.debug(Utils.gson.toJson(event));
		return true;
	}
	
	/*
	 * add subscriber to the internal list
	 */
	public static  boolean addSubscriber(String topicName, int subId, InetAddress ipAddress){
		if(!topics.containsKey(topicName)) return false;
		List<Integer> subscribersList;
		synchronized (topicsSubsMap) {
			if(( subscribersList = topicsSubsMap.get(topicName) ) == null){
				topicsSubsMap.put(topicName, subscribersList = new ArrayList<>());
			}
			subscribersList.add(subId);
		}
		idIpMap.put(subId, ipAddress);
		Utils.debug("Subscriber:" + subId + " subscribed to topic: "+ topicName);
		return true;
	}
	
	/*
	 * remove subscriber from the list
	 */
	public static boolean removeSubscriber(int subId){
		return false;
	}
	
	public static Queue<Event> clearPendingEventsForClient(int id){
		Queue<Event> eventsQ = pendingNotifMap.get(id);
		if(eventsQ == null) return null;
		pendingNotifMap.put(id, new LinkedList<Event>());
		return eventsQ;
	}
	
	/*
	 * show the list of subscriber for a specified topic
	 */
	public static List<PubSubAgent> showSubscribers(Topic topic){
		return null;
	}
	
	public static void main(String[] args) {
		new EventManager().startService();
	}


}
