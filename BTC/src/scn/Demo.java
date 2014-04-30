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


	/** The time elapsed since the last flight was generated*/
	private double time_since_flight_generation = 0;

	
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


	/**
	 * Creates a new aircraft object and introduces it to the airspace.
	 * Also doesn't spawn a plane that is to close to another so there is 
	 * not an instant crash when spawning 
	 */
	private void generateFlight() {
				
		Aircraft aircraft = createAircraft();
		boolean isTooClose = false;
		
		if (aircraft != null) {
			// go through all the aircrafts currently in the airspace
			for (Aircraft a : aircraft_in_airspace) {
				// check the distance from the current selected aircraft and the aircraft waiting to be spawned
				int distanceX = (int)(Math.abs(Math.round(a.getPosition().getX()) - Math.round(aircraft.getFlightPlan()
						.getRoute()[0]
						.getLocation()
						.getX())));
				
				int distanceY = (int)(Math.abs(Math.round(a.getPosition().getY()) - Math.round(aircraft.getFlightPlan()
						.getRoute()[0]
						.getLocation()
						.getY())));
				
				int distanceFromEachPlane = (int)Math.sqrt((int)Math.pow(distanceX, 2) + (int) Math.pow(distanceY, 2));
				
				// if the distance is less than certain amount then aircraft is too close to be spawned
				if (distanceFromEachPlane <= 100) {
					isTooClose = true;
				}				
			}			
			
			if (!isTooClose) { // Continue only if aricraft is not too close
				if (aircraft.getFlightPlan().getOriginName().equals(airport.name)) {
					orders_box.addOrder("<<< " + aircraft.getName() 
							+ " is awaiting take off from " 
							+ aircraft.getFlightPlan().getOriginName()
							+ " heading towards " + aircraft.getFlightPlan().getDestinationName() + ".");
					airport.addToHangar(aircraft);
				} else {
					orders_box.addOrder("<<< " + aircraft.getName() 
							+ " incoming from " + aircraft.getFlightPlan().getOriginName() 
							+ " heading towards " + aircraft.getFlightPlan().getDestinationName() + ".");
					aircraft_in_airspace.add(aircraft);
				}
			}
			
		}
	}
	
	/**
	 * Handle nitty gritty of aircraft creating
	 * including randomisation of entry, exit, altitude, etc.
	 * @return the created aircraft object
	 */
	private Aircraft createAircraft() {
		// Origin and Destination
		String destination_name;
		String origin_name = "";
		Waypoint origin_point;
		Waypoint destination_point;
	
		// Chooses two waypoints randomly and then checks if they satisfy the rules, if not, it tries until it finds good ones. 	
		java.util.ArrayList<Waypoint> available_origins = getAvailableEntryPoints();
		
		if (available_origins.isEmpty()) {
			if (airport.aircraft_hangar.size() == airport.getHangarSize()) {
				return null;
			} else {
				origin_point = airport;
				origin_name = airport.name;
			}
		} else {
			origin_point = available_origins.get(RandomNumber.randInclusiveInt(0, available_origins.size()-1));
			for (int i = 0; i < location_waypoints.length; i++) {
				if (location_waypoints[i].equals(origin_point)) {
					origin_name = location_waypoints[i].getName();
					break;
				}
			}
		}
		
		// Work out destination
		int destination = RandomNumber.randInclusiveInt(0, location_waypoints.length - 1);
		destination_name = location_waypoints[destination].getName();
		destination_point = location_waypoints[destination];
		
		while (location_waypoints[destination].getName() == origin_name) {
			destination = RandomNumber.randInclusiveInt(0, location_waypoints.length - 1);
			destination_name = location_waypoints[destination].getName();
			destination_point = location_waypoints[destination];
		}			
		
		// Name
		String name = "";
		boolean name_is_taken = true;
		while (name_is_taken) {
			name = "Flight " + (int)(900 * Math.random() + 100);
			name_is_taken = false;
			for (Aircraft a : aircraft_in_airspace) {
				if (a.getName() == name) name_is_taken = true;
			}
		}
		return new Aircraft(name, destination_name, origin_name, destination_point, origin_point, aircraft_image, 32 + (int)(10 * Math.random()), airspace_waypoints, difficulty);
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
		
		//****** REBUILD PROGRESS MARKER ******
		

		if (selected_aircraft != null) {
			if(!selected_aircraft.is_takeoff()){
				if (selected_aircraft.isManuallyControlled()) {
					if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A})) {
						selected_aircraft.turnLeft(time_difference);
					} else if (input.keyPressed(new int[]{input.KEY_RIGHT, input.KEY_D})) {
						selected_aircraft.turnRight(time_difference);
					}
				} else if (input.keyPressed(new int[]{input.KEY_LEFT, input.KEY_A, input.KEY_RIGHT, input.KEY_D})) {
					toggleManualControl();
				}
				
				if (input.keyPressed(new int[]{input.KEY_S, input.KEY_DOWN})) {
					selected_aircraft.setAltitudeState(Aircraft.ALTITUDE_FALL);
				} else if (input.keyPressed(new int[]{input.KEY_W, input.KEY_UP})) {
					selected_aircraft.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
				}
			}	
			if (selected_aircraft.isOutOfAirspaceBounds()) {
				orders_box.addOrder(">>> " + selected_aircraft.getName() + " out of bounds, returning to route");
				deselectAircraft();
			}	
		}
		
		time_since_flight_generation += time_difference;
		if(time_since_flight_generation >= getFlightGenerationInterval()) {
			time_since_flight_generation -= getFlightGenerationInterval();
			if (aircraft_in_airspace.size() < getMaxAircraft()) {
				generateFlight();
			}
		}
		if (aircraft_in_airspace.size() == 0)
			generateFlight();
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
		main.setScene(new GameOver(main, plane1, plane2, score.getTotalScore()));
	}
	
	
	/** draw the game window */
	@Override
	public void draw() {
		game.draw();
	}


	private boolean waypointInFlightplanClicked(int x, int y, Aircraft a) {
		if (a != null) {
			for (Waypoint w : airspace_waypoints) {
				if (w.isMouseOver(x-airspace_view_offset_x, y-airspace_view_offset_y) && a.getFlightPlan().indexOfWaypoint(w) > -1) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Waypoint findClickedWaypoint(int x, int y) {
		for (Waypoint w : airspace_waypoints) {
			if (w.isMouseOver(x-airspace_view_offset_x, y-airspace_view_offset_y)) {
				return w;
			}
		}
		return null;
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
	
	private boolean manualOverridePressed(int x, int y) {
		return manual_override_button.isMouseOver(x - airspace_view_offset_x, y - airspace_view_offset_y);
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