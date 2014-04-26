package svc;
import java.net.*;
import java.io.*;



public class Server extends Thread {
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private int port = 10007;
	
	
	public Server() throws IOException {
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 10007.");
		}
		
		try {
			System.out.println("Waiting for Client");
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Accept failed.");
		}		
		
		out = new ObjectOutputStream(clientSocket.getOutputStream());
		in = new ObjectInputStream(clientSocket.getInputStream());
	}
	
	public void setObjectOutStream(Object obj) throws IOException {
		out.writeObject(obj);
		out.flush();
	}
	
	public Object getObjectInStream() throws ClassNotFoundException, IOException {
		return in.readObject();
	}
	
	public void endServer() throws IOException { 
		out.close(); 
	    in.close(); 
	    clientSocket.close(); 
	    serverSocket.close(); 
	}
	
	public static void main(String[] args) throws IOException  {
	{ 
		//Point3d pt3 = null;
	    //Point3d pt4 = null;   
	    
	    //System.out.println ("Server recieved point: " + pt3 + " from Client");

	    //pt4 = new Point3d (-24, -23, -22);
	    //System.out.println ("Server sending point: " + pt4 + " to Client");

	    //System.console().readLine();
	    
	   
	   } 

	}
}
