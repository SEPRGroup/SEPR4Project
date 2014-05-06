package svc;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class BroadcastClient implements Runnable {

	public static final String VALIDATION_REQUEST = "SEPR_PSA_REQUEST_54321";
	public static final String JOIN = "SEPR_PSA_JOIN";
	private InetAddress ip;
	private String name;
	private DatagramSocket c;
	public List<BroadcastResponse> pResponses = new ArrayList<BroadcastResponse>();

	public BroadcastClient(){

	}
	public BroadcastClient(InetAddress ip, String name){
		this.ip = ip;
		this.name = name;

	}
	public static void main(String[] args){
		BroadcastClient client = new BroadcastClient();
		client.run();
	}


	@Override
	public void run() {
		/** based off original code
		/***************************************************************************************
		 *    Title: Network discovery using UDP Broadcast (Java)
		 *    Author: Michiel De Mey 
		 *    Date: 2014/05/05
		 *    Code version: N/A
		 *    Availability: http://michieldemey.be/blog/network-discovery-using-udp-broadcast/
		 *
		 ***************************************************************************************/
		try {
			//Open a random port to send the package
			c = new DatagramSocket();
			c.setBroadcast(true);
			c.setSoTimeout(1000);
		}
		catch (SocketException se){
			if (c != null) c.close();
			se.printStackTrace();
			return;
		}
		if(ip == null){
			// Find the server using UDP broadcast
	
			//broadcast request
			byte[] sendData = VALIDATION_REQUEST.getBytes();
			ArrayList<DatagramPacket> responses = new ArrayList<DatagramPacket>();
			try {
				//Try 255.255.255.255 first
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, 
						InetAddress.getByName("255.255.255.255"), BroadcastServer.PORT);
				c.send(sendPacket);
				System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");

				/*{
				// Broadcast the message over all the network interfaces
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while (interfaces.hasMoreElements()) {
					NetworkInterface networkInterface = interfaces.nextElement();

					if (networkInterface.isLoopback() || !networkInterface.isUp()) {
						continue; // Don't want to broadcast to the loopback interface
					}

					for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
						InetAddress broadcast = interfaceAddress.getBroadcast();
						if (broadcast == null) {
							continue;
						}

						// Send the broadcast package!
						try {
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
							c.send(sendPacket);
						} catch (Exception e) {
						}

						System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
					}
				}

				System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
			}*/


				//Wait for a response 
				while (true){	//while responses are arriving, continue
					try{
						byte[] recvBuf = new byte[15000];
						DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
						c.receive(receivePacket);
						responses.add(receivePacket);
					}
					catch (SocketTimeoutException se){
						break; //no more responses have arrived; finish
					}
				}

			} catch (IOException e){
				e.printStackTrace();
				return;
			}
			finally {
				c.close();	//Close the port!
			}

			//We have a response?
			for (DatagramPacket r : responses){
				System.out.println(getClass().getName() + ">>> Broadcast response from server: " + r.getAddress().getHostAddress());
				//Check if the message is correct
				String message = new String(r.getData()).trim();
				if (message.startsWith(BroadcastServer.VALIDATION_CONFIRM)){
					//DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
					pResponses.add(new BroadcastResponse(r.getAddress(),
							message.substring(BroadcastServer.VALIDATION_CONFIRM.length())));
				}
			}

			for (BroadcastResponse r: pResponses){
				System.out.print(r.responder +":\t");
				System.out.println(r.response);
				System.out.println();
			}
		}else{
			
			byte[] sendData = null;
			String s = JOIN + "@" + name;
			sendData = s.getBytes() ;
			
				//Try 255.255.255.255 first
				DatagramPacket sendPacket;
				try {
				
					sendPacket = new DatagramPacket(sendData, sendData.length, 
							ip, BroadcastServer.PORT);
					c.send(sendPacket);
					System.out.println(getClass().getName() + ">>> Join packet sent to: "+ ip.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
		}

	}

	public class BroadcastResponse{

		public final InetAddress responder;
		public final String response;

		private BroadcastResponse(InetAddress responder, String response) {
			this.responder = responder;
			this.response = response;
		}

	}
}

