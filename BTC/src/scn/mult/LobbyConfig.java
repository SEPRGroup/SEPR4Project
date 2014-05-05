package scn.mult;

import lib.jog.window;
import lib.jog.audio.Sound;
import scn.Scene;

public class LobbyConfig  extends Scene{

	private lib.ButtonText[] buttons;
	
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
		
	}

	@Override
	public void start() {
		
		buttons = new lib.ButtonText[2];

		lib.ButtonText.Action host = new lib.ButtonText.Action() {
			@Override
			public void action() {
				
				main.setScene(new LobbyConfig());
				
			}
		};
		//buttons[0] = new lib.ButtonText("Host", host, window.width()/4-BUTTON_WIDTH/2, window.height()/2-BUTTON_HEIGHT/2,BUTTON_WIDTH, BUTTON_HEIGHT);
		
		lib.ButtonText.Action find = new lib.ButtonText.Action() {
			@Override
			public void action() {
				
				main.setScene(new FindGame(main));
				
			}
		};
		//buttons[1] = new lib.ButtonText("Find a game", find, window.width()-window.width()/4-BUTTON_WIDTH/2, window.height()/2-BUTTON_HEIGHT/2,BUTTON_WIDTH, BUTTON_HEIGHT);
	
	}

	@Override
	public void update(double time_difference) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw() {
		// TODO Auto-generated method stub
		
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
