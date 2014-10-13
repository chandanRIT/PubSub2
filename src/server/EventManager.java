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
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import other.Event;
import other.Topic;
import other.Utils;

/**
 * This class provides the UI for the EventManager in the PubSub system and also stores various 
 * data structures to facilitate the PubSub system's passing of messages and its state.
 * @author kchandan
 *
 */
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
	 * This method starts the server in a separate thread and a thread which sends notifications to clients 
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
	
	/**
	 * This method is used to stop the PUB-SUB System's server side of things.
	 */
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
				Socket notifSocket = new Socket(idIpMap.get(i),Utils.DEF_CLIENT_NOTIF_PORT);
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
	 * create a new topic in the EM when advertisement of new topic is received
	 */
	public static boolean addTopic(Topic topic){
		if( topics.containsKey(topic.getName()) ) return false; //Topic already exists. 
		topics.put(topic.getName(), topic);
		//topicsSubsMap.put(topic, null);
		//topicsEventsMap.put(topic.getName(), new ArrayList<Event>());
		Utils.debug(Utils.gson.toJson(topic));
		return true;
	}
	
	/*
	 * Add event to the blockingQueue.
	 */
	public static boolean addEvent(Event event){
		if(! topics.containsKey(event.getTopicName())) return false; //Invalid topic 
		blockingEventsQ.add(event);
		Utils.debug(Utils.gson.toJson(event));
		return true;
	}
	
	/*
	 * add subscriber to the corresponding Topic's list
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
	
	/**
	 * This method adds a subscriber for the corresponding keyword
	 * @param kw
	 * @param subId
	 * @param ipAddress
	 * @return
	 */
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
	 * remove subscriber from the corresponding topic's list
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
	
	/**
	 * Removes subscriber from the corresponding keywords list
	 * @param subId
	 * @param kw
	 * @return
	 */
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
	
	/**
	 * @return All the topics created in the PubSub System 
	 */
	public static List<String> getAllTopics(){ // required for PubSubAgent:getAdvertisements() 
		synchronized(topics){
			Set<String>	topicNamesSet = topics.keySet();
			return  topicNamesSet.isEmpty() ? null :  new ArrayList<String>(topics.keySet());
		}
	}
	
	/*
	 * 
	 */
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
	public static List<Integer> getSubscribers(String topicName){
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
	
	final static String MENU = //"Agent ID: %s" +
			 "\n***** Menu ***** \n" +	
			 "0. Display Advertized Topics \n" +
			 "1. Display current Subscribers \n" +
			 "2. Display current Subscribers for topic: <TopicName> \n" +
			 "3. ShutDown EventManager \n" +
			 "Please Choose: "; 
	
	/**
	 * Starts the server and handles UI
	 * @param args
	 */
	public static void main(String[] args) {
		EventManager.startService();
		Scanner sc = new Scanner(System.in);
		while(true){
			System.out.print(MENU);
			switch(sc.nextLine()){
				case "0": 
					displayAdvertizedTopics();
					break;
					
				case "1":
					displayCurrSubscribers();
					break;
				
				case "2":	
					displayCurrSubscribers(sc);
					break;
					
				case "3":
					System.out.println("Shutting Down Event Manager ...");
					stopEventManager();
					return; //exit main/
				
				default:
					System.out.println("\nInvalid Choice entered");
			}
			//Thread.sleep(1300);
		} 
	}
	
	public static void displayAdvertizedTopics(){
		System.out.println("\nAdvertized Topics are as below:");
		List<String> topicNames = getAllTopics();
		if(topicNames == null || topicNames.isEmpty()){ 
			System.out.println("No Topics created yet!");
			return;
		}
		for(String t : topicNames){
			System.out.println(t);
		}
	}
	
	public static void displayCurrSubscribers(){
		System.out.println("\nCurrent Subscribers are as below:");
		Set<Integer> subsSet = getAllSubscribers();
		if(subsSet == null || subsSet.isEmpty()){ 
			System.out.println("No Subscriptions yet!");
			return;
		}
		for(Integer t : subsSet){
			System.out.println("Agent:" + t);
		}
	}
	
	public static void displayCurrSubscribers(Scanner sc){
		System.out.print("\nEnter topic name to display its susbcribers: ");
		List<Integer> subs = getSubscribers(sc.nextLine());
		if(subs == null || subs.isEmpty()){ 
			System.out.println("No Subscriptions yet!");
			return;
		}
		System.out.println("List of subscribers are as below:");
		for(Integer t : subs){
			System.out.println("Agent:" + t);
		}
	} 
}
