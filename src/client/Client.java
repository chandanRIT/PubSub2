package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import other.Utils;
import other.Event;

public class Client {
	
	private Socket socket;
	private ServerSocket notificationsSocket;
	
	private PrintWriter printWriter;
	private BufferedReader bReader;
	private StringBuilder sBuff;
	
	int clientId;
	
	public Client(int clientId) {
		Queue<Event> eventsQ = new LinkedList<>();
		
		try {
			this.clientId = clientId;
			notificationsSocket = new ServerSocket(Utils.CLIENT_NOTIF_PORT);
			RecNotifThread receiveNotThread = new RecNotifThread(notificationsSocket, eventsQ);
			receiveNotThread.start();
			
			/*new Thread(
		            new Runnable(
		            		notificationsSocket, "Multithreaded Server")
		            ){}.start();*/
			
		} catch (IOException e) {
			System.out.println("Exception in Client:Client()");
			System.out.println(e);
			//e.printStackTrace();
		}
		
		sBuff = new StringBuilder();
	}
	
	private void openSocket(){
		try {
			socket = new Socket(Utils.DEF_HNAME, Utils.DEF_PORT);
			printWriter = new PrintWriter(socket.getOutputStream(),true);
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printWriter.println(clientId+"");
		} catch (IOException e) {
			System.out.println("Exception in Client:openSocket()");
			System.out.println(e);
			//e.printStackTrace();
		}
	}
	
	/**
	 * opens a connection, sends the command and its associated data (payload)
	 * and reads a line of response from the server and closes the socket.
	 * @param cmd
	 * @param data
	 * @return
	 */
	protected boolean sendReqAndRecResp(String cmd, String data){
		openSocket();
		printWriter.println(cmd);
		if(data!=null)
			printWriter.println(data);
		printWriter.flush();
		String response = Utils.STATUS_NOKAY;
		try {
			response = bReader.readLine();
			socket.close();
		} catch (IOException e) {
			System.out.println("IOException in Client:sendAndRecResp()");
			System.out.println(e);
			//e.printStackTrace();
		}
		if(response.equals(Utils.STATUS_OKAY)) 
			return true;
		return false;
	}
	
	public String readTillEnd(){
		sBuff.setLength(0);
		String inputLine = null;
		try {
			while((inputLine = bReader.readLine()) != null){
				//System.out.println(inputLine);
				sBuff.append(inputLine+"\n");
			}
		} catch (IOException e) {
			System.out.println("IOException in Client:read()");
			System.out.println(e);
			//e.printStackTrace();
		}
			
		return sBuff.toString();
	}
	
	/*public boolean closeSocket(){
		try {
			socket.close();
			return true;
		} catch (IOException e) {
			System.out.println("IOException in Client:closeConn()");
			System.out.println(e);
			return false;
			//e.printStackTrace();
		}
	}
	
	public boolean isSocketOpen(){
		return !socket.isClosed();
	}*/
	
	public static void main(String[] args)
	{
		try
		{	Client cl = new Client(2);
			cl.socket = new Socket("localhost",2567);
			/*
				printWriter = new PrintWriter(socket.getOutputStream(),true);
	            printWriter.println("Hello Socket");
	            printWriter.println("EYYYYYAAAAAAAA!!!!");
			 */

			String inputLine;
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(cl.socket.getInputStream()));
			// Get the server message
			while((inputLine = bufferedReader.readLine()) != null)
				System.out.println(inputLine);
		}
		catch(Exception e)
		{
			System.out.println(e);
			//e.printStackTrace();
		}
	}
}

class RecNotifThread extends Thread{ //Receive Notification Thread
	ServerSocket serverSocket;
	Queue<Event> eventsQ;
	public RecNotifThread(ServerSocket socket, Queue<Event> eventsQ){
		serverSocket = socket;
		this.eventsQ = eventsQ;
	}
	
	public void run(){
		while(true){
			try {
				Socket notifSocket = serverSocket.accept();
				InputStream in = notifSocket.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				String line = br.readLine();
				this.eventsQ.add(Utils.gson.fromJson(line, Event.class));
				System.out.println("\n" + "***Notification --> " + line);
			
			} catch (IOException e) {
				System.out.println("IOException in RecNotifThread:run()");
				System.out.println(e);
				//e.printStackTrace();
			}
		}
	}
}
