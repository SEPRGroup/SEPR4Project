package scn;

import java.awt.Rectangle;
import java.io.File;

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
	
	private final cls.GameWindow game;

	//****** REBUILD PROGRESS MARKER ******
	
	private cls.Score score; 	
	private boolean shown_aircraft_waiting_message = false;
	private cls.OrdersBox orders_box;
	private static double time_elapsed;
	private Aircraft selected_aircraft;
	private Waypoint clicked_waypoint;
	private int selected_path_point; // Selected path point, in an aircraft's route, used for altering the route
	public static java.util.ArrayList<Aircraft> aircraft_in_airspace;
	public java.util.ArrayList<Aircraft> recently_departed_aircraft;
	
	
	/**
	 * A button to start and end manual control of an aircraft
	 */
	private lib.ButtonText manual_override_button;
	/**
	 * Tracks if manual heading compass of a manually controlled aircraft has been clicked
	 */
	private boolean compass_clicked;
	/**
	 * Tracks if waypoint of a manually controlled aircraft has been clicked
	 */
	private boolean waypoint_clicked;
	/**
	 * An altimeter to display aircraft altitidue, heading, etc.
	 */
	private cls.Altimeter altimeter;
	private cls.AirportControlBox airport_control_box;

	/**
	 * The time elapsed since the last flight was generated
	 */
	private double time_since_flight_generation = 0;
	
	/**
	 * The current control altitude of the ACTO - initially 30,000
	 */
	private int highlighted_altitude = 30000;
	
	/**Music to play during the game scene*/
	private audio.Music music;

	
	/**
	 * Demo's instance of the airport class
	 */
	public static Airport airport = new Airport("Mosbear Airport");
	
	/**
	 * The set of waypoints in the airspace which are origins / destinations
	 */
	public static Waypoint[] location_waypoints = new Waypoint[] {
		// A set of Waypoints which are origin / destination points 
		new Waypoint(8, 8, true, "North West Top Leftonia"), // top left
		new Waypoint(8, window.height() - ORDERS_BOX.height - 72, true, "100 Acre Woods"), // bottom left
		new Waypoint(window.width() - 40, 8, true, "City of Rightson"), // top right
		new Waypoint(window.width() - 40, window.height() - ORDERS_BOX.height - 72, true, "South Sea"), // bottom right
		airport
	};

	/**
	 * All waypoints in the airspace, INCLUDING locationWaypoints.
	 */
	public static Waypoint[] airspace_waypoints = new Waypoint[] {		
		/* All waypoints in the airspace, including location Way Points*/
	
		// Airspace waypoints
		new Waypoint(0.10*window.width(), 0.07*window.height(), false),
		new Waypoint(0.55*window.width(), 0.1*window.height(), false),
		new Waypoint(0.81*window.width(), 0.08*window.height(), false),
		new Waypoint(0.39*window.width(), 0.21*window.height(), false),
		new Waypoint(0.82*window.width(), 0.42*window.height(), false),
		new Waypoint(0.20*window.width(), 0.42*window.height(), false),
		new Waypoint(0.16*window.width(), 0.66*window.height(), false),
		new Waypoint(0.39*window.width(), 0.68*window.height(), false),
		new Waypoint(0.63*window.width(), 0.78*window.height(), false),
		new Waypoint(0.78*window.width(), 0.78*window.height(), false),

		// Destination/origin waypoints - present in this list for pathfinding.
		location_waypoints[0],
		location_waypoints[1],
		location_waypoints[2],
		location_waypoints[3],
		location_waypoints[4]
	};
	
	/**
	 * This method provides maximum number of planes using value of multiplier
	 * @return maximum number of planes
	 */
	private int getMaxAircraft() {
		if (score.getMultiplier() == 1) 
			return 3;
		else if (score.getMultiplier() == 3) 
			return 5;
		else
			return score.getMultiplier();
	}
	
	/**
	 * The interval in seconds to generate flights after
	 */
	private int getFlightGenerationInterval() {
		if (difficulty == 1)
			return (30 / (getMaxAircraft() * 2)); // Planes move 2x faster on medium so this makes them spawn 2 times as often to keep the ratio
		if (difficulty == 2)
			return (30 / (getMaxAircraft() * 3) ); // Planes move 3x faster on hard so this makes them spawn 3 times as often to keep the ratio 
		return (30 / getMaxAircraft());
	}
	
	/**
	 * Getter for aircraft list
	 * @return the arrayList of aircraft in the airspace
	 */
	public java.util.ArrayList<Aircraft> getAircraftList() {
		return aircraft_in_airspace;
	}

	public static double getTime() {
		return time_elapsed;
	}
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane (no plane is currently going to exit the airspace there,
	 * also it is not too close to any plane). 
	 */	
	private java.util.ArrayList<Waypoint> getAvailableEntryPoints() {
		java.util.ArrayList<Waypoint> available_entry_points = new java.util.ArrayList<Waypoint>();
		
		for (Waypoint entry_point : location_waypoints) {
			
			boolean is_available = true;
			/**
			 * prevents spawning a plane in waypoint both:
			 * if any plane is currently going towards it 
			 * if any plane is less than 250 from it
			 */
			
			for (Aircraft aircraft : aircraft_in_airspace) {
				// Check if any plane is currently going towards the exit point/chosen originPoint
				// Check if any plane is less than what is defined as too close from the chosen originPoint
				if (aircraft.current_target.equals(entry_point.getLocation()) || aircraft.isCloseToEntry(entry_point.getLocation())) {
					is_available = false;
				}	
			}
			
			if (is_available) {
				available_entry_points.add(entry_point);
			}	
		}
		return available_entry_points;
	}
	
	/**
	 * Constructor
	 * @param main the main containing the scene
	 * @param difficulty the difficulty the scene is to be initialised with
	 */
	public Demo(Main main, int difficulty) {
		super(main);
		airport.loadImage();
		game = new GameWindow(16,48, window.width() - 32 -1, window.height() - 176 -1, difficulty);
	}

	/**
	 * Initialise and begin music, init background image and scene variables.
	 * Shorten flight generation timer according to difficulty
	 */
	@Override
	public void start() {	
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
		music.play();
		orders_box = new cls.OrdersBox(ORDERS_BOX.x, ORDERS_BOX.y, ORDERS_BOX.width, ORDERS_BOX.height, 6);
		aircraft_in_airspace = new java.util.ArrayList<Aircraft>();
		recently_departed_aircraft = new java.util.ArrayList<Aircraft>();
		lib.ButtonText.Action manual = new lib.ButtonText.Action() {
			@Override
			public void action() {
				// _selectedAircraft.manuallyControl();
				toggleManualControl();
			}
		};
		
		score = new cls.Score();
		
		manual_override_button = new lib.ButtonText("Take Control", manual, (window.width() - 128) / 2, 32, 128, 64, 8, 4);
		time_elapsed = 0;
		compass_clicked = false;
		selected_aircraft = null;
		clicked_waypoint = null;
		selected_path_point = -1;
		
		manual_override_button = new lib.ButtonText(" Take Control", manual, (window.width() - 128) / 2, 32, 128, 64, 8, 4);
		altimeter = new cls.Altimeter(ALTIMETER.x, ALTIMETER.y, ALTIMETER.width, ALTIMETER.height, orders_box);
		airport_control_box = new AirportControlBox(AIRPORT_CONTROL.x, AIRPORT_CONTROL.y, AIRPORT_CONTROL.width, AIRPORT_CONTROL.height, airport);
		deselectAircraft();
	}
	
	/**
	 * Causes a selected aircraft to call methods to toggle manual control
	 */
	private void toggleManualControl() {
		if (selected_aircraft != null) {
			selected_aircraft.toggleManualControl();
			manual_override_button.setText( (selected_aircraft.isManuallyControlled() ? "Remove" : " Take") + " Control");
		}
	}
	
	/**
	 * Causes an aircraft to call methods to handle deselection
	 */
	private void deselectAircraft() {
		if (selected_aircraft != null && selected_aircraft.isManuallyControlled()) {
			selected_aircraft.toggleManualControl();
			manual_override_button.setText(" Take Control");
		}
		selected_aircraft = null;
		clicked_waypoint = null; 
		selected_path_point = -1;
		altimeter.hide();
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
	
	/**
	 * Sets the airport to busy, adds the aircraft passed to the airspace, where it begins its flight plan starting at the airport
	 * @param aircraft
	 */
	public static void takeOffSequence(Aircraft aircraft) {
		aircraft_in_airspace.add(aircraft);
		// Space to implement some animation features?
	}

	/**
	 * cleanly exit by stopping the scene's music
	 */
	@Override
	public void close() {
		music.stop();
	}
	
	/**
	 * Update all objects within the scene, ie aircraft, orders box altimeter.
	 * Cause collision detection to occur
	 * Generate a new flight if flight generation interval has been exceeded.
	 */
	@Override
	public void update(double time_difference) {
		time_elapsed += time_difference;
		score.update();
		graphics.setColour(graphics.green_transp);
		if (airport.getLongestTimeInHangar(time_elapsed) > 5) {
			score.increaseMeterFill(-1);
			if (!shown_aircraft_waiting_message) {
				orders_box.addOrder(">>> Plane waiting to take off, multiplier decreasing");
				shown_aircraft_waiting_message = true;
			}
		} else {
			shown_aircraft_waiting_message = false;
		}
		
		orders_box.update(time_difference);
		for (Aircraft aircraft : aircraft_in_airspace) {
			aircraft.update(time_difference);
			if (aircraft.isFinished()) {
				aircraft.setAdditionToMultiplier(score.getMultiplierLevel());
				score.increaseMeterFill(aircraft.getAdditionToMultiplier());
				aircraft.setScore(score.calculateAircraftScore(aircraft));
				score.increaseTotalScore(score.getMultiplier() * aircraft.getScore());
				aircraft.setDepartureTime(System.currentTimeMillis());
				recently_departed_aircraft.add(aircraft);
		
				if (aircraft.getAdditionToMultiplier() < 0)
					orders_box.addOrder("<<< The plane has breached separation rules on its path, your multiplier may be reduced ");
				
				switch (RandomNumber.randInclusiveInt(0, 2)){
				case 0:
					orders_box.addOrder("<<< Thank you Comrade");
					break;
				case 1:
					orders_box.addOrder("<<< Well done Comrade");
					break;
				case 2:
					orders_box.addOrder("<<< Many thanks Comrade");
					break;
				}
			}
		}
		checkCollisions(time_difference);
		for (int i = aircraft_in_airspace.size()-1; i >=0; i --) {
			if (aircraft_in_airspace.get(i).isFinished()) {
				if (aircraft_in_airspace.get(i) == selected_aircraft) {
					deselectAircraft();
				}
				aircraft_in_airspace.remove(i);
			}
		}
		airport.update(this);
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
	
	/**
	 * Cause all planes in airspace to update collisions
	 * Catch and handle a resultant game over state
	 * @param time_difference delta time since last collision check
	 */
	private void checkCollisions(double time_difference) {
		for (Aircraft plane : aircraft_in_airspace) {
			int collision_state = plane.updateCollisions(time_difference, getAircraftList(), score);
			if (collision_state >= 0) {
				gameOver(plane, getAircraftList().get(collision_state));
				return;
			}
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
		aircraft_in_airspace.clear();
		airport.clear();
		playSound(audio.newSoundEffect("sfx" + File.separator + "crash.ogg"));
		main.closeScene();
		main.setScene(new GameOver(main, plane1, plane2, score.getTotalScore()));
	}
	
	/**
	 * Draw the scene GUI and all drawables within it, e.g. aircraft and waypoints
	 */
	@Override
	public void draw() {
		graphics.setColour(graphics.green);
		graphics.rectangle(false, airspace_view_offset_x, airspace_view_offset_y, window.width() - 32, window.height() - 176);

		//System.out.println("set Demo");
		graphics.setViewport(airspace_view_offset_x, airspace_view_offset_y, window.width() - 32 -1, window.height() - 176 -1);
		
		graphics.setColour(255, 255, 255, 96);
		graphics.draw(background, 0, 0, window.scale());	//{!} NOT accounting for fixed border size
		graphics.setColour(255, 255, 255, 96);
		airport.draw();
		drawMap();
		
		//System.out.println("restore Demo");
		graphics.setViewport();
		
		
		if (selected_aircraft != null && selected_aircraft.isManuallyControlled() && !selected_aircraft.is_takeoff()) {
			selected_aircraft.drawCompass();
		}
		
		score.draw();
		orders_box.draw();
		altimeter.draw();
		airport_control_box.draw();
		drawPlaneInfo();
		
		graphics.setColour(graphics.green);
		drawAdditional();
		drawPlaneScoreLabels();
	}
	
	/**
	 * Draw waypoints, and route of a selected aircraft between waypoints
	 * Print waypoint names next to waypoints
	 */
	private void drawMap() {
		for (Waypoint waypoint : airspace_waypoints) {
			if (!waypoint.equals(airport)) { // Skip the airport
				waypoint.draw();
			}
		}
		graphics.setColour(255, 255, 255);
		for (Aircraft aircraft : aircraft_in_airspace) {
			aircraft.draw(highlighted_altitude);
			if (aircraft.isMouseOver()) {
				aircraft.drawFlightPath(false);
			}
		}
		
		if (selected_aircraft != null) {
			// Flight Path
			selected_aircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
			// Override Button
			graphics.setColour(graphics.black);
			graphics.rectangle(true, (window.width() - 128) / 2, 16, 128, 32);
			graphics.setColour(graphics.green);
			graphics.rectangle(false, (window.width() - 128) / 2, 16, 128, 32);
			manual_override_button.draw();
			
			selected_aircraft.drawFlightPath(true);
			graphics.setColour(graphics.green);
			
		}
		
		if (clicked_waypoint != null && selected_aircraft.isManuallyControlled() == false) {
			selected_aircraft.drawModifiedPath(selected_path_point, input.mouseX() - airspace_view_offset_x, input.mouseY() - airspace_view_offset_y);
		}
		
		//draw transfer waypoint labels
		graphics.setColour(graphics.green);
		graphics.print(location_waypoints[0].getName(), location_waypoints[0].getLocation().getX() + 9, location_waypoints[0].getLocation().getY() - 6);
		graphics.print(location_waypoints[1].getName(), location_waypoints[1].getLocation().getX() + 9, location_waypoints[1].getLocation().getY() - 6);
		graphics.print(location_waypoints[2].getName(), location_waypoints[2].getLocation().getX() - 141, location_waypoints[2].getLocation().getY() - 6);
		graphics.print(location_waypoints[3].getName(), location_waypoints[3].getLocation().getX() - 91, location_waypoints[3].getLocation().getY() - 6);
		graphics.print(location_waypoints[4].getName(), location_waypoints[4].getLocation().getX() - 20, location_waypoints[4].getLocation().getY() + 25);

	}
	
	/**
	 * Draw the info of a selected plane in the scene GUI
	 */
	private void drawPlaneInfo() {
		graphics.setColour(graphics.green);
		graphics.rectangle(false, PLANE_INFO.x, PLANE_INFO.y, PLANE_INFO.width, PLANE_INFO.height);
		if (selected_aircraft != null) {
			//System.out.println("set Demo.planeInfo");
			graphics.setViewport(PLANE_INFO.x, PLANE_INFO.y, PLANE_INFO.width, PLANE_INFO.height);
			graphics.printCentred(selected_aircraft.getName(), 0, 5, 2, PLANE_INFO.width);
			// Altitude
			String altitude = String.format("%.0f", selected_aircraft.getPosition().getZ()) + "£";
			graphics.print("Altitude:", 10, 40);
			graphics.print(altitude, PLANE_INFO.width - 10 - altitude.length()*8, 40);
			// Speed
			String speed = String.format("%.2f", selected_aircraft.getSpeed() * 1.687810) + "$";
			graphics.print("Speed:", 10, 55);
			graphics.print(speed, PLANE_INFO.width - 10 - speed.length()*8, 55);
			// Origin
			graphics.print("Origin:", 10, 70);
			graphics.print(selected_aircraft.getFlightPlan().getOriginName(), PLANE_INFO.width - 10 - selected_aircraft.getFlightPlan().getOriginName().length()*8, 70);
			// Destination
			graphics.print("Destination:", 10, 85);
			graphics.print(selected_aircraft.getFlightPlan().getDestinationName(), PLANE_INFO.width - 10 - selected_aircraft.getFlightPlan().getDestinationName().length()*8, 85);
			//System.out.println("restore Demo.planeInfo");
			graphics.setViewport();
		}
	}
	
	/**
	 * Draws points scored for a plane when it successfully leaves the airspace. The points the
	 * plane scored are displayed just above the plane.
	 */
	private void drawPlaneScoreLabels() {
		Aircraft aircraft_to_remove = null;
		int displayed_for = 2000; // How long the label will be displayed for
		for (Aircraft plane : recently_departed_aircraft) {

			double current_time = System.currentTimeMillis(); // Current (system) time
			double departure_time = plane.getTimeOfDeparture(); // Time when the plane successfully left airspace 
			double left_airspace_for = current_time - departure_time; // How long since the plane left airspace
			if (left_airspace_for > displayed_for) {
				aircraft_to_remove = plane;
			}
			else {
				int score_text_alpha =  (int)((displayed_for - left_airspace_for)/displayed_for * 255); // Transparency of the label, 255 is opaque
				String plane_score_value = String.valueOf(plane.getScore() * score.getMultiplier());
				// Drawing the score
				int score_text_x = (int) plane.getFlightPlan().getRoute()[plane.getFlightPlan().getRoute().length -1].getLocation().getX();
				int score_text_y = (int) plane.getFlightPlan().getRoute()[plane.getFlightPlan().getRoute().length -1].getLocation().getY();
				graphics.setColour(255, 255, 255, score_text_alpha);
				if (score_text_x < 40) score_text_x += 50;
				if (score_text_y < 40) score_text_y += 50;
				if (score_text_x > 1000) score_text_x -= 50;
				if (score_text_y > 1000) score_text_y -= 50;
				graphics.print(plane_score_value, score_text_x, score_text_y, 2);
			}

		} 
		if (aircraft_to_remove != null)
			recently_departed_aircraft.remove(aircraft_to_remove);
				
	}
		
	
	/**
	 * draw a readout of the time the game has been played for & aircraft in the sky.
	 */
	private void drawAdditional() {
		int hours = (int)(time_elapsed / (60 * 60));
		int minutes = (int)(time_elapsed / 60);
		minutes %= 60;
		double seconds = time_elapsed % 60;
		java.text.DecimalFormat df = new java.text.DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds); 
		graphics.print(timePlayed, window.width() - (timePlayed.length() * 8 + 32), 32);
		int planes = aircraft_in_airspace.size();
		graphics.print(String.valueOf("Highlighted altitude: " + Integer.toString(highlighted_altitude)) , 32, 15);
		graphics.print(String.valueOf(aircraft_in_airspace.size()) + " plane" + (planes == 1 ? "" : "s") + " in the sky.", 32, 32);
	}
	
	private boolean compassClicked() {
		if (selected_aircraft != null) {
			double dx = selected_aircraft.getPosition().getX() - input.mouseX() + airspace_view_offset_x;
			double dy = selected_aircraft.getPosition().getY() - input.mouseY() + airspace_view_offset_y;
			int r = Aircraft.COMPASS_RADIUS;
			return  dx*dx + dy*dy < r*r;
		}
		return false;
	}
	
	private boolean aircraftClicked(int x, int y) {
		for (Aircraft a : aircraft_in_airspace) {
			if (a.isMouseOver(x-airspace_view_offset_x, y-airspace_view_offset_y)) {
				return true;
			}
		}
		return false;
	}
	
	private Aircraft findClickedAircraft(int x, int y) {
		for (Aircraft a : aircraft_in_airspace) {
			if (a.isMouseOver(x-airspace_view_offset_x, y-airspace_view_offset_y)) {
				return a;
			}
		}
		return null;
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
	
	private boolean isArrivalsClicked(int x, int y) {
		return airport.isWithinArrivals(new Vector(x,y,0)) && !airport.is_active;
	}
	
	private boolean isDeparturesClicked(int x, int y) {
		return airport.isWithinDepartures(new Vector(x,y,0)) && !airport.is_active;
	}

	/**
	 * Handle mouse input
	 */
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
		
		if (key == input.MOUSE_LEFT) {
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
		} else if (key == input.MOUSE_RIGHT) {
			if (compass_clicked && selected_aircraft != null) {
				double dx = input.mouseX() - selected_aircraft.getPosition().getX() + airspace_view_offset_x;
				double dy = input.mouseY() - selected_aircraft.getPosition().getY() + airspace_view_offset_y;
				double newBearing = Math.atan2(dy, dx);
				selected_aircraft.setBearing(newBearing);
			}
		} else if (key == input.MOUSE_WHEEL_UP) {
			highlighted_altitude = 30000;
		} else if (key == input.MOUSE_WHEEL_DOWN){
			highlighted_altitude = 28000;
		}
	}

	@Override
	public void keyPressed(int key) {
		
	}

	@Override
	/**
	 * handle keyboard input
	 */
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
		Demo.difficulty = difficulty;
	}
}