package scn;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.window;
import btc.Main;

public class Title extends Scene {
	private final static String HELP_FILE_PATH = System.getProperty("user.dir") + "/user_manual.pdf";
	
	/**
	 * The 'beep' played as the radar makes a sweep
	 */
	private audio.Sound beep;
	
	/**
	 * A List of buttons, to hold declared buttons in the scene
	 */
	private lib.ButtonText[] buttons;
	
	/**
	 * Holds the angle to draw the radar sweep at.
	 * Also used to play the beep sound as the radar sweeps the BTC title string
	 * Updated regularly during Title's update()
	 */
	private double angle;
	
	/**
	 * Background image for menu
	 */
	private static graphics.Image backgroundImage;
	
	/**
	 * Constructor for the Title Scene
	 * @param main the main holding the scene
	 */
	public Title(Main main) {
		super(main);
	}

	/**
	 * Initialises all objects, such as buttons and sound effects
	 * Only runs at start of scene
	 */
	@Override
	public void start() {
		beep = audio.newSoundEffect("sfx" + File.separator + "beep.ogg");
		beep.setVolume(0.2f);
		angle = 0;
		
		backgroundImage = graphics.newImage("gfx" +File.separator + "mainBackground.png");
		
		
		buttons = new lib.ButtonText[4];
		{// Demo Button
			lib.ButtonText.Action demo = new lib.ButtonText.Action() {
				@Override
				public void action() {
					main.setScene(new DifficultySelect(main, DifficultySelect.CREATE_DEMO));
				}
			};
			buttons[0] = new lib.ButtonText("Play Demo", demo, 
					window.height(), window.height()/2 + 96,
					window.width() - window.height(), 24,
					8, 6);
		}
	
		{// Credits Button
			lib.ButtonText.Action credits = new lib.ButtonText.Action() {
				@Override
				public void action() {
					main.setScene(new Credits(main));
				}
			};
			buttons[1] = new lib.ButtonText("Credits", credits,
					window.height(), window.height()/2 + 126, 
					window.width() - window.height(), 24,
					8, 6);
		}
		
		{// Help Button
			lib.ButtonText.Action help = new lib.ButtonText.Action() {
				@Override
				public void action() {
					try {
						Desktop.getDesktop().open(new File(HELP_FILE_PATH));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			buttons[2] = new lib.ButtonText("Help        (Opens user manual PDF)", help,
					window.height(), window.height()/2 + 156,
					window.width() - window.height(), 24, 
					8, 6);
		}
		
		{// Exit Button
			lib.ButtonText.Action exit = new lib.ButtonText.Action() {
				@Override
				public void action() {
					main.quit();
				}
			};
			buttons[3] = new lib.ButtonText("Exit", exit,
					window.height(), window.height()/2 + 186,
					window.width() - window.height(), 24,
					8, 6);
		}
	}

	
	/**
	 * Updates all objects in the title scene
	 * Called by Main class
	 * @param time_difference the time since the last update
	 */
	@Override
	public void update(double time_difference) {
		angle += time_difference; // Increase the angle of the radar sweep
		
		// Check the angle of the radar sweep;
		// If approaching the BTC title string, play the beep
		double beepTimer = (angle * 4) + (Math.PI * 4 / 5); 
		beepTimer %= (2 * Math.PI);
		if (beepTimer <= 0.1) {
			playSound(beep);
		}
	}
	
	@Override
	public void mousePressed(int key, int x, int y) { }

	@Override
	public void mouseReleased(int key, int mx, int my) {
		for (lib.ButtonText button : buttons) {
			if (button.isMouseOver(mx, my)) {
				button.act();
			}
		}	
	}

	@Override
	public void keyPressed(int key) { }

	@Override
	public void keyReleased(int key) { }

	/**
	 * Handles drawing of the scene
	 * Calls drawRadar() and drawMenu() to draw elements of the scene
	 * Called by Main
	 */
	@Override
	public void draw() {
		
		graphics.draw(backgroundImage, 0, 0, window.scale());
		//drawRadar();
		drawMenu();
	}
	
	/**
	 * Draws the radar arc and title string
	 */
	private void drawRadar() {
		// Radar
		// Set of circles for radar 'screen'
		graphics.setColour(graphics.white);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/2 - 32, 100);
		graphics.setColour(0, 128, 0, 32);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/3, 100);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/4 - 16, 100);
		graphics.circle(false, window.height()/2, window.height()/2, window.height()/9, 100);
		graphics.circle(false, window.height()/2, window.height()/2, 2, 100);
		graphics.setColour(graphics.white);
		// Sweep of radar
		double radarAngle = (angle * 4) % (2 * Math.PI);
		int w = (int)( Math.cos(radarAngle) * (window.height()/2 - 32) );
		int h = (int)( Math.sin(radarAngle) * (window.height()/2 - 32) );
		graphics.line(window.height()/2, window.height()/2, window.height()/2 + w, window.height()/2 + h);
		graphics.setColour(0, 128, 0, 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -8 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -7 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -6 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -5 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -4 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -3 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -2 * Math.PI / 8);
		graphics.arc(true, window.height()/2, window.height()/2, window.height()/2 - 32, radarAngle, -1 * Math.PI / 8);
		// Title
		String title = "Bear Traffic Controller";
		// Fades title string's characters over time
		// Characters brighten when the sweep passes over them
		double a = radarAngle + (Math.PI * 4 / 5);
		for (int i = 0; i < title.length(); i++) {
			a -= Math.PI / 32;
			double opacity = a %= (2 * Math.PI);
			opacity *= 256 / (2 * Math.PI);
			opacity = 256 - opacity;
			opacity %= 256;
			graphics.setColour(0, 128, 0, opacity);
			graphics.print(title.substring(i, i+1), 74*4.5 + i * 14, 344, 1.8);
		}
		// Subtitle
		String subtitle = "PSA Edition";
		a = radarAngle + (Math.PI * 4 / 5);
		for (int i = 0; i < subtitle.length(); i++) {
			a -= Math.PI / 32;
			double opacity = a %= (2 * Math.PI);
			opacity *= 256 / (2 * Math.PI);
			opacity = 256 - opacity;
			opacity %= 256;
			graphics.setColour(0, 128, 0, opacity);
			graphics.print(subtitle.substring(i, i+1), 94*4.5 + i * 14, 364, 1.8);
		}
	}
	
