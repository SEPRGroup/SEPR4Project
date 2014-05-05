package scn;

import static cls.GameWindow.DIFFICULTY_MEDIUM;
import scn.Demo;
import scn.DifficultySelect;
import scn.Scene;
import btc.Main;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import lib.jog.audio.Sound;

public class FindGame extends Scene {

	
	private lib.ButtonText[] buttons;
	
	private int scene; 
	
	public FindGame(Main main) {
		super(main);
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
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
	}

	@Override
	public void start() {
		
		buttons = new lib.ButtonText[1];
		
		lib.ButtonText.Action host = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene){
				case DifficultySelect.CREATE_DEMO:
					main.setScene(new Demo(main, DIFFICULTY_MEDIUM));
					break;
				}
			}
		};
		buttons[0] = new lib.ButtonText("Host", host, 200, 300, 50, 10);
		
		
	}

	@Override
	public void update(double time_difference) {
		// TODO Auto-generated method stub
		
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
		graphics.line(x2-3,y1,x2-3,y2);
		for (int i = 0; i <= rows; i++){
			graphics.line(x1,y1 + i * lineHeight, x2 , y1 + i * lineHeight);
		}
	}
	@Override
	public void draw() {
		// TODO Auto-generated method stub
		drawTable(5,6,100,200,1200,600);
		graphics.setColour(graphics.green);
		for (lib.ButtonText button : buttons) {
			button.draw();
		}
		graphics.rectangle(false, 200, 300, 50, 10);
		graphics.line(300, 70, window.width() - window.width()/6, 48);
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
