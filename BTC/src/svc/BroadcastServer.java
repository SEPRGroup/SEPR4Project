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
	public static final int PORT = 10008;
	public static final String VALIDATION_CONFIRM = "SEPR_PSA_VALIDRESPONSE_12345";

	DatagramSocket socket;
	private boolean alive = false;
	LobbyInfo info;


	public BroadcastServer(LobbyInfo info) {
		super();
		this.info = info;
	}
	
	
	public void kill(){
		alive = false;
	}
	
	
	@Override
	public void run() {
		
		//Keep a socket open to listen to all the UDP traffic that is destined for this port
		try {
			socket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);
			socket.setSoTimeout(1000);
		} catch (Exception e) {
			socket.close();
			e.printStackTrace();
			return;
		}

		//listen
		try {
			System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
			alive = true;
			byte[] recvBuf = new byte[150000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			byte[] sendData = (VALIDATION_CONFIRM +info.getCommandString()).getBytes();

			while (alive) {
				try {
					socket.receive(receivePacket); //Receive a packet
				} catch (SocketTimeoutException ex) {
					continue;	//timeout; perform check and continue
				}

				//See if the packet holds the right command (message)
				String message = new String(receivePacket.getData()).trim();
				if (message.equals(BroadcastClient.VALIDATION_REQUEST)) {
					//Packet received
					System.out.println(getClass().getName() + ">>>Discovery packet received from: " + receivePacket.getAddress().getHostAddress());
					System.out.println(getClass().getName() + ">>>Packet received; data: " +new String(receivePacket.getData()));
					//Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
					try {
						socket.send(sendPacket);
						System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
					} catch (IOException e) {
						System.out.println(">>>Failed sending packet to: " + sendPacket.getAddress().getHostAddress());
					}
				}

			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			socket.close();
			System.out.println(getClass().getName() + ">>>Stopped receiving broadcast packets!");
		}
	}
	
	public static void main(String[] args){
		LobbyInfo test = new LobbyInfo("name","destination",1);
		BroadcastServer server = new BroadcastServer(test);
		server.run();
	}

}