	/**
	 * Draws menu boxes, boxes around buttons, and strings
	 */
	private void drawMenu() {
		// Draw Extras e.g. Date, Time, Credits
		graphics.setColour(graphics.white);
		graphics.line(window.height(), 16, window.height(), window.height() - 16);
		java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");
		java.text.DateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
		java.util.Date date = new java.util.Date();
		graphics.print(dateFormat.format(date), window.height() + 8, 20);
		graphics.print(timeFormat.format(date), window.height() + 8, 36);
		graphics.line(window.height(), 48, window.width() - 16, 48);
		graphics.print("Created by: Team FLR", window.height() + 8, 56);
		graphics.print("Improved by: Team MQV", window.height() + 8, 68);
		graphics.print("Perfected by: Team PSA", window.height() + 8, 80);
		
		// Draw Buttons
		for (lib.ButtonText button : buttons) 
			button.draw();
		graphics.setColour(graphics.white);
		graphics.line(window.height(), window.height()/2 + 90, window.width() - 16, window.height()/2 + 90);
		graphics.line(window.height(), window.height()/2 + 120, window.width() - 16, window.height()/2 + 120);
		graphics.line(window.height(), window.height()/2 + 150, window.width() - 16, window.height()/2 + 150);
		graphics.line(window.height(), window.height()/2 + 180, window.width() - 16, window.height()/2 + 180);
		graphics.line(window.height(), window.height()/2 + 210, window.width() - 16, window.height()/2 + 210);
	}
	
	@Override
	public void close() {

	}
	
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
	}
}