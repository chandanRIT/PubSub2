package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import other.Event;
import other.Utils;

public class Client {

	Socket socket;
	PrintWriter printWriter;
	BufferedReader bReader;

	int clientId;
	String destIPAddress;
	//Queue<Event> eventsQ;

	public Client(int clientId, String destIPAddress) {
		this.clientId = clientId;
		this.destIPAddress = destIPAddress;
	}

	public int getId(){
		return clientId;
	}

	protected void openSocket(){
		try {
			socket = new Socket(destIPAddress, Utils.DEF_PORT);
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
		return response.equals(Utils.STATUS_OKAY); 
	}

	/*public String readTillEnd(){
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
	}*/

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

}

class RecNotifThread extends Thread{ //Receive Notification Thread

	private boolean stopRun = false;
	//Queue<Event> eventsQ;
	private ServerSocket serverSocket;
	
	public RecNotifThread(Queue<Event> eventsQ){
		//serverSocket = socket;
		//this.eventsQ = eventsQ;
	}
	
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
	
	public void run(){
		try {
			this.serverSocket = new ServerSocket(Utils.CLIENT_NOTIF_PORT);
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
