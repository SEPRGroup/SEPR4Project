package scn.mult;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import btc.Main;

import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;
import scn.Scene;
import svc.BroadcastServer;
import svc.LobbyInfo;
import svc.Server;

public class LobbyConfig  extends Scene{

	private static graphics.Image backgroundImage;
	private lib.ButtonText[] buttons;
	private String name,clientName = "";
	private InetAddress clientIP = null;
	private LobbyInfo User;
	private String ip;
	BroadcastServer host;
	
	public LobbyConfig(Main main,String name){
		this.name = name;
	}
	
	@Override
	public void mousePressed(int key, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(int key) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(int key) {
		// TODO Auto-generated method stub
		switch (key) {
		case input.KEY_ESCAPE :
			main.closeScene();
			break;

		}
	}

	@Override
	public void start() {

		backgroundImage = graphics.newImage("gfx" +File.separator + "mainBackgroundBlurred.png");
		User = new LobbyInfo(name,"Some text here",1);
		try {
			String[] s = InetAddress.getLocalHost().toString().split("/");
			ip = s[1];
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		host = new BroadcastServer(User);
		
		Thread hosting = new Thread(host);
		hosting.start();
		
	}

	@Override
	public void update(double time_difference) {
		// TODO Auto-generated method stub
		if(host.ip != null){
			clientIP = host.ip;
			clientName = host.clientName;
			Server server = new Server();
		}else{
			clientIP = null;
			clientName =  null;
		}
		
	}

	@Override
	public void draw() {
		// TODO Auto-generated method stub
		graphics.draw(backgroundImage, 0, 0, window.scale());
		drawTable(3,6,(int)(100*window.scale()),(int)(200*window.scale()),(int)(1200*window.scale()),(int)(500*window.scale()));
		graphics.print("IP",210, 260,3);
		graphics.print("Name", 380, 240,3);
		graphics.print(ip,120,340,2);
		graphics.print(name, 360, 340,2);
		if(clientIP != null && clientName!=null){
			graphics.print(clientIP.toString().replace("/", ""),120,440,2);
			graphics.print(clientName, 360, 440,2);
		}
		graphics.line(350, 200, 350, 500);
	}

	@Override
	public void close() {
		host.kill();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playSound(Sound sound) {
		// TODO Auto-generated method stub
		
	}
	public void drawTable(int rows, int columns,int x1, int y1, int x2, int y2){
		int lineHeight = (y2 - y1) / rows;
		graphics.setColour(graphics.white);

		//Draw top line of table
		graphics.line(x1,y1+1,x2,y1+1);
		graphics.line(x1,y1+2,x2,y1+2);
		graphics.line(x1,y1+3,x2,y1+3);

		//Draw bottom line of table
		graphics.line(x1,y2+1,x2,y2+1);
		graphics.line(x1,y2+2,x2,y2+2);
		graphics.line(x1,y2+3,x2,y2+3);

		//Draw left line of table
		graphics.line(x1+1,y1,x1+1,y2);
		graphics.line(x1+2,y1,x1+2,y2);
		graphics.line(x1+3,y1,x1+3,y2);

		//Draw right line of table
		graphics.line(x2-1,y1,x2-1,y2);
		graphics.line(x2-2,y1,x2-2,y2);
		graphics.line(x2,y1,x2,y2);

		{
			int lineY = y1;
			for (int i = 0; i < rows-1; i++){
				lineY += lineHeight;
				graphics.line(x1, lineY, x2 ,  lineY);
			}
		}

	}
}
