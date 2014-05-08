package scn.mult;

import lib.jog.graphics;
import lib.jog.window;
import lib.jog.audio.Sound;
import scn.Scene;
import btc.Main;

public class GameEnd extends Scene{
	
	private int score; // Passed in from Demo
	private Boolean winner;
	
	private lib.TextBox textBox;
	
	private int keyPressed;
	private double timer = 0;

	
	public GameEnd(Main main, int score, Boolean winner) {
		super(main);
		this.score = score;
		this.winner = winner;
		textBox = new lib.TextBox(64, 186, window.width() - 128, window.height() - 96, 32);
	}

	@Override
	public void mousePressed(int key, int x, int y) {}

	@Override
	public void mouseReleased(int key, int x, int y) {}

	/**
	 * Tracks if any keys are pressed when the game over screen begins
	 * Prevents the scene instantly ending due to a key press from previous scene
	 */
	@Override
	public void keyPressed(int key) {
		keyPressed = key;
	}

	/**
	 * Ends the scene if any key is released
	 */
	@Override
	public void keyReleased(int key) {
		if (key == keyPressed) {
			main.closeScene(); // Close this screen
		}
	}

	@Override
	public void start() {
		if (winner){
			textBox.addText("You win!");
		}
		else {
			textBox.addText("You lose...");
		}
		textBox.delay(0.8);
		textBox.addText("Your score:");
		textBox.delay(0.4);
		textBox.addText(String.valueOf(score));
	}

	@Override
	public void update(double time_difference) {
		timer += time_difference;
		textBox.update(time_difference);
	}

	@Override
	public void draw() {
		textBox.draw();
		
		int opacity = (int)(255 * Math.sin(timer));
		graphics.setColour(0, 128, 0, opacity);
		graphics.printCentred("Press any key to continue", 0, window.height() - 256, 1, window.width());		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}
