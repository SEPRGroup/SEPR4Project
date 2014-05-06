package svc;

import java.util.concurrent.ArrayBlockingQueue;

/** class implementing NetworkIO that guarantees objects are transmitted*/
public class tcpConnection implements NetworkIO {
	
	private int status = NetworkIO.STATUS_IDLE;
	private Exception lastError = null;
	
	private ArrayBlockingQueue<Object>
		out = new ArrayBlockingQueue<Object>(20),
		in = new ArrayBlockingQueue<Object>(20);
	

	public tcpConnection() {
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void connect(String destination, String port) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void sendObject(Object obj) {
		out.add(obj);
	}

	
	@Override
	public Object pollObjects() {
		if (!in.isEmpty()) 
			return in.poll();
		else return null;
	}
	
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public Boolean isAlive() {
		// TODO Auto-generated method stub
		return null;
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
