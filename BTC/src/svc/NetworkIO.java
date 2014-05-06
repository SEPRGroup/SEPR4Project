package svc;

public interface NetworkIO {
	
	/**connection has not been utilized*/
	public static final int STATUS_IDLE = 0;
	/**attempting to establish a connection*/
	public static final int STATUS_TRAINING =1;
	/**connection is active and will function*/
	public static final int STATUS_ALIVE = 2;
	/**connection has failed and will not function*/
	public static final int STATUS_FAILED = 3;
	
	
	/**
	 * Attempt to initiate a connection  
	 * @param destination the network address to connect to
	 * @param port the port to connect to
	 */
	public void connect(String destination, String port);
	
	/** Close the connection */
	public void close();
		
	/** 
	 * Send the object to connected parties: non-blocking
	 * @param obj The object to be sent
	 */
	public void sendObject(Object obj);
	
	/** 
	 * Get the oldest remaining Object that has been received: non-blocking
	 * @return the Object, or else null
	 */
	public Object pollObjects();
	
	/**@return if the connection has been maintained */
	public Boolean isAlive();
	
	/**@return the last fatal error encountered */
	public Exception getLastError();
	
	/**@return the status of the connection  */
	public int getStatus();
}
