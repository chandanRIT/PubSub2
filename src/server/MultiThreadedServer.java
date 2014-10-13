package server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import other.Utils;

public class MultiThreadedServer implements Runnable{
	
    private int port;
    private ServerSocket serverSocket = null;
    private boolean isStopped = false; //flag which is used in the server loop. Set this to false to stop the server.
    //private Thread runningThread= null;

    public MultiThreadedServer(){
        this(Utils.DEF_PORT);
    }
    
    public MultiThreadedServer(int port){
        this.port = port;
    }
    
    public void run(){
        /*synchronized(this){
            runningThread = Thread.currentThread();
        }*/
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
            new Thread(new WorkerThread(clientSocket, "Multithreaded Server")).start();
        }
        System.out.println("Server Stopped.") ;
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stopServer(){
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
        	System.out.println("***IOException in MultiThreadedServer:stop()");
            throw new RuntimeException("Error closing server", e);
        }
    }

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