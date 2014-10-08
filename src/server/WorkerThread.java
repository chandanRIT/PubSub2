package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
				
				//while(true){
					String cmd = bReader.readLine();
					String status = Utils.STATUS_OKAY;
					
					switch (cmd){
						case Utils.CMD_TOPIC: 
							if (!EventManager.addTopic(Utils.gson.fromJson(bReader.readLine(), Topic.class)) )
								status = Utils.STATUS_NOKAY;
							break;
							
						case Utils.CMD_EVENT:
							if (!EventManager.addEvent(Utils.gson.fromJson(bReader.readLine(), Event.class)))
								status = Utils.STATUS_NOKAY;
								break;
								
						case Utils.CMD_SUB:
							String topicName = bReader.readLine();
							if (!EventManager.addSubscriber(topicName, agentID, clientSocket.getInetAddress()))
								status = Utils.STATUS_NOKAY;
								break;
						
						case Utils.CMD_UNSUB:
							break;
								
						case "EXIT":
							//dont change the status
							break;
						
						default:
							System.out.println("Invalid cmd string from client");
					}
									
					pWriter.println(status);
					pWriter.flush();
					
					//if(cmd.equals("EXIT")) break;
				//}
				clientSocket.close(); //this closes any associated readers, streams and thus the entire socket
				//System.out.println("Agent " + agentID + " went offline");
			
            } catch (IOException e) {
				System.out.println("IOException in WorkerThread:run()");
				System.out.println(e);
				//e.printStackTrace();
			}
            /*
            long time = System.currentTimeMillis();
            //output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " +
				//this.serverText + " - " +
				//time +
				//"").getBytes());
            output.write("Hi from Server\n".getBytes());
            Thread.sleep(2000);
            output.write("Bye from Server\n".getBytes());
            output.close();
            input.close();
            System.out.println("Request processed: " + time);
             */
    }
    
    //assume the client checks for vaidity or args and strings. So the string which reaches here is good to parse
    public String handlePub(BufferedReader bReader) throws IOException{
    	/*String line = bReader.readLine();
    	String[] strArr = line.split(" ");
    	if(strArr.length == 2){ // advertise topic
    		//outStream.write(gson.toJson(new Topic(Integer.parseInt(strArr[0]), strArr[1])).getBytes());
    		EventManager.addTopic(gson.fromJson(line, Topic.class));
    	} else if (strArr.length == 3){ //publish an event on the topic 
    		outStream.write(gson.toJson(new Event(Integer.parseInt(strArr[0]), strArr[1], strArr[2])).getBytes());
    	} else {
    		throw new RuntimeException("Invalid number of String splits");
    	}*/
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
    }
}