package scn;

import java.awt.Rectangle;
import java.io.File;
import java.util.List;

import lib.RandomNumber;
import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import cls.Aircraft;
import cls.Airport;
import cls.AirportControlBox;
import cls.GameWindow;
import cls.Vector;
import cls.Waypoint;
import btc.Main;

public class Demo extends Scene {
	/**Music to play during the game scene*/
	private audio.Music music;
	
	private final cls.GameWindow game;
	
	
	//****** REBUILD PROGRESS MARKER ******
	

	/** Tracks if manual heading compass of a manually controlled aircraft has been clicked*/
	private boolean compass_clicked;
	/** Tracks if waypoint of a manually controlled aircraft has been clicked*/
	private boolean waypoint_clicked;



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
		game = new GameWindow(16, 8, 
				window.width() -32, window.height() -16, difficulty);
		game.setControllable(true);
	}

	
	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty
	 */
	@Override
	public void start() {	
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		music.play();

		compass_clicked = false;
	}


	/** cleanly exit by stopping the scene's music */
	@Override
	public void close() {
		music.stop();
	}
	
	
	/**
	 * Update game state and handle any game over state
	 * {!}Generate a new flight if flight generation interval has been exceeded.
	 */
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
		airport_control_box.mousePressed(key, x, y);
		altimeter.mousePressed(key, x, y);
		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(x, y)) {
				Aircraft clickedAircraft = findClickedAircraft(x, y);
				deselectAircraft();
				selected_aircraft = clickedAircraft;
				altimeter.show(selected_aircraft);
				
			} else if (waypointInFlightplanClicked(x, y, selected_aircraft) && !selected_aircraft.isManuallyControlled()) {
				clicked_waypoint = findClickedWaypoint(x, y);
				if (clicked_waypoint != null) {
					waypoint_clicked = true; // Flag to mouseReleased
					selected_path_point = selected_aircraft.getFlightPlan().indexOfWaypoint(clicked_waypoint);					
				}
			}
			
			if (isArrivalsClicked(x, y) && selected_aircraft != null) {
				if (selected_aircraft.is_waiting_to_land && selected_aircraft.current_target.equals(airport.getLocation())) {
					airport.mousePressed(key, x, y);
					selected_aircraft.land();
					deselectAircraft();
				}
			} else if (isDeparturesClicked(x, y)) {
				if (airport.aircraft_hangar.size() > 0) {
					airport.mousePressed(key, x, y);
					airport.signalTakeOff();
				}
			}
		} else if (key == input.MOUSE_RIGHT) {
			if (aircraftClicked(x, y)) {
				selected_aircraft = findClickedAircraft(x, y);
			}
			if (selected_aircraft != null) {
				if (compassClicked()) {
					compass_clicked = true; // Flag to mouseReleased
					if (!selected_aircraft.isManuallyControlled())
						toggleManualControl();
				} else {
					if (selected_aircraft.isManuallyControlled()) {
						toggleManualControl();
					} else {
						deselectAircraft();					
					}
				}
			}
		}
	}
	

	@Override
	public void mouseReleased(int key, int x, int y) {
		airport.mouseReleased(key, x, y);
		airport_control_box.mouseReleased(key, x, y);
		altimeter.mouseReleased(key, x, y);
		
		switch (key){
		case input.MOUSE_LEFT: 
			if (manualOverridePressed(x, y)) {
				manual_override_button.act();
			} else if (waypoint_clicked && selected_aircraft != null) {
				Waypoint newWaypoint = findClickedWaypoint(x, y);
				if (newWaypoint != null) {
					selected_aircraft.alterPath(selected_path_point, newWaypoint);
					orders_box.addOrder(">>> " + selected_aircraft.getName() + " please alter your course.");
					orders_box.addOrder("<<< Roger that. Altering course now.");
				}
				selected_path_point = -1;
			}
			clicked_waypoint = null; // Fine to set to null now as will have been dealt with
			break;
		case input.MOUSE_RIGHT:
			if (compass_clicked && selected_aircraft != null) {
				double dx = input.mouseX() - selected_aircraft.getPosition().getX() + airspace_view_offset_x;
				double dy = input.mouseY() - selected_aircraft.getPosition().getY() + airspace_view_offset_y;
				double newBearing = Math.atan2(dy, dx);
				selected_aircraft.setBearing(newBearing);
			}
			break;
		}
	}

	@Override
	public void keyPressed(int key) {
		game.keyPressed(key);
	}

	@Override
	/** handle keyboard input */
	public void keyReleased(int key) {
		switch (key) {		
			case input.KEY_SPACE :
				toggleManualControl();
			break;
			
			case input.KEY_LCRTL :	//{!} for debug only
				generateFlight();
			break;
			
			case input.KEY_ESCAPE :
				aircraft_in_airspace.clear();
				airport.clear();
				main.closeScene();
			break;
			
			case input.KEY_F5 :	//{!} for debug only
				Aircraft a1 = createAircraft();
				Aircraft a2 = createAircraft();
				gameOver(a1, a2);
			break;
		}
	}
	
	// Necessary for testing	
	/**
	 * This method should only be used for unit testing (avoiding instantiation of main class). Its purpose is to initialize array where
	 * aircraft are stored. 
	 */	
	@Deprecated
	public void initializeAircraftArray() {
		aircraft_in_airspace = new java.util.ArrayList<Aircraft>();
	}
		 
	/**
	 * This constructor should only be used for unit testing. Its purpose is to allow an instance
	 * of demo class to be created without an instance of Main class (effectively launching the game)
	 * @param difficulty
	 */	
	@Deprecated
	public Demo(int difficulty) {
		game = new GameWindow(16,48, window.width() - 32 -1, window.height() - 176 -1, difficulty);
	}
}