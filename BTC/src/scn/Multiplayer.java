package scn;

import java.io.File;

import cls.GameWindow;
import cls.TransferBar;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

class Multiplayer extends Scene {
	/**Music to play during the game scene*/
	private audio.Music music;
	
	private int
		oldWidth, oldHeight;
	private final int difficulty;
		
	private cls.GameWindow 
		game1, game2;
	private TransferBar transfers;

	
	
	public Multiplayer(Main main, int difficulty) {
		super(main);
		this.difficulty = difficulty;
		
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
	}
	
	
	@Override
	public void start() {
		oldWidth = window.width();
		oldHeight = window.height();
		window.setSize(Main.TARGET_WIDTH*3 /2, Main.TARGET_HEIGHT);
		graphics.resize();
		
		int	w = window.width(),
			h = window.height(),
			spacing = 8;
		int	gw = (w -3*spacing) /2,
			gh = h -150,
			tw = w -2*spacing,
			th = h -gh -3*spacing;
			

		game1 = new GameWindow(spacing, spacing, gw, gh, difficulty);
		game2 = new GameWindow((w +spacing)/2, spacing, gw, gh, difficulty);
		transfers = new TransferBar(spacing, gh +2*spacing, tw, th, 5000, difficulty);
		
		music.play();
		game1.setControllable(true);
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
	public void update(double timeDifference) {
		game1.update(timeDifference);
		game2.update(timeDifference);
		transfers.update(timeDifference);

		//synchronize scores
		//talk to network stuff
		//track gameovers
		//manage transfers

	}

	
	@Override
	public void draw() {
		game1.draw();
		game2.draw();
		transfers.draw();
	}
	

	@Override
	public void mousePressed(int key, int x, int y) {
		game1.mousePressed(key, x, y);

	}

	
	@Override
	public void mouseReleased(int key, int x, int y) {
		game1.mouseReleased(key, x, y);

	}

	
	@Override
	public void keyPressed(int key) {
		game1.keyPressed(key);

	}

	
	@Override
	public void keyReleased(int key) {
		switch (key) {
		case input.KEY_ESCAPE :
			main.closeScene();
			break;
		}

		game1.keyReleased(key);
	}
	
	
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
