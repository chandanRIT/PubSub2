package server;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import other.Event;
import other.Topic;
import other.Utils;
import client.PubSubAgent;

public class EventManager{
	static Map<String, Topic> topics = new HashMap<>(); //map between topicNames and Topic objects
	static Map<String, List<Integer>> topicsSubsMap = new HashMap<>(); //map between topicName and Subscriber-Ids
	//static Map<String, List<Event>> topicsEventsMap = new HashMap<>();
	static BlockingQueue<Event> blockingEventsQ = new LinkedBlockingQueue<>(); // the events queue to which events are added and dispatched from continuously
	static Map<Integer, InetAddress> idIpMap = new HashMap<>(); //map between clientIds and their ipaddresses.IP adresss they used while subscribing to the latest topic 
	static Map<Integer, Queue<Event>> pendingNotifMap = new HashMap<>(); //map between clientIds and their associated pending Events
	//static Map<String, Map<String, Integer>>
	static Map<String, List<Integer>> kwSubsMap = new HashMap<>(); //map between a keyword and Subscriber-Ids
	
	//static boolean stopNotifThread = false;
	static MultiThreadedServer mts;
	/*
	 * Start the repo service
	 */
	private static void startService() {
		mts = new MultiThreadedServer();
		new Thread(mts).start();
		new Thread(){ //start the notification thread right away
			public void run(){
				while(true){ // may be use !stopNotifThread too
					try {
						Event event = blockingEventsQ.take();
						if(event.getPubId() == -747) return; // Poison Pill to stop the server
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
	
	private static void stopEventManager(){
		//stopNotifThread = true; //this stops the anonymous thread used for notifications 
		blockingEventsQ.add(new Event(-747, "", "")); //Poison Pill Event to signal to come out of blocking take
		mts.stopServer();
	}

	/*
	 * notify all subscribers of new event 
	 */
	private static void notifySubscribers(Event event) {
		Set<Integer> subs = new HashSet<>(); //use Set, since it does not add duplicates
		
		synchronized (topicsSubsMap) {
			List<Integer> tsubs = topicsSubsMap.get(event.getTopicName());
			if(tsubs!=null) 
				subs.addAll(tsubs); //get the list of associated subs
		}
		
		String[] kwArr = event.getKeywords();
		if(kwArr != null) { 
			synchronized (kwSubsMap) {
				for(String kw : kwArr){
					List<Integer> kwsubs = kwSubsMap.get(kw);
					if(kwsubs != null)
						subs.addAll(kwsubs);
				}
			}
		}
		System.out.println("Subscribers list for Event: "+ event.getTitle()+ " are "+ subs);
		
		for(Integer i : subs){
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
		if(! topics.containsKey(event.getTopicName())) return false; //Invalid topic 
		blockingEventsQ.add(event);
		Utils.debug(Utils.gson.toJson(event));
		return true;
	}
	
	/*
	 * add subscriber to the internal list
	 */
	public static boolean addSubscriber(String topicName, int subId, InetAddress ipAddress){
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
	
	public static boolean addSubscriberKW(String kw, int subId, InetAddress ipAddress){
		synchronized (kwSubsMap) {
			List<Integer> subs;
			if(( subs = kwSubsMap.get(kw) ) == null){
				kwSubsMap.put(kw, subs = new ArrayList<>());
			}
			subs.add(subId);
		}
			
		idIpMap.put(subId, ipAddress);
		Utils.debug("Subscriber:" + subId + " subscribed to Keyword: "+ kw);
		return true;
	}
	
	/*
	 * remove subscriber from the list
	 */
	public static boolean removeSubscriber(int subId, String topicName){
		if(!topics.containsKey(topicName)) return false; //no such topic advertised
		List<Integer> subscribersList;
		synchronized (topicsSubsMap) { //put a lock on topicsSubsMap because we dont want concurrent modifcations/iterations to it
			subscribersList = topicsSubsMap.get(topicName);
			if(subscribersList == null || subscribersList.isEmpty()) return false; //no subscribers exist yet
			Utils.debug("Subscriber:" + subId + " unsubscribed from topic: "+ topicName);
			return subscribersList.remove(new Integer(subId)); //remove this id from the list
		}
	}
	
	public static boolean removeSubscriberKW(int subId, String kw){
		List<Integer> subscribersList;
		synchronized (kwSubsMap) { //put a lock on topicsSubsMap because we dont want concurrent modifcations/iterations to it
			subscribersList = kwSubsMap.get(kw);
			if(subscribersList == null || subscribersList.isEmpty()) return false; //no subscribers exist yet
			Utils.debug("Subscriber:" + subId + " unsubscribed from Keyword: "+ kw);
			return subscribersList.remove(new Integer(subId)); //remove this ID Integer object from the list
		}
	}
	
	public static boolean unSubscribeFromAll(int subId){
		return false;
	}
	
	/**
	 * goes through all the topics and checks if the subId is in their subscription list 
	 * @param subId
	 * @return A list of topics to which subId is subscribed to
	 */
	public static List<String> getSubscriptions(int subId){ // this method returns null if no subscriptions found 
		List<String> topics = new ArrayList<>();
		synchronized(topicsSubsMap){
			for(Entry<String, List<Integer>> entry : topicsSubsMap.entrySet()){
				if(entry.getValue().contains(subId))
					topics.add(entry.getKey());
			}
		}
		return topics.isEmpty() ? null : topics;
	}
	
	/**
	 * @param subId
	 * @return List<String> : A list of keywords which subId is subscribed to
	 */
	public static List<String> getSubscribedKWs(int subId){
		List<String> keywords = new ArrayList<>();
		synchronized(kwSubsMap){
			for(Entry<String, List<Integer>> entry : kwSubsMap.entrySet()){
				if(entry.getValue().contains(subId))
					keywords.add(entry.getKey());
			}
		}
		return keywords.isEmpty() ? null : keywords;
	}
	
	public static List<String> getAllTopics(){ // required for PubSubAgent:getAdvertisements() 
		synchronized(topics){
			Set<String>	topicNamesSet = topics.keySet();
			return  topicNamesSet.isEmpty() ? null :  new ArrayList<String>(topics.keySet());
		}
	}
	
	public static Queue<Event> clearPendingEventsForClient(int id){
		Queue<Event> eventsQ = pendingNotifMap.get(id);
		if(eventsQ == null) return null;
		pendingNotifMap.put(id, new LinkedList<Event>());
		return eventsQ;
	}
	
	/*
	 * Returns the list of subscriber for a specified topic. Will be used in the EM GUI.
	 * 
	 */
	public static List<Integer> showSubscribers(Topic topicName){
		synchronized (topicsSubsMap) {
			List<Integer> tSubs;
			if ((tSubs = topicsSubsMap.get(topicName))!=null && !tSubs.isEmpty())
				return new ArrayList<>(tSubs);
		}
		return null;		
	}
	
	/**
	 * 
	 * @return All the Subscriber Ids registered to any Topic.
	 */
	public static Set<Integer> getAllSubscribers(){
		Set<Integer> subsSet = new HashSet<>();
		synchronized(topicsSubsMap){
			for(Entry<String, List<Integer>> entry : topicsSubsMap.entrySet()){
				List<Integer> subsList = entry.getValue();
				if(subsList!=null){
					//synchronized (subsList) { //may be sync not necessary on subsList since topicsSubsMap is locked 
						subsSet.addAll(subsList);
					//}
				}
			}
		}
		return subsSet.isEmpty() ? null : subsSet;
	}
	
	public static void main(String[] args) {
		EventManager.startService();
	}
}
