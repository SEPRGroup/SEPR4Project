package svc;

import java.io.*;import java.net.*;

import cls.Waypoint;
//import points.Point3d;


public class Client {
	
	//Server socket and host information
	private Socket socket = null;
	private String host;
	private int port;
	
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;

	public Client(String host, int port) throws IOException {
		
		this.host = host;
		this.port = port;
		
	}
	
	public void setObjectOutStream(Object obj) throws IOException {
		
		out.writeObject(obj);
        out.flush();
	}
	
	public void cancelClient() throws IOException{
		out.close();
		in.close();
		socket.close();
	}
	public void listenSocket(){
		try{
			// Create the socket 
			socket = new Socket(host,port);
			
			//Create method to communicate with server
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		}
		catch (UnknownHostException e){
			System.out.println("Unknown host: " + this.host);
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public void communicate(){
		Object obj;
		while(true){
			obj = null;
			try {
				obj = readStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Was able to read something from the server
			if(obj != null){
				
			}
		}
	}
	
	public synchronized Object readStream() throws IOException{
		Object obj = null;
			try {
				obj = in.readObject();
				System.out.println(obj);
				if(obj != null){
					System.out.println("send accept");
					setObjectOutStream(1);
					//wait(5000);
				}
			} 
			//	check = false;
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return obj;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {

		//Create client object
		Client client = new Client("127.0.0.1",10007);
       
		
		client.listenSocket();
		client.communicate();
		Waypoint point;
		Object test = client.readStream();
		if(test instanceof Waypoint){
			point = (Waypoint)test;
		}
		
      

    }
	
}
