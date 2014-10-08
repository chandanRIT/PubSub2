package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

import other.Utils;

public class MultiThreadedServer implements Runnable{
	
    int port;
    private ServerSocket serverSocket = null;
   
    private boolean isStopped = false;
    private Thread runningThread= null;

    public MultiThreadedServer(){
        this(Utils.DEF_PORT);
    }
    
    public MultiThreadedServer(int port){
        this.port = port;
    }
    
    public void run(){
        synchronized(this){
            runningThread = Thread.currentThread();
        }
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
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            //work on client sockets
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
            System.out.println("MTS Listening at port: " + port);
        } catch (IOException e) {
        	System.out.println("***IOException in MultiThreadedServer:openServerSocket");
            throw new RuntimeException("Cannot open port "+ port, e);
        }
    }
}