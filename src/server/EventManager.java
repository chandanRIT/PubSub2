package server;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import other.Event;
import client.PubSubAgent;
import other.Topic;
import other.Utils;

public class EventManager{
	static Map<String, Topic> topics = new HashMap<>();
	static Map<String, List<Integer>> topicsSubsMap = new HashMap<>(); //subscribers (PubSubAgent) have integer Ids
	//static Map<String, List<Event>> topicsEventsMap = new HashMap<>();
	static BlockingQueue<Event> blockingEventsQ = new LinkedBlockingQueue<>();
	static Map<Integer, InetAddress> idIpMap = new HashMap<>();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		//notifySubscribers(event);
		return true;
	}
	
	/*
	 * add subscriber to the internal list
	 */
	public static  boolean addSubscriber(String topicName, int subscriberId, InetAddress ipAddress){
		if(!topics.containsKey(topicName)) return false;
		List<Integer> subscribersList;
		synchronized (topicsSubsMap) {
			if(( subscribersList = topicsSubsMap.get(topicName) ) == null){
				topicsSubsMap.put(topicName, subscribersList = new ArrayList<>());
			}
			subscribersList.add(subscriberId);
		}
		idIpMap.put(subscriberId, ipAddress);
		Utils.debug("Subscriber:" + subscriberId + " subscribed to topic: "+ topicName);
		return true;
	}
	
	/*
	 * remove subscriber from the list
	 */
	public static boolean removeSubscriber(){
		return false;
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
