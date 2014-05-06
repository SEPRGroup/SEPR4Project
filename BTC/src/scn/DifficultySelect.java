package scn;

import java.io.File;

import lib.TextBox;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;

import static cls.GameWindow.DIFFICULTY_EASY;
import static cls.GameWindow.DIFFICULTY_MEDIUM;
import static cls.GameWindow.DIFFICULTY_HARD;


public class DifficultySelect extends Scene {
	
	private final int EASY_BUTTON_X = window.width()/4;
	private final int EASY_BUTTON_Y = 2*window.height()/3;
	private final int EASY_BUTTON_WIDTH = 128;
	private final int EASY_BUTTON_HEIGHT = 16;
	
	private final int MEDIUM_BUTTON_X = window.width()/2;
	private final int MEDIUM_BUTTON_Y = EASY_BUTTON_Y;
	private final int MEDIUM_BUTTON_WIDTH = EASY_BUTTON_WIDTH;
	private final int MEDIUM_BUTTON_HEIGHT = EASY_BUTTON_HEIGHT;
	
	private final int HARD_BUTTON_X = 3*window.width()/4;
	private final int HARD_BUTTON_Y = EASY_BUTTON_Y;
	private final int HARD_BUTTON_WIDTH = EASY_BUTTON_WIDTH;
	private final int HARD_BUTTON_HEIGHT = EASY_BUTTON_HEIGHT;
	
	/**
	 * Background image for difficulty selection
	 */
	private static graphics.Image backgroundImage;
	
	
	private lib.ButtonText[] buttons;
	// Text box for flavour text
	private lib.TextBox text_box;
	private static final String place_name = "Moscow";
	
	// To allow the difficulty selection to work with multiple potential game scenes, e.g. separate Demo and a Full Game
	private int scene;
	// static ints for clarity of reading. Implement more to allow more game scenes.
	public final static int CREATE_DEMO = 0;

	/**
	 * Constructor
	 * @param main the main containing the scene
	 * @param scene the scene to create e.g. Demo
	 */
	protected DifficultySelect(Main main, int scene) {
		super(main);
		this.scene = scene;
	}

	@Override
	public void mousePressed(int key, int x, int y) { }

	@Override
	/**
	 * Causes a button to act if mouse released over it
	 */
	public void mouseReleased(int key, int x, int y) {
		for (lib.ButtonText b : buttons) {
			if (b.isMouseOver(x, y)) {
				b.act();
			}
		}
	}

	@Override
	public void keyPressed(int key) { }

	@Override
	/**
	 * Quits back to title scene on escape button
	 */
	public void keyReleased(int key) {
		if (key == input.KEY_ESCAPE) {
			main.closeScene();
		}
	}

	@Override
	/**
	 * Initialises scene variables, buttons, text box.
	 */
	public void start() {
		backgroundImage = graphics.newImage("gfx" +File.separator + "mainBackgroundBlurred.png");
		buttons = new lib.ButtonText[3];
		lib.ButtonText.Action easy = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene){
				case DifficultySelect.CREATE_DEMO:
					main.setScene(new Demo(main, DIFFICULTY_EASY));
					break;
				}
			}
		};
		buttons[0] = new lib.ButtonText("Easy", easy, EASY_BUTTON_X, EASY_BUTTON_Y, EASY_BUTTON_WIDTH, EASY_BUTTON_HEIGHT);
		
		lib.ButtonText.Action medium = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene){
				case DifficultySelect.CREATE_DEMO:
					main.setScene(new Demo(main, DIFFICULTY_MEDIUM));
					break;
				}
			}
		};
		buttons[1] = new lib.ButtonText("Medium", medium, MEDIUM_BUTTON_X, MEDIUM_BUTTON_Y, MEDIUM_BUTTON_WIDTH, MEDIUM_BUTTON_HEIGHT);
		
		lib.ButtonText.Action hard = new lib.ButtonText.Action() {
			@Override
			public void action() {
				switch (scene){
				case DifficultySelect.CREATE_DEMO:
					main.setScene(new Demo(main, DIFFICULTY_HARD));
					break;
				}
			}
		};
		buttons[2] = new lib.ButtonText("Hard", hard, HARD_BUTTON_X, HARD_BUTTON_Y, HARD_BUTTON_WIDTH, HARD_BUTTON_HEIGHT);
		
		text_box = new lib.TextBox(128, 96, window.width() - 256, window.height() - 96, 32);
		text_box.addText("The time has come." + TextBox.DELAY_START + "0.5" + TextBox.DELAY_END + " You are the chosen one. ");
		text_box.delay(0.5);
		text_box.addText("You have become an air traffic controller at " + DifficultySelect.place_name + " international airport to fulfil your superhero mission.");
		text_box.delay(0.5);
		text_box.newline();
		text_box.addText("You will now experience what you have never thought you would.");
		text_box.newlines(3);
		text_box.delay(1);
		text_box.addText("Guide planes to their destination successfully and you will be rewarded." + TextBox.DELAY_START + "0.5" + TextBox.DELAY_END +  " So, get ready! ");
						
						
	}

	/**
	 * Updates text box
	 */
	@Override
	public void update(double time_difference) {
		text_box.update(time_difference);
	}

	/**
	 * Draws text box, buttons, and prints strings
	 */
	@Override
	public void draw() {
		graphics.draw(backgroundImage, 0, 0, window.scale());
		graphics.setColour(graphics.white);
		graphics.printCentred("Select the difficulty:", window.width()/2, window.height()/2 + 50, 1, 100);
		graphics.rectangle(false, EASY_BUTTON_X, EASY_BUTTON_Y, EASY_BUTTON_WIDTH, EASY_BUTTON_HEIGHT);
		graphics.rectangle(false, MEDIUM_BUTTON_X, MEDIUM_BUTTON_Y, MEDIUM_BUTTON_WIDTH, MEDIUM_BUTTON_HEIGHT);
		graphics.rectangle(false, HARD_BUTTON_X, HARD_BUTTON_Y, HARD_BUTTON_WIDTH, HARD_BUTTON_HEIGHT);
		for (lib.ButtonText button : buttons) {
			button.draw();
		}
		text_box.draw();
	}

	@Override
	public void close() { }

	@Override
	public void playSound(Sound sound) {	
		
	}

}
