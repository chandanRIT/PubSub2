package client;


import java.util.Scanner;

public class PubSubCmdGui {

	/**
	 * @param args
	 */
	static String MENU = //"Agent ID: %s" +
						 "\n***** Menu ***** \n" +	
						 "1. Advertise: <topic-name> \n" +
						 "2. Publish: <topic-name> <event-title> <event-content> \n" +
						 "3. Subscribe: <topic-name> \n" +
						 "4. Unsubscribe: <topic-name> \n" +
						 "5. Get Subscribed Topics \n" +
						 "6. List Events \n" +
						 "7. Exit \n" +
						 "Please Choose: ";
	
	final static String USAGE_ERROR = "Usage Error! \nUsage: java PubSubCmdGui <Numeric pubId> <IPAddress>";
	
	public PubSubAgent psAgent;
	
	public PubSubCmdGui(int agentId, String ipAddress){
		psAgent = new PubSubAgent(agentId, ipAddress);
	}
	
	public static void main(String[] args) {
		if(args.length != 2 || !checkIfStrIsInt(args[0])) {
			System.out.println(USAGE_ERROR);
			return;
		};
		
		PubSubCmdGui cmdGui = new PubSubCmdGui(Integer.parseInt(args[0]), args[1]);
		System.out.println("Agent ID: " + cmdGui.psAgent.getId());
		
		Scanner sc = new Scanner(System.in);
		while(true){
			System.out.println(MENU);
			String choice = sc.nextLine();
			switch(choice){
				case "1": 
					cmdGui.handleAdvertise(sc);
					break;
					
				case "2":
					cmdGui.handlePublish(sc);
					break;
					
				case "3":
					cmdGui.handleSubscribe(sc);
					break;
					
				case "4":
					cmdGui.handleUnSubscribe(sc);
					break;
					
				case "5":
					break;
					
				case "6":
					break;
					
				case "7":
					/*
					if(cmdGui.psAgent.closeConn())
						System.out.println("Exiting ...");
					else 
						System.out.println("Exiting Anyway ..");
					*/
					System.out.println("Exiting Agent ...");
					cmdGui.psAgent = null;
					cmdGui = null;
					return; //exit main/
				
				default:
					System.out.println("\nInvalid Choice entered");
			}
		} 
	}
	
	void handleSubscribe(Scanner sc){
		System.out.println("\nEnter Topic names to subscribe to below: ");
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
	
	void handleUnSubscribe(Scanner sc){
		System.out.println("\nEnter Topic names to unsubscribe from below: ");
		String topicName;
		while(true){
			System.out.print("Enter TopicName: ");
			if ((topicName = sc.nextLine()).equals("")) return; // return to main menu on empty string
			if(this.psAgent.unsubscribe(topicName))
				System.out.println("Unsubscribed from Topic: '" + topicName +  "'.");
			else 
				System.out.println("No such Topic exists");
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
			System.out.println("Enter topic, title, content: ");
			if ((line = sc.nextLine()).equals("")) return;
			String[] strArr = line.split(",");
			if(strArr.length != 3) {
				System.out.println("Expecting 3 strings. Invalid number of strings provided."); 
				continue;
			}
			if(this.psAgent.publish(strArr[0], strArr[1], strArr[2]))
				System.out.println("Event '" + strArr[1] +  "' published");
			else
				System.out.println("Event not published. Event's Topic doesnot exist");
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
