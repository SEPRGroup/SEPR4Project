package svc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

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
	public InetAddress ip;
	public String clientName;
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
			if (socket != null){
				socket.close();
			}
			e.printStackTrace();
			return;
		}

		//listen
		String[] temp = new String[2];
		try {
			System.out.println(getClass().getName() + ">>>Ready to receive broadcast packets!");
			alive = true;
			byte[] recvBuf = new byte[150000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			byte[] sendData = (VALIDATION_CONFIRM +info.getCommandString()).getBytes();

			while (alive) {
				try {
					recvBuf = new byte[150000];
					receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
					socket.receive(receivePacket); //Receive a packet
				} catch (SocketTimeoutException ex) {
					continue;	//timeout; perform check and continue
				}

				//See if the packet holds the right command (message)
				String message = new String(receivePacket.getData()).trim();
				String s = message;
				if(message.contains("@")){
					s = message;
					temp = null;
					temp = message.split("@");
					message = temp[0];
				}
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
				}else if(message.equals(BroadcastClient.JOIN)){
					ip = receivePacket.getAddress();
					clientName = temp[1];
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

		final LobbyInfo test = new LobbyInfo("name","description",1);
		BroadcastServer server = new BroadcastServer(test);
		server.run();
	}

}

