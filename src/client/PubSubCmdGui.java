package client;


import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import other.Event;
import other.Utils;

/**
 * This class handles the UI of the agents. It takes in input from the users and 
 * @author kchandan
 *
 */
public class PubSubCmdGui {

	/**
	 * @param args
	 */
	final static String MENU = //"Agent ID: %s" +
						 "\n***** Menu ***** \n" +	
						 "0. Advertise: <topic-name> \n" +
						 "1. Subscribe to Topic: <topic-name> \n" +
						 "2. Unsubscribe from Topic: <topic-name> \n" +
						 "3. Subscribe to Keyword: <keyword> \n" +
						 "4. Unsubscribe from Keyword: <keyword> \n" +
						 "5. Publish: <topic-name>,<event-title>,<event-content>, [space-seperated keywords (optional)] \n" +
						 "6. Display Advertized Topics \n" +
						 "7. Display Subscribed Topics \n" +
						 "8. Display Subscribed Keywords \n" +
						 "9. Exit \n" +
						 "Please Choose: ";
	
	final static String USAGE_ERROR = "Usage Error! \nUsage: java PubSubCmdGui <Numeric pubId> <IPAddress>";
	
	public PubSubAgent psAgent; //reference to PubSubAgent object which communicates with the Event Manager
	
	public PubSubCmdGui(int agentId, String ipAddress){
		psAgent = new PubSubAgent(agentId, ipAddress);
	}
	
