package scn;

import java.io.File;

import cls.Aircraft;
import cls.GameWindow;
import cls.TransferBar;
import cls.GameWindow.TransferBuffer;
import lib.RandomNumber;
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
		transfers = new TransferBar(spacing, gh +2*spacing, tw, th, 2500, difficulty);
		
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
		
		{	//manage transfers
			double rand = Math.random();
			/*if (rand < 0.1*timeDifference){
				java.util.List<Aircraft> c = game1.getAircraftList();
				transfers.enterLeft(c.get(RandomNumber.randInclusiveInt(0, c.size()-1)));				
			}
			else*/ if (rand < 0.05*timeDifference){
				java.util.List<Aircraft> c = game2.getAircraftList();
				transfers.enterRight(c.get(RandomNumber.randInclusiveInt(0, c.size()-1)));				
			}
		}
		
		{	//handle transfers out
			for (TransferBuffer tb : game1.transfers){
				if (tb.name == "City of Rightson"){
					Aircraft a = tb.pollOut();
					while (a != null) {
						System.out.println(a.getName() +" transferred out");
						//{!} notify other player
						transfers.enterLeft(a);
						a = tb.pollOut();
					};
				}
				else tb.clearOut();
			}
		}
		transfers.update(timeDifference);
		{	//handle transfers in
			Aircraft a = transfers.pollLeft();
			while (a != null) {
				for (TransferBuffer tb : game1.transfers){
					if (tb.name == "South Sea"){
						System.out.println(a.getName() +" transferred in");
						tb.transferIn(a);
						break;
					}
				}
				a = transfers.pollLeft();
			}
		}

		/*a = transfers.pollRight();
		if (a != null) System.out.println("Right:\t" +a.getName());*/
		

		//synchronize scores
		//talk to network stuff
		//track gameovers
		


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
