package server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import other.Utils;

/**
 * This class acts as the server. It listens continuously for clients and delegates client request processing
 * to the WorkerThread object. 
 * @author kchandan
 *
 */
public class MultiThreadedServer implements Runnable{
	
    private int port; //port at which the server listens for clients
    private ServerSocket serverSocket = null;
    private boolean isStopped = false; //flag which is used in the server loop. Set this to false to stop the server.
    //private Thread runningThread= null;

    //Constructors
    public MultiThreadedServer(){
        this(Utils.DEF_EM_PORT);
    }
    
    public MultiThreadedServer(int port){
        this.port = port;
    }
    
    /**
     * The run method for the thread which opens a ServerSocket and waits for clients.
     * It creates a WorkerThread class and delegates handling of clients requests to it. 
     * This avoids the bottle-neck issue that may arise at the server.   
     */
    public void run(){
        openServerSocket();
        while(!isStopped()){
            Socket clientSocket = null;
            try {
            	//accept connections from clients
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            //Process new clients in a different thread 
            new Thread(new WorkerThread(clientSocket)).start();
        }
        System.out.println("Server Stopped.") ;
    }
    
    /**
     * @return The state of the server if it's running or if it's stopped.
     */
    private synchronized boolean isStopped() {
        return isStopped;
    }

    /*
     * This method is used to stop the server. 
     * It closes the socket and thus unblocks server.accept() to stop the thread. 
     */
    public synchronized void stopServer(){
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
        	System.out.println("***IOException in MultiThreadedServer:stop()");
            throw new RuntimeException("Error closing server", e);
        }
    }
    
    /**
     * This method creates a Server Socket at the specified port  
     */
    private void openServerSocket() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Event Manager is at IPAddress: " + InetAddress.getLocalHost().getHostAddress() + " and Port: " + port);
        } catch (IOException e) {
        	System.out.println("***IOException in MultiThreadedServer:openServerSocket");
            throw new RuntimeException("Cannot open port "+ port, e);
        }
    }
}