	/**
	 * Handles the UI, takes input from the user and performs corresponding actions
	 * and displays results or provides feedback.
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		if(args.length != 2 || !checkIfStrIsInt(args[0])) {
			System.out.println(USAGE_ERROR);
			return;
		};
		
		PubSubCmdGui cmdGui = new PubSubCmdGui(Integer.parseInt(args[0]), args[1]);
		System.out.println("\nAgent ID: " + cmdGui.psAgent.getId());
		
		Queue<Event>missedEvents = cmdGui.psAgent.pullNotifications();
		if (!missedEvents.isEmpty()) {
			System.out.println("\nMissed notifications below:");
			int i = 0;
			for(Event event : missedEvents){
				System.out.println(i++ +  ". " + Utils.gson.toJson(event));
			}
			missedEvents = null;
		}
		
		Scanner sc = new Scanner(System.in);
		while(true){
			System.out.print(MENU);
			switch(sc.nextLine()){
				case "0": 
					cmdGui.handleAdvertise(sc);
					break;
					
				case "1":
					cmdGui.handleSubscribe(sc);
					break;
					
				case "2":
					cmdGui.handleUnSubscribe(sc);
					break;
					
				case "3":
					cmdGui.handleSubscribeKW(sc);
					break;
					
				case "4":
					cmdGui.handleUnSubscribeKW(sc);
					break;
					
				case "5":
					cmdGui.handlePublish(sc);
					break;
				
				case "6":
					cmdGui.handleGetAdvertizedTopics();
					break;
				
				case "7":
					cmdGui.handleGetSubscribedTopics();
					break;
					
				case "8":
					cmdGui.handleGetSubscribedKWs();
					break;	
				
				case "9":
					System.out.println("\nExiting Agent ...");
					cmdGui.psAgent.stopNotifThread();
					return; //exit main/
				
				default:
					System.out.println("\nInvalid Choice entered");
			}
			//Thread.sleep(1300);
		} 
	}
	
	//all the handles for each of the switch-case in the above method is below
	
	void handleSubscribe(Scanner sc){
		System.out.println("\nEnter TopicName to subscribe to below: ");
		String topicName;
		while(true){
			System.out.print("Enter TopicName: ");
			if ((topicName = sc.nextLine()).equals("")) return; // return to main menu on empty string
			if(this.psAgent.subscribe(topicName))
				System.out.println("Subscribed to Topic: '" + topicName +  "'.");
			else 
				System.out.println("No such Topic exists");
		}
	}
	
	void handleSubscribeKW(Scanner sc){
		System.out.println("\nEnter Keyword to subscribe to below: ");
		String kw;
		while(true){
			System.out.print("Enter Keyword: ");
			if ((kw = sc.nextLine()).equals("")) return; // return to main menu on empty string
			if(this.psAgent.subscribeKW(kw))
				System.out.println("Subscribed to Keyword: '" + kw +  "'.");
			else 
				System.out.println("Failed to subscribe.");
		}
	}
	
	void handleUnSubscribe(Scanner sc){
		System.out.println("\nEnter TopicName to unsubscribe from below: ");
		String topicName;
		while(true){
			System.out.print("Enter TopicName: ");
			if ((topicName = sc.nextLine()).equals("")) return; // return to main menu on empty string
			if(this.psAgent.unsubscribe(topicName))
				System.out.println("Unsubscribed from Topic: '" + topicName +  "'.");
			else 
				System.out.println("Subscriber never subscribed to such topic.");
		}
	}
	
	void handleUnSubscribeKW(Scanner sc){
		System.out.println("\nEnter keyword to unsubscribe from below: ");
		String kw;
		while(true){
			System.out.print("Enter Keyword: ");
			if ((kw = sc.nextLine()).equals("")) return; // return to main menu on empty string
			if(this.psAgent.unSubscribeKW(kw))
				System.out.println("Unsubscribed from Keyword: '" + kw +  "'.");
			else 
				System.out.println("Subscriber never subscribed to such keyword.");
		}
	}
	
	public void handleGetAdvertizedTopics(){
		System.out.println("\nAdvertized Topics are as below:");
		List<String> topics = this.psAgent.getAdvertisements();
		if(topics == null || topics.isEmpty()){ 
			System.out.println("No Topics created yet!");
			return;
		}
		for(String t : topics){
			System.out.println(t);
		}
	}
	
	public void handleGetSubscribedKWs(){
		System.out.println("\nSubscribed Keywords for Agent(" + this.psAgent.getId() + ") are as below:");
		List<String> keywords = this.psAgent.getSubscribedKWs();
		if(keywords == null || keywords.isEmpty()){ 
			System.out.println("No subscriptions exist!");
			return;
		}
		for(String t : keywords){
			System.out.println(t);
		}
	}
	
	void handleGetSubscribedTopics(){
		System.out.println("\nSubscribed Topics for Agent(" + this.psAgent.getId() + ") are as below:");
		List<String> topics = this.psAgent.getSubscriptions();
		if(topics == null || topics.isEmpty()){ 
			System.out.println("No Subscriptions exist!");
			return;
		}
		for(String t : topics){
			System.out.println(t);
		}
	}
	
	void handleAdvertise(Scanner sc){
		System.out.println("\nAdvertise Topics below: ");
		String topicName;
		while(true){
			System.out.print("Enter TopicName: ");
			if ((topicName = sc.nextLine()).equals("")) return;
			if(this.psAgent.advertise(topicName))
				System.out.println("Topic '" + topicName +  "' created");
			else 
				System.out.println("Topic already exists");
		}
	}
	
	void handlePublish(Scanner sc){
		System.out.println("\nPublish Events on Topics below: ");
		String line;
		while(true){
			System.out.print("Topic,Title,Content,[keywords space-seperated]:");
			if ((line = sc.nextLine()).equals("")) return;
			String[] strArr = line.split(",");
			if(strArr.length < 3 || strArr.length > 4 ) {
				System.out.println("Expecting 3 or 4 comma-seperated strings."); 
				continue;
			}
			boolean status = strArr.length == 3 ? this.psAgent.publish(strArr[0], strArr[1], strArr[2], null) : 
				this.psAgent.publish(strArr[0], strArr[1], strArr[2], strArr[3].trim().split("[ ]+")); 
			if(status)
				System.out.println("Event '" + strArr[1] +  "' published");
			else
				System.out.println("Event not published. Specified Topic doesnot exist");
		}
	}
	
	static boolean checkIfStrIsInt(String str){
		try{
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException nfe){
			return false;
		}	
	}

}
