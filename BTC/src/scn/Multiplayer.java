package scn;

import java.io.File;

import cls.GameWindow;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.window;
import btc.Main;

class Multiplayer extends Scene {
	/**Music to play during the game scene*/
	private audio.Music music;
	
	private int
		oldWidth, oldHeight;
	private final cls.GameWindow 
		game1, game2;

	
	
	public Multiplayer(Main main, int difficulty) {
		super(main);
		
		int	w = window.width(),
			h = window.height(),
			spacing = 8;
		int	gw = (w -3*spacing) /2,
			gh = (Main.TARGET_HEIGHT * h)/(Main.TARGET_HEIGHT +200);
			
		
		game1 = new GameWindow(spacing, spacing, gw, gh, difficulty);
		game2 = new GameWindow((w +spacing)/2, spacing, gw, gh, difficulty);
		
		game1.setControllable(true);
	}
	
	
	@Override
	public void update(double time_difference) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void draw() {
		game1.draw();
		game2.draw();
		
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

	}
	
	
	@Override
	public void start() {
		oldWidth = window.width();
		oldHeight = window.height();
		window.setSize(Main.TARGET_WIDTH*3 /2, Main.TARGET_HEIGHT +200);
		graphics.resize();
		
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		music.play();
	}


	@Override
	public void close() {
		music.stop();
		//if game is still running, restore size.
			//otherwise, closing will be faster
		if (!window.isClosed()){
			window.setSize(oldWidth, oldHeight);
			graphics.resize();
		}
	}

	
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
