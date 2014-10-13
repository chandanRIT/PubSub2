package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import other.Event;
import other.Utils;

/**
 * This thread is responsible to maintain the listener socket which the Eventmanager uses to send notifications. 
 * @author kchandan
 *
 */
class RecNotifThread extends Thread{ //Receive Notification Thread

	private boolean stopRun = false;
	//Queue<Event> eventsQ;
	private ServerSocket serverSocket;
	
	public RecNotifThread(Queue<Event> eventsQ){
		//serverSocket = socket;
		//this.eventsQ = eventsQ;
	}
	
	/**
	 * this method stops this thread gracefully.
	 */
	public void stopRecNotif(){
		stopRun = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("IOException in RecNotifThread:stopRecNotif()");
			System.out.println(e);
			//e.printStackTrace();
		}
	}
	
	/**
	 * Thread's run method
	 */
	public void run(){
		try {
			this.serverSocket = new ServerSocket(Utils.DEF_CLIENT_NOTIF_PORT);
			while(true){ //may be use !stopRun here
				Socket notifSocket = serverSocket.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(notifSocket.getInputStream()));

				String line = br.readLine();
				//this.eventsQ.add(Utils.gson.fromJson(line, Event.class));
				System.out.println("\n" + "***Notification --> " + line);
				notifSocket.close();
			}
		} catch (IOException e) {
			if(stopRun) return; //cuz if we intended to stop the server
			System.out.println("IOException in RecNotifThread:run()");
			System.out.println(e);
			//e.printStackTrace();
		}
	}
}