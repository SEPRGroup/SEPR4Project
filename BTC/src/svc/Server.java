package svc;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.*;

import cls.Waypoint;



public class Server extends Thread {
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	
	//Client and Thread lists
	public static ArrayList<Client> clientList = new ArrayList<Client>();
	ArrayList<Thread> threads = new ArrayList<Thread>();
	
	private int clientCount; //How many clients have joined
	private ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(20);
	
	
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private int port = 10007;
	
	
	public Server() {
		
	}
	
	public void setObjectOutStream(Object obj) throws IOException {
		out.writeObject(obj);
		out.flush();
		
	}
	
	public Object getObjectInStream() throws ClassNotFoundException, IOException {
		return in.readObject();
	}
	
	private void listenSocket() {
		
		// Create server
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 10007.");
		}
		
		
		//Wait for clients to connect
		while(true){
			
			// Temp client object
			Client c;
			try {
				System.out.println("Waiting for Client");
				clientSocket = serverSocket.accept();
				clientCount++;
			} catch (IOException e) {
				System.out.println("Accept failed.");
			}	
			if(clientCount >= 1){
				break;
			}
		}
				
		
		try {
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			in = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private void communicate() {
		Waypoint point = new Waypoint(600, 800, false);
		Waypoint point1 = new Waypoint(600, 800, false);
		Waypoint point2 = new Waypoint(600, 800, false);
		queue.add(point);
		queue.add(point1);
		queue.add(point2);
		while(true){
			if(!queue.isEmpty()){
				sendObject(queue);
			}
			
				
		}
	}
	public void endServer() throws IOException { 
		out.close(); 
	    in.close(); 
	    clientSocket.close(); 
	    serverSocket.close(); 
	}
	
	public synchronized void sendObject(Object obj){
		//int num = 33;
		Object test;
		
	        	try {
					setObjectOutStream(obj);
					//wait(1000);
					test = getObjectInStream();
					if(test instanceof Integer){
						int message = (int) test;
						if(message == 1){
							System.out.println("client received");
							out.reset();
							if(obj instanceof ArrayBlockingQueue){
								((ArrayBlockingQueue<Object>) obj).remove();
							}
							
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}// catch (InterruptedException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//} 
	        	catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        	
	        }
	
	public static void main(String[] args) throws IOException  {
	{ 
		//Create Server object
		Server server = new Server();
		
		
	
		//server.sendObject(point);
		
		server.listenSocket();
		server.communicate();
	   
	   } 

	}

	

}
