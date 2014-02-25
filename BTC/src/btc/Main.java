package btc;

import java.io.File;

import org.lwjgl.Sys;

import lib.jog.*;

/**
 * <h1>Main</h1>
 * <p>Main class that is run when game is run. 
 * Handles the scenes (gamestates).</p>
 * @author Huw Taylor
 */
public class Main implements input.EventHandler {

	/**
	 * Creates a new instance of Main, starting a new game.
	 * @param args any command-line arguments.
	 */
	public static void main(String[] args) {
		new Main();
	}
	
	final private String TITLE = "Bear Traffic Controller: MQV Edition";
	final private int WIDTH = 1280;
	final private int HEIGHT = 960;
	final private String[] ICON_FILENAMES = {
		"gfx" + File.separator + "icon16.png",
		"gfx" + File.separator + "icon32.png",
		"gfx" + File.separator + "icon64.png",
	};

	private double last_frame_time;
	private double time_difference;
	private java.util.Stack<scn.Scene> scene_stack;
	private scn.Scene current_scene;
	private int fps_counter;
	private long last_fps_time;
	
	/**
	 * Constructor for Main. Initialises the jog library classes, and then
	 * begins the game loop, calculating time between frames, and then when
	 * the window is closed it releases resources and closes the program
	 */
	public Main() {
		start();
		while(!window.isClosed()) {
			time_difference = getTimeSinceLastFrame();
			update(time_difference);
			draw();
		}
		quit();
	}
	
	/**
	 * Creates window, initialises jog classes and sets starting values to variables.
	 */
	private void start() {
		window.initialise(TITLE, WIDTH, HEIGHT);
		window.setIcon(ICON_FILENAMES);
		graphics.initialise();
		graphics.Font font = graphics.newBitmapFont("gfx" + File.separator + "font.png", "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz1234567890.,_-!?()[]><#~:;/\\^'\"{}£$@@@@@@@@");
		graphics.setFont(font);
		
		scene_stack = new java.util.Stack<scn.Scene>();
		setScene(new scn.Title(this));
		
		last_frame_time = (double)(Sys.getTime()) / Sys.getTimerResolution();
		last_fps_time = Sys.getTime()* 1000 / Sys.getTimerResolution(); // Set to current Time
	}
	
	/**
	 * Updates audio, input handling, the window, the current scene and FPS.
	 * @param time_difference the time elapsed since the last frame.
	 */
	private void update(double time_difference) {
		audio.update();
		input.update(this);
		window.update();
		current_scene.update(time_difference);
		updateFPS();
	}
	
	/**
	 * Calculates the time since the last frame in seconds as a double-precision floating point number.
	 * @return the time in seconds since the last frame.
	 */
	private double getTimeSinceLastFrame() {
		double current_time = (double)(Sys.getTime()) / Sys.getTimerResolution();
	    double delta = current_time - last_frame_time;
	    last_frame_time = current_time; // Update last frame time
	    return delta;
	}
	
	/**
	 * Clears the graphical viewport and calls the draw function of the current scene.
	 */
	private void draw() {
		graphics.clear();
		current_scene.draw();
	}
	
	/**
	 * Closes the current scene, closes the window, releases the audio resources and quits the process.
	 */
	public void quit() {
		current_scene.close();
		window.dispose();
		audio.dispose();
		System.exit(0);
	}
	
	/**
	 * Closes the current scene, adds new scene to scene stack and starts it
	 * @param new_scene The scene to set as current scene
	 */
	public void setScene(scn.Scene new_scene) {
		if (current_scene != null) 
			current_scene.close();
		current_scene = scene_stack.push(new_scene); // Add new scene to scene stack and set to current scene
		current_scene.start();
	}
	
	/**
	 * Closes the current scene, pops it from the stack and sets current scene to top of stack
	 */
	public void closeScene() {
		current_scene.close();
		scene_stack.pop();
		current_scene = scene_stack.peek();
	}
	
	/** 
	 * Updates the FPS - increments the FPS counter. 
	 * If it has been over a second since the FPS was updated, update it
	 */
	public void updateFPS() {
		long current_time = ((Sys.getTime()* 1000) / Sys.getTimerResolution());
		if (current_time - last_fps_time > 1000) { // Update once per second
			window.setTitle(TITLE + " - FPS: " + fps_counter);
			fps_counter = 0; // Reset the FPS counter
			last_fps_time += current_time - last_fps_time; // Add on the time difference
		}
		fps_counter++;
	}
	

	@Override
	public void mousePressed(int key, int x, int y) {
		current_scene.mousePressed(key, x, y);
	}

	@Override
	public void mouseReleased(int key, int x, int y) {
		current_scene.mouseReleased(key, x, y);
	}

	@Override
	public void keyPressed(int key) {
		current_scene.keyPressed(key);
	}

	@Override
	public void keyReleased(int key) {
		current_scene.keyReleased(key);
	}
}