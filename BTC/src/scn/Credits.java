package scn;

import java.io.File;

import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

public class Credits extends Scene {
	
	/**
	 * Default speed to scroll the credits
	 */
	private final static int SCROLL_SPEED = 64;
	
	private float speed;
	/**
	 * The position to print the credits text at. Initially offscreen
	 */
	private double scroll_position;
	/**
	 * Music to play during the credits
	 */
	private audio.Music music;
	
	/**
	 * Background image for credits
	 */
	private static graphics.Image backgroundImage;

	/**
	 * Constructor
	 * @param main The main containing the scene
	 */
	public Credits(Main main) {
		super(main);
	}
	
	/**
	 * Input handlers
	 */
	@Override
	public void mousePressed(int key, int x, int y) { }

	@Override
	public void mouseReleased(int key, int x, int y) { }

	@Override
	public void keyPressed(int key) { }

	/**
	 * Exit to the title screen if escape is pressed
	 */
	@Override
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
	}
	
	/**
	 * Initiate music, and the credits text to be offscreen
	 */
	@Override
	public void start() {
		backgroundImage = graphics.newImage("gfx" +File.separator + "mainBackgroundBlurred.png");
		speed = 1f;
		scroll_position = -window.height();
		music = audio.newMusic("sfx" + File.separator + "piano.ogg");
		music.play();
	}

	@Override
	/**
	 * Update the credits's scroll position
	 * Speed up the credits movement if keys are pressed
	 * Close the credits if they have finished scrolling
	 */
	public void update(double time_difference) {
		boolean hurried = input.isKeyDown(input.KEY_SPACE) || input.isMouseDown(input.MOUSE_LEFT);
		speed = hurried ? 4f : 1f;
		scroll_position += SCROLL_SPEED * time_difference * speed;
		if (scroll_position > 1800)
			main.closeScene();
	}

	/**
	 * Print the credits based on the current scroll position
	 */
	@Override
	public void draw() {
		graphics.draw(backgroundImage, 0, 0, window.scale());
		int gap = 64;
		int currentHeight = 0;
		graphics.setColour(graphics.white);
		graphics.push();
		graphics.translate(0, scroll_position);
		currentHeight += gap;
		graphics.printCentred("Bear Traffic Controller", 0, currentHeight, 3, window.width());
		currentHeight += gap * 2;
		
		graphics.printCentred("Created by", 0, currentHeight, 2, window.width());
		graphics.printCentred("___________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("__________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Team FLR:", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		
		graphics.printCentred("Josh Adams", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Gareth Handley", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Sanjit Samaddar", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Alex Stewart", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Huw Taylor", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Stephen Webb", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;	
		
		graphics.printCentred("Improved by", 0, currentHeight, 2, window.width());
		graphics.printCentred("___________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("__________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Team MQV:", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		
		graphics.printCentred("Adam Al-jidy", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jakub Brezonak", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jack Chapman", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Liam Mullane", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Matt Munro", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Liam Wellacott", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;
		
		graphics.printCentred("Perfected by", 0, currentHeight, 2, window.width());
		graphics.printCentred("____________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("___________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Team PSA:", 0, currentHeight, 2, window.width());
		currentHeight += gap;
		
		graphics.printCentred("Jake Digweed", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Charlie Ford", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Matthew Hands", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Stephen Jenkins", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Zivile Lisauskaite", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("Karl Sonley", 2 * window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap;
		graphics.printCentred("Pat Squires", window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;

		graphics.printCentred("Music", 0, currentHeight, 2, window.width());
		graphics.printCentred("_____", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("____", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Gypsy Shoegazer", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Beep SFX", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap / 2;
		graphics.printCentred("Kevin MacLeod", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Partners in Rhyme", 2*window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("FreeSound", window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;
		
		graphics.printCentred("Graphics", 0, currentHeight, 2, window.width());
		graphics.printCentred("________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("_______", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("Landing Plane", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Luc Gibson", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("gibsondesigns.co.uk", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap / 2;
		graphics.printCentred("Plane Design", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Jeremy Sallee", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("salleedesign.com", 2*window.width()/3, currentHeight, 2, window.width()/3);
		
		currentHeight += gap * 2;
		
		graphics.printCentred("External Libraries", 0, currentHeight, 2, window.width());
		graphics.printCentred("__________________", 0, currentHeight + 8, 2, window.width());
		graphics.printCentred("_________________", 4, currentHeight + 8, 2, window.width());
		currentHeight += gap;
		graphics.printCentred("LWJGL", 0, currentHeight, 2, window.width()/3);
		graphics.printCentred("Slick2D", window.width()/3, currentHeight, 2, window.width()/3);
		graphics.printCentred("JOG", 2*window.width()/3, currentHeight, 2, window.width()/3);
		currentHeight += gap * 2;
		
		graphics.printCentred("Thanks for playing!", 0, currentHeight, 2, window.width());
		graphics.pop();
	}

	@Override
	public void close() {
		music.stop();
	}

	@Override
	public void playSound(Sound sound) {	
		
	}

}
