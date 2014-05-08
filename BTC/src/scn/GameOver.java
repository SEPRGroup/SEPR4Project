package scn;

import java.io.File;

import cls.Aircraft;
import cls.Vector;
import lib.SpriteAnimation;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.window;
import btc.Main;

public class GameOver extends Scene {
	/**
	 * Text box to write the details of the game failure
	 */
	private lib.TextBox text_box;
	
	/**
	 * Used to position the explosion, and provide graphical feedback of how and where the player failed
	 */
	private Aircraft crashed_plane_1;
	private Aircraft crashed_plane_2;
	/**
	 * A random number of deaths caused by the crash
	 */
	private int num_deaths;
	
	private int score; // Passed in from Demo
	
	/**
	 * The position of the crash - the vector midpoint of the positions of the two crashed planes
	 */
	private Vector crash_position;
	/**
	 * A sprite animation to handle the frame by frame drawing of the explosion
	 */
	private SpriteAnimation explosion_animation;
	/**
	 * The explosion image to use for the animation
	 */
	private Image explosion;
	
	private int keyPressed;
	
	/**
	 * Timer to allow for explosion and plane to be shown for a period, followed by the text box.
	 */
	private double timer;
	
	/**
	 * Constructor for the Game Over scene
	 * @param main main containing the scene
	 * @param plane_1 the first plane involved in the crash
	 * @param plane_2 the second plane involved in the crash
	 * @param score the score when the game finished
	 */
	public GameOver(Main main, Aircraft plane_1, Aircraft plane_2, int score) {
		super(main);
		crashed_plane_1 = plane_1;
		crashed_plane_2 = plane_2;
		crash_position = new Vector(plane_1.getPosition().getX(), plane_1.getPosition().getY(), 0);
		int frames_across = 8;
		int frames_down = 4;
		this.score = score;
		explosion = graphics.newImage("gfx" + File.separator + "explosionFrames.png");
		Vector midpoint = crashed_plane_1.getPosition().add(crashed_plane_2.getPosition()).scaleBy(0.5);
		Vector explosion_position = midpoint.sub(new Vector(explosion.width()/(frames_across*2), explosion.height()/(frames_down*2), 0));
		explosion_animation = new SpriteAnimation(explosion, (int)explosion_position.getX(), (int)explosion_position.getY(), 6, 16, frames_across, frames_down, false);
	}
	
	/**
	 * Initialises the random number of deaths, timer, and text box with strings to be written about the game failure
	 */
	@Override
	public void start() {
		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		num_deaths = (int)(Math.random() * 500) + 300;
		timer = 0;
		text_box = new lib.TextBox(64, 186, window.width() - 128, window.height() - 96, 32);
		text_box.addText(String.valueOf(num_deaths) + " people died in the crash.");
		text_box.delay(0.4);
		text_box.addText("You are the worst air traffic controller ever.");
		text_box.newlines(2);
		text_box.delay(0.8);
		text_box.addText("The guilt for the death you have caused and your failure to pass as a superhero will persecute you forever and you will revert to drinking in an attempt to cope.");
		text_box.newlines(2);
		text_box.delay(0.8);
		text_box.addText("Now you will remember.");
		text_box.newlines(2);
		text_box.delay(0.8);
		text_box.addText("Game Over.");
	}

	/**
	 * If it runs before the explosion has finished, update the explosion
	 * otherwise, update text box instead
	 */
	@Override
	public void update(double time_difference) {
		if (explosion_animation.hasFinished()){
			timer += time_difference;
			text_box.update(time_difference);
		} else {
			explosion_animation.update(time_difference);
		}
	}

	@Override
	public void mousePressed(int key, int x, int y) { }

	@Override
	public void mouseReleased(int key, int x, int y) { }

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

	/**
	 * Draws game over
	 * If explosion has finished, draw the textbox
	 * Otherwise, draw the planes and explosion
	 */
	@Override
	public void draw() {
		graphics.setColour(graphics.white);
		graphics.printCentred(crashed_plane_1.getName() + " crashed into " + crashed_plane_2.getName() + ".", 0, 32, 2, window.width());
		graphics.printCentred("Total score: " + String.valueOf(score), 0, 64, 4, window.width());
		if (explosion_animation.hasFinished()) {
			text_box.draw();
		} else {
			crashed_plane_1.draw();
			crashed_plane_2.draw();
			Vector midPoint = crash_position.add(crashed_plane_2.getPosition()).scaleBy(0.5);
			double radius = 20; // Radius of explosion
			graphics.setColour(graphics.red);
			graphics.circle(false, midPoint.getX(), midPoint.getY(), radius);
			explosion_animation.draw();
		}
		int opacity = (int)(255 * Math.sin(timer));
		graphics.setColour(0, 128, 0, opacity);
		graphics.printCentred("Press any key to continue", 0, window.height() - 256, 1, window.width());
	}

	@Override
	public void close() { }

	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}

}