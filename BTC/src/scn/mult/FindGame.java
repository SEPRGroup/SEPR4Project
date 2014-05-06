package scn.mult;

import static cls.GameWindow.DIFFICULTY_MEDIUM;

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
import btc.Main;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;

public class FindGame extends Scene {


	private lib.ButtonText[] buttons;
	BroadcastClient client;
	Thread clientThread;
	private int scene; 
	private String name;
	private List<BroadcastResponse> responses = new CopyOnWriteArrayList<BroadcastResponse>();
	
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
		
	}

@Override
public void update(double time_difference) {
	if(clientThread!= null){
		if(client.pResponses.size() > responses.size()){
			for(int i = responses.size(); i <client.pResponses.size();i++){
				responses.add(client.pResponses.get(i));
			}
			populateButtons();
		}
		
		if(!clientThread.isAlive()){
			clientThread = null;
			client = null;
		}
		
		
	}
}

public void drawTable(int rows, int columns,int x1, int y1, int x2, int y2){
	int lineHeight = (y2 - y1) / rows;
	graphics.setColour(graphics.green);

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
	drawTable(5,6,(int)(100*window.scale()),(int)(200*window.scale()),(int)(1200*window.scale()),(int)(600*window.scale()));
	graphics.print("IP",210, 240,3);
	graphics.print("Name", 380, 240,3);
	graphics.setColour(graphics.green);
	int count = 0;
	String[] fields = new String[5];
	for( BroadcastResponse r :responses){
		fields = r.response.split("@");
		graphics.print(r.responder.toString().replace("/","") , 120, 320 + count*80,2);
		graphics.print(fields[0] , 360, 320 + count*80,2);
		count++;
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
