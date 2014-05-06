package scn.mult;

import static cls.GameWindow.DIFFICULTY_MEDIUM;

import scn.Demo;
import scn.DifficultySelect;
import scn.Scene;
import btc.Main;
import lib.TextField;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;

public class HostOrFind extends Scene {


	private lib.ButtonText[] buttons;
	
	private static int BUTTON_WIDTH = 400;
	private static int BUTTON_HEIGHT = 200;
	private int nameX;
	private TextField text;
	public HostOrFind(Main main) {
		super(main);
	}
	@Override
	public void mousePressed(int key, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		for (lib.ButtonText button : buttons) {
			if (button.isMouseOver(x, y)) {
				button.act();
			}
		}	
		text.mouseReleased(key, x, y);

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
		text.keyReleased(key);
	}

	@Override
	public void start() {
		nameX = 800;
		buttons = new lib.ButtonText[2];
		text = new TextField(window.width()/2 - 200,300, 400, 30,3);
		lib.ButtonText.Action host = new lib.ButtonText.Action() {
			@Override
			public void action() {
				if(text.getText().length() == 0){
					return;
				}
				main.setScene(new LobbyConfig(main,text.getText()));
				
			}
		};
		buttons[0] = new lib.ButtonText("Host", host, window.width()/4-BUTTON_WIDTH/2, window.height()/2-BUTTON_HEIGHT/2,BUTTON_WIDTH, BUTTON_HEIGHT);
		
		lib.ButtonText.Action find = new lib.ButtonText.Action() {
			@Override
			public void action() {
				if(text.getText().length() == 0){
					return;
				}
				main.setScene(new FindGame(main,text.getText()));
				
			}
		};
		buttons[1] = new lib.ButtonText("Find a game", find, window.width()-window.width()/4-BUTTON_WIDTH/2, window.height()/2-BUTTON_HEIGHT/2,BUTTON_WIDTH, BUTTON_HEIGHT);

	}

	@Override
	public void update(double time_difference) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw() {
		text.draw();
		graphics.setColour(graphics.green);
		graphics.printRight("Name: ", 400, 300, 3, 1);
		graphics.rectangle(false, window.width()/4-BUTTON_WIDTH/2, window.height()/2-BUTTON_HEIGHT/2, BUTTON_WIDTH, BUTTON_HEIGHT);
		graphics.rectangle(false, window.width()-window.width()/4-BUTTON_WIDTH/2, window.height()/2-BUTTON_HEIGHT/2, BUTTON_WIDTH, BUTTON_HEIGHT);
		for (lib.ButtonText button : buttons) {
			button.draw();
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
