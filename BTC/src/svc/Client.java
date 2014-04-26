package svc;

import java.io.*;import java.net.*;

import points.Point3d;


public class Client {
	
	private Socket socket = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;

	public Client() throws IOException {
		 
		try {
			// echoSocket = new Socket("taranis", 7);
	        socket = new Socket("127.0.0.1", 10007);

	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host: taranis.");
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for "
	                               + "the connection to: taranis.");
	            System.exit(1);
	        }    
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
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
	
	public static void main(String[] args) throws IOException {

		//Point3d pt1 = new Point3d (7, 8, 10);
        //Point3d pt2 = null;

        //System.out.println ("Sending point: " + pt1 + " to Server");
        
        //System.out.println ("Send point, waiting for return value");

        //try {
        //     pt2 = (Point3d) in.readObject();
        //    }
        // catch (Exception ex)
        //    {
        //    System.out.println (ex.getMessage());
        //    }

        //System.out.println("Got point: " + pt2 + " from Server");
	
        //System.console().readLine();

    }
	
}
