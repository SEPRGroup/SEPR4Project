package svc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/** class implementing NetworkIO that guarantees objects are transmitted*/
public class tcpConnection implements NetworkIO {

	public static final int TCP_PORT = 10007;
	private int status = NetworkIO.STATUS_IDLE;
	private Exception lastError = null;

	private final ArrayBlockingQueue<Object>
	out = new ArrayBlockingQueue<Object>(20),
	in = new ArrayBlockingQueue<Object>(20);

	private ObjectOutputStream writeStream;
	private ObjectInputStream readStream;
	
	private boolean host;
	
	
	private ServerSocket serverSocket;
	private Socket socket;
	
	
	
	public tcpConnection(boolean host) {
		this.host = host;
	}

	@Override
	public void connect(String destination, int port) {
		if (status == STATUS_IDLE || status == STATUS_FAILED){
			status = STATUS_TRAINING;
			new Thread(new Initialize(port,destination,host)).start();
		
		}
	}

	class Initialize implements Runnable{
		int port;
		boolean host;
		String destination;

		private Initialize(int port,String destination,boolean host){
			this.port = port;
			this.host = host;
			this.destination = destination;
		}

		@Override
		public void run() {
			try {
				if(host){
					do{
						serverSocket = new ServerSocket(port);
						System.out.println("Waiting for Client");
						//Wait for the connection to be made, blocks until the connection has been made
						socket = serverSocket.accept();
						System.out.println("Expected: " +destination +"\t Received: " +socket.getInetAddress().getHostName());
						if (!socket.getInetAddress().getHostName().equals(destination)){
							//socket is not the client we wanted to accept
							System.out.println("Expected: " +destination +"\t Rejected: " +socket.getInetAddress().getHostName());
							socket.close();
							socket = null;
						}
					} while (socket == null);
					//done listening for clients
					serverSocket.close();
				} else{
					socket = new Socket(destination,port);
				}
				System.out.println("accepted");
				writeStream = new ObjectOutputStream(socket.getOutputStream());
				System.out.println("output created");
				readStream = new ObjectInputStream(socket.getInputStream());
				System.out.println("input created");

				

				status = STATUS_ALIVE;
			} catch (IOException e) {
				lastError = e;
				System.err.println("Could not listen on port: "+ port);
				status = STATUS_FAILED;
			}
			new Thread(new Sender()).start();
			new Thread(new Receiver()).start();
		}
	}

	class Sender implements Runnable{
		
		@Override
		public void run() {
			try {
				while (true){
					writeStream.writeObject(out.take());
					writeStream.flush();
				}
			} catch (Exception e) {
				lastError = e;
				e.printStackTrace();
				status = STATUS_FAILED;
			}
			finally {
				try {
					if (serverSocket != null) serverSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
				try {
					if (socket != null) socket.close();
				}
				catch (IOException e){ e.printStackTrace(); }
			}
		}

	}

	class Receiver implements Runnable{
		
		@Override
		public void run() {
			try {
				while (true){
					in.add(readStream.readObject());
				}
			} catch (Exception e) {
				lastError = e;
				e.printStackTrace();
				status = STATUS_FAILED;
			}
			finally {
				try {
					if (serverSocket != null) serverSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
				try {
					if (socket != null) socket.close();
				}
				catch (IOException e){ e.printStackTrace(); }
			}
		}

	}

	@Override
	public void sendObject(Object obj) {
		
		out.add(obj);
	}

	@Override
	public Object pollObjects() {

		return in.poll();
	}

	@Override
	public void close() {
		
		if(status == STATUS_ALIVE){
			try {
				try {
					if (serverSocket != null) socket.close();
				}  catch (IOException e) { e.printStackTrace(); }
				try {
					if (readStream != null) readStream.close();
				} catch (IOException e) { e.printStackTrace(); }
				
				try {
					if (writeStream != null) writeStream.close();
				} catch (IOException e) { e.printStackTrace(); }
				try {
					if (socket != null) socket.close();
				} catch (IOException e){ e.printStackTrace(); }
			} 
			finally{
				status = STATUS_FAILED;
			}
		}
	}

	@Override
	public Boolean isAlive() {
		
		return status == STATUS_ALIVE;
	}

	@Override
	public Exception getLastError() {
		
		return lastError;
	}

	@Override
	public int getStatus() {
		
		return status;
	}

}