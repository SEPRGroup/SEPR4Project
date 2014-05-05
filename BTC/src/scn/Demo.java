package scn;

import java.io.File;
import java.util.List;

import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import cls.Aircraft;
import cls.GameWindow;
import btc.Main;

public class Demo extends Scene {
	/**Music to play during the game scene*/
	private audio.Music music;
	
	private final cls.GameWindow game;



	public double getTime() {
		return game.getTime();
	}

	
	/**
	 * Constructor
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public Demo(Main main, int difficulty) {
		super(main);
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		game = new GameWindow(16, 8, 
				window.width() -32, window.height() -16, difficulty);
	}

	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty
	 */
	@Override
	public void start() {	
		music.play();
		
		game.setControllable(true);
	}


	/** cleanly exit by stopping the scene's music */
	@Override
	public void close() {
		music.stop();
	}
	
	
	/** Update game state and handle any game over state */
	@Override
	public void update(double time_difference) {
		game.update(time_difference);
		
		if (game.isGameOver()){
			List<Aircraft> crashed = game.getCrashedAircraft();
			//list of crashed aircraft should contain at least 2 elements
			gameOver(crashed.get(0), crashed.get(1));
		}

	}
	
	
	@Override
	public void playSound(audio.Sound sound) {
		sound.stop();
		sound.play();
	}
	
	
	/**
	 * Handle a game over caused by two planes colliding
	 * Create a gameOver scene and make it the current scene
	 * @param plane1 the first plane involved in the collision
	 * @param plane2 the second plane in the collision
	 */
	public void gameOver(Aircraft plane1, Aircraft plane2) {
		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		main.closeScene();
		main.setScene(new GameOver(main, plane1, plane2, game.getScore()));
	}
	
	
	/** draw the game window */
	@Override
	public void draw() {
		game.draw();
	}

	
	/** Handle mouse input */
	@Override
	public void mousePressed(int key, int x, int y) {
		game.mousePressed(key, x, y);
	}
	

	@Override
	public void mouseReleased(int key, int x, int y) {
		game.mouseReleased(key, x, y);
	}

	@Override
	public void keyPressed(int key) {
		game.keyPressed(key);
	}

	@Override
	/** handle keyboard input */
	public void keyReleased(int key) {
		game.keyReleased(key);
		
		switch (key) {
			case input.KEY_ESCAPE :
				main.closeScene();
			break;
		}
	}
	
	// Necessary for testing	
	/**
	 * This constructor should only be used for unit testing. Its purpose is to allow an instance
	 * of demo class to be created without an instance of Main class (effectively launching the game)
	 * @param difficulty
	 */	
	@Deprecated
	public Demo(int difficulty) {
		game = new GameWindow(16,48, window.width() - 32 -1, window.height() - 176 -1, difficulty);
	}
	
	@Deprecated
	public GameWindow getGame() {
		return game;
	}
}