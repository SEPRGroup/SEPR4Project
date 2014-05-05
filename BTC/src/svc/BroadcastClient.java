package svc;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class BroadcastClient implements Runnable {
	DatagramSocket c;

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
		
		// Find the server using UDP broadcast
		try {
			//Open a random port to send the package
			c = new DatagramSocket();
			c.setBroadcast(true);

			byte[] sendData = "SEPR_PSA_REQUEST_54321".getBytes();

			//Try the 255.255.255.255 first
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8888);
				c.send(sendPacket);
				System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
			} catch (Exception e) {
			}

			/*// Broadcast the message over all the network interfaces
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
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
						c.send(sendPacket);
					} catch (Exception e) {
					}

					System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
				}
			}

			System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
			*/

			//Wait for a response
			ArrayList<DatagramPacket> responses = new ArrayList<DatagramPacket>(); 
			c.setSoTimeout(1000);
			while (true){
				try{
					byte[] recvBuf = new byte[15000];
					DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
					c.receive(receivePacket);
					responses.add(receivePacket);
				}
				catch (java.net.SocketTimeoutException e){
					break;
				}
			}
			//Close the port!
			c.close();

			//We have a response?
			ArrayList<BroadcastResponse> pResponses = new ArrayList<BroadcastResponse>();
			for (DatagramPacket r : responses){
				System.out.println(getClass().getName() + ">>> Broadcast response from server: " + r.getAddress().getHostAddress());
				//Check if the message is correct
				String message = new String(r.getData()).trim();
				String[] data = message.split("@");
				if ((data.length > 0) && data[0].equals("SEPR_PSA_VALIDRESPONSE_12345")) {
					//DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
					pResponses.add(new BroadcastResponse(r.getAddress(), data));
				}
			}
			
			for (BroadcastResponse r: pResponses){
				System.out.print(r.responder +":\t");
				for (String s: r.response){
					System.out.print(s +", ");
				}
				System.out.println();
			}
			


		} catch (IOException ex) {

		}
	}
	
	private class BroadcastResponse{
		
		public final InetAddress responder;
		public final String[] response;
		
		private BroadcastResponse(InetAddress responder, String[] response) {
			this.responder = responder;
			this.response = response;
		}
		
	}
}

