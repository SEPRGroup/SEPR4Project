package svc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/** class implementing NetworkIO that guarantees objects are transmitted*/
public class tcpConnection implements NetworkIO {

	private int status = NetworkIO.STATUS_IDLE;
	private Exception lastError = null;

	private final ArrayBlockingQueue<Object>
	out = new ArrayBlockingQueue<Object>(20),
	in = new ArrayBlockingQueue<Object>(20);

	private ObjectOutputStream writeStream;
	private ObjectInputStream readStream;

	private ServerSocket readSocket;
	private Socket writeSocket;
	
	
	
	public tcpConnection() {
	
	}

	@Override
	public void connect(String destination, int port) {
		if (status == STATUS_IDLE || status == STATUS_FAILED){
			status = STATUS_TRAINING;
			new Thread(new Initialize(port));
		}
	}

	class Initialize implements Runnable{
		int port;

		private Initialize(int port){
			this.port = port;
		}

		@Override
		public void run() {
			try {
				readSocket = new ServerSocket(port);
				System.out.println("Waiting for Client");
				//Wait for the connection to be made, blocks until the connection has been made
				writeSocket = readSocket.accept();

				writeStream = new ObjectOutputStream(writeSocket.getOutputStream());
				readStream = new ObjectInputStream(writeSocket.getInputStream());

				status = STATUS_ALIVE;
			} catch (IOException e) {
				lastError = e;
				System.err.println("Could not listen on port: "+ port);
				status = STATUS_FAILED;
			}
			new Thread(new Sender());
			new Thread(new Receiver());
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
					if (readSocket != null) readSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
				try {
					if (writeSocket != null) writeSocket.close();
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
					if (readSocket != null) readSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
				try {
					if (writeSocket != null) writeSocket.close();
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
					if (readSocket != null) readSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
				try {
					if (writeSocket != null) writeSocket.close();
				}
				catch (IOException e){ e.printStackTrace(); }
			} finally{
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