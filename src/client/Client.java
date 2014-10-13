package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

import other.Event;
import other.Utils;

/**
 * This class is the base class for PubSubAgent class and takes care of the low level networking.
 * Every Client has an Id and destination IPAddress to connect to.
 * @author kchandan
 */
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
			socket = new Socket();
			socket.connect(new InetSocketAddress(destIPAddress,Utils.DEF_EM_PORT), 2000);
			printWriter = new PrintWriter(socket.getOutputStream(),true);
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printWriter.println(clientId+"");
		} catch (IOException e) {
			throw new RuntimeException("May be the Server's down");
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
