package svc;

import java.io.*;import java.net.*;

import points.Point3d;


public class Client {

	public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            // echoSocket = new Socket("taranis", 7);
            echoSocket = new Socket("127.0.0.1", 10007);
            
            out = new ObjectOutputStream(echoSocket.getOutputStream());
            in = new ObjectInputStream(echoSocket.getInputStream());

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: taranis.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to: taranis.");
            System.exit(1);
        }



	Point3d pt1 = new Point3d (7, 8, 10);
        Point3d pt2 = null;

        System.out.println ("Sending point: " + pt1 + " to Server");
	out.writeObject(pt1);

    
    
    
	
        out.flush();
        System.out.println ("Send point, waiting for return value");

        try {
             pt2 = (Point3d) in.readObject();
            }
        catch (Exception ex)
            {
             System.out.println (ex.getMessage());
            }

	System.out.println("Got point: " + pt2 + " from Server");
	
	System.console().readLine();
	
	out.close();
	in.close();
	echoSocket.close();
    }
	
}
