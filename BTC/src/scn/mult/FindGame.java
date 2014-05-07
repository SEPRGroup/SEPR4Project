package scn.mult;

import static cls.GameWindow.DIFFICULTY_MEDIUM;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import scn.Demo;
import scn.DifficultySelect;
import scn.Scene;
import svc.BroadcastClient;
import svc.BroadcastClient.BroadcastResponse;
import svc.LobbyInfo;
import svc.tcpConnection;
import btc.Main;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;

import static svc.NetworkIO.*;

public class FindGame extends Scene {


	private lib.ButtonText[] buttons;
	BroadcastClient client;
	Thread clientThread;
	private int scene; 
	private String name;
	private List<BroadcastResponse> responses = new CopyOnWriteArrayList<BroadcastResponse>();
	private List<LobbyInfo> info = new ArrayList<LobbyInfo>();
	private static graphics.Image backgroundImage;
	private tcpConnection connection = new tcpConnection();
	private int lobbyIndex;
	
	public FindGame(Main main, String name) {
		super(main);
		this.name = name;
	}
	@Override
	public void mousePressed(int key, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int key, int x, int y) {
	
		for (lib.ButtonText b : buttons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
	}

	@Override
	public void keyPressed(int key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
	}

	@Override
	public void start() {
		
		backgroundImage = graphics.newImage("gfx" +File.separator + "mainBackgroundBlurred.png");
		client = new BroadcastClient();
		clientThread = new Thread(client);
		clientThread.start();

		
}
	private void populateButtons(){		
		
		buttons = new lib.ButtonText[client.pResponses.size()];
		int count = 0;
		for( BroadcastResponse r: responses){
			final int i= count;
			buttons[count] = new lib.ButtonText("Join", 
			new lib.ButtonText.Action(){
				@Override
				public void action() { handleJoinClick(i); }	
			},
			700, 320 + count*80,30, 12);

			count++;
		}
		
	}
	private void handleJoinClick(int index){
		System.out.println(index);
		System.out.println(responses.get(index).responder);
		
		client = new BroadcastClient(responses.get(index).responder,name);
		Thread c = new Thread(client);
		c.start();
		connection.connect(responses.get(index).responder.getHostAddress(), tcpConnection.TCP_PORT);
		for (lib.ButtonText button : buttons) {
			button.setAvailability(false);
		}
		lobbyIndex = index;
	}

@Override
public void update(double time_difference) {
	if(clientThread!= null){
		if(client.pResponses.size() > responses.size()){
			for(int i = responses.size(); i <client.pResponses.size();i++){
				responses.add(client.pResponses.get(i));
				try {
					info.add(new LobbyInfo(responses.get(i).response));
				} catch (Exception e) { e.printStackTrace();}
			}
			populateButtons();
		}
		
		if(!clientThread.isAlive()){
			clientThread = null;
			client = null;
		}
	}
	switch (connection.getStatus()){
	case STATUS_IDLE: break;
	case STATUS_TRAINING:
		//print connecting
		graphics.print("Connecting to" + info.get(lobbyIndex).name+ "...", 200, 100);
		break;
	case STATUS_ALIVE:
		//advance to multiplayer
		//main.setScene(new scn.Multiplayer(main,info.get(lobbyIndex).difficulty,connection);
		break;
	case STATUS_FAILED:
		//rebroadcast; reeanable buttons
		if(buttons != null){
			for (lib.ButtonText button : buttons) {
				button.setAvailability(true);
			}
		}
		connection  = new tcpConnection();
		break;
	}
	
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
@Override
public void draw() {
	// TODO  Auto-generated method stub
	graphics.draw(backgroundImage, 0, 0, window.scale());
	drawTable(5,6,(int)(100*window.scale()),(int)(200*window.scale()),(int)(1200*window.scale()),(int)(600*window.scale()));
	graphics.print("IP",210, 240,3);
	graphics.print("Name", 380, 240,3);
	graphics.setColour(graphics.white);

	for( int i = 0; i < info.size(); i ++){
		BroadcastResponse r = responses.get(i);
		graphics.print(r.responder.getHostAddress() , 120, 320 + i*80,2);
		graphics.print(info.get(i).name , 360, 320 + i*80,2);
	
	}
	
	graphics.line(350, 200, 350, 600);
	if(buttons != null){
		for (lib.ButtonText button : buttons) {
			button.draw();
		}
	}
}

@Override
public void close() {
	// TODO Auto-generated method stub

}

@Override
public void playSound(Sound sound) {
	// TODO Auto-generated method stub

}

}
