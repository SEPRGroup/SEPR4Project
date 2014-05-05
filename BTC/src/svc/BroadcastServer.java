package svc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/***************************************************************************************
 *    Title: Network discovery using UDP Broadcast (Java)
 *    Author: Michiel De Mey 
 *    Date: 2014/05/05
 *    Code version: N/A
 *    Availability: http://michieldemey.be/blog/network-discovery-using-udp-broadcast/
 *
 ***************************************************************************************/
public class BroadcastServer implements Runnable {

	DatagramSocket socket;
	private boolean alive = false;
	LobbyInfo info;
	public static final int port = 10008;


	public BroadcastServer(LobbyInfo info) {
		super();
		this.info = info;
	}
	public void kill(){
		alive = false;
	}
	@Override
	public void run() {

		//Keep a socket open to listen to all the UDP trafic that is destined for this port
		try {
			socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);
			socket.setSoTimeout(1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		alive = true;
		System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
		while (alive) {

			
			try {
				//Receive a packet
				byte[] recvBuf = new byte[150000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);




				//See if the packet holds the right command (message)
				String message = new String(packet.getData()).trim();
				if (message.equals("SEPR_PSA_REQUEST_54321")) {

					byte[] sendData = ("SEPR_PSA_VALIDRESPONSE_12345@"+ info.toString()).getBytes();

					//Packet received
					System.out.println(getClass().getName() + ">>>Discovery packet received from: " + packet.getAddress().getHostAddress());
					System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()));
					//Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
					socket.send(sendPacket);

					System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
				}
			} catch (IOException ex) {
				//Logger.getLogger(BroadcastServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println(getClass().getName() + ">>>Stopped receiving broadcast packets!");

	}
	public static void main(String[] args){
		LobbyInfo test = new LobbyInfo("name","destination",1);
		BroadcastServer server = new BroadcastServer(test);
		server.run();
	}

}

