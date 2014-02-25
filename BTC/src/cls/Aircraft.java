package cls;

import java.io.File;
import java.util.ArrayList;

import scn.Demo;
import lib.RandomNumber;
import lib.jog.audio;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;

/**
 * <h1>Aircraft</h1>
 * <p>Represents an aircraft. Calculates velocity, route-following, etc.</p>
 */
public class Aircraft {
	private final static int RADIUS = 16; // The physical size of the plane in pixels. This determines crashes.
	private final static int MOUSE_LENIANCY = 32;  // How far away (in pixels) the mouse can be from the plane but still select it.
	public final static int COMPASS_RADIUS = 64; // How large to draw the bearing circle.
	private final static audio.Sound WARNING_SOUND = audio.newSoundEffect("sfx" + File.separator + "beep.ogg"); // Used during separation violation
	
	private static int minimum_separation_distance; // Depends on difficulty

	private graphics.Image image; // The plane image
	private double turn_speed; // How much the plane can turn per second - in radians.
	private String flight_name; // Unique and generated randomly - format is Flight followed by a random number between 100 and 900 e.g Flight 404
	private double creation_time; // Used to calculate how long an aircraft spent in the airspace
	private double optimal_time; // Optimal time a plane needs to reach its exit point
	
	private Vector position, velocity;
	private boolean is_manually_controlled = false;
	private boolean has_finished = false; // If destination is airport, must be given a land command bnefore it returns True 
	public boolean is_waiting_to_land; // If the destination is the airport, True until land() is called. 
	private double currently_turning_by = 0; // In radians
	private int altitude_change_speed; // The speed to climb or fall by. Depends on difficulty
	private FlightPlan flight_plan;
	private boolean is_landing = false;
	
	public Vector current_target; // The position the plane is currently flying towards (if not manually controlled).
	private double manual_bearing_target = Double.NaN;
	private int current_route_stage = 0;
	private int altitude_state; // Whether the plane is climbing or falling

	private double departure_time; // Used when calculating when a label representing the score a particular plane scored should disappear

	private boolean collision_warning_sound_flag = false;
	
	private int base_score; // Each plane has its own base score that increases total score when a plane successfully leaves the airspace
	private int individual_score;
	private int addition_to_multiplier = 1; // This variable increases the multiplierVariable when a plane successfully leaves the airspace.
	
	private java.util.ArrayList<Aircraft> planes_too_near = new java.util.ArrayList<Aircraft>(); // Holds a list of planes currently in violation of separation rules with this plane
	
	/**
	 * Static ints for use where altitude state is to be changed.
	 */
	public static final int ALTITUDE_CLIMB = 1;
	public static final int ALTITUDE_FALL = -1;
	public static final int ALTITUDE_LEVEL = 0;
	
	// Getters
	/**
	 * Used to get (system) time when an aircraft was created.
	 * @return Time when aircraft was created.
	 */
	public double getTimeOfCreation() {
		return creation_time;
	}	

	/**
	 * Used to get (system) time when an aircraft successfully departed.
	 * @return Time when aircraft departed.
	 */
	public double getTimeOfDeparture() {
		return departure_time;
	}
	
	/**
	 * Getter for optimal time.
	 * @return Optimal time for an aircraft to complete its path.
	 */
	public double getOptimalTime() {
		return optimal_time;
	}
	
	/**
	 * Used to get a base score per plane outside of Aircraft class.
	 * @return base score for plane
	 */
	public int getBaseScore() {
		return base_score;
	}

	/**
	 * Gets the score for a specific aircraft.
	 * @return individual score for plane
	 */
	public int getScore() {
		return individual_score;
	}
	
	/**
	 * Used to get a additionToMultiplier outside of Aircraft class.
	 * @return additionToMultiplier
	 */
	public int getAdditionToMultiplier() {
		return addition_to_multiplier;
	}
	
	public Vector getPosition() {
		return position;
	}

	public String getName() {
		return flight_name;
	}

	public boolean isFinished() { // Returns whether the plane has reached its destination
		return has_finished;
	}

	public boolean isManuallyControlled() {
		return is_manually_controlled;
	}

	public int getAltitudeState() {
		return altitude_state;
	}
	
	public double getBearing() {
		return Math.atan2(velocity.getY(), velocity.getX());
	}

	public double getSpeed() {
		return velocity.magnitude();
	}
	
	public FlightPlan getFlightPlan() {
		return flight_plan;
	}
	
	// Setters
	/**
	 * Used outside of Aircraft class to assign a (system) time to a plane that successfully left airspace
	 * @param departureTime (system time when a plane departed)
	 */
	public void setDepartureTime(double departureTime) {
		departure_time = departureTime;
	} 

	/**
	 * Sets the score for a specific aircraft.
	 */
	public void setScore(int score) {
		individual_score = score;
	}

	/**
	 * Used to set additionToMultiplier outside of Aircraft class.
	 * @param number
	 */
	public void setAdditionToMultiplier(int multiplierLevel) {
		switch (multiplierLevel) {
		case 1:
			addition_to_multiplier = 64;
			break;
		case 2:
			addition_to_multiplier = 32;
			break;
		case 3:
			addition_to_multiplier = 32;
			break;
		case 4:
			addition_to_multiplier = 16;
			break;
		case 5:
			addition_to_multiplier = 8;
			break;
		}
	}
	
	public void setBearing(double newHeading) {
		manual_bearing_target = newHeading;
	}
	
	private void setAltitude(int height) {
		velocity.setZ(height);
	}
	
	public void setAltitudeState(int state) {
		this.altitude_state = state; // Either climbing or falling
	}

	/**
	 * Constructor for an aircraft.
	 * @param name the name of the flight.
	 * @param nameOrigin the name of the location from which the plane hails.
	 * @param nameDestination the name of the location to which the plane is going.
	 * @param originPoint the point to initialise the plane.
	 * @param destinationPoint the end point of the plane's route.
	 * @param img the image to represent the plane.
	 * @param speed the speed the plane will travel at.
	 * @param sceneWaypoints the waypoints on the map.
	 * @param difficulty the difficulty the game is set to
	 */
	public Aircraft(String name, String name_destination, String name_origin, Waypoint destination_point, Waypoint origin_point, graphics.Image img, double speed, Waypoint[] scene_waypoints, int difficulty) {
		flight_name = name;		
		flight_plan = new FlightPlan(scene_waypoints, name_origin, name_destination, origin_point, destination_point);		
		image = img;
		creation_time = System.currentTimeMillis() / 1000; // System time when aircraft was created in seconds.
		position = origin_point.getLocation();
		
		if (origin_point.getLocation() == Demo.airport.getLocation()) {
			position = position.add(new Vector(-100, -70, 0)); // Start at departures
		}
		int altitude_offset = RandomNumber.randInclusiveInt(0, 1) == 0 ? 28000 : 30000;
		position = position.add(new Vector(0, 0, altitude_offset));

		// Calculate initial velocity (direction)
		current_target = flight_plan.getRoute()[0].getLocation();
		double x = current_target.getX() - position.getX();
		double y = current_target.getY() - position.getY();
		velocity = new Vector(x, y, 0).normalise().scaleBy(speed);

		is_waiting_to_land = flight_plan.getDestination().equals(Demo.airport.getLocation());

		// Speed up plane for higher difficulties
		switch (difficulty) {
		// Adjust the aircraft's attributes according to the difficulty of the parent scene
		// 0 has the easiest attributes (slower aircraft, more forgiving separation rules)
		// 2 has the hardest attributes (faster aircraft, least forgiving separation rules)
		case Demo.DIFFICULTY_EASY:
			minimum_separation_distance = 64;
			turn_speed = Math.PI / 4;
			altitude_change_speed = 500;
			base_score = 60;
			optimal_time = flight_plan.getTotalDistance() / speed;
		break;

		case Demo.DIFFICULTY_MEDIUM:
			minimum_separation_distance = 96;
			velocity = velocity.scaleBy(2);
			turn_speed = Math.PI / 3;
			altitude_change_speed = 300;
			base_score = 150;
			optimal_time = flight_plan.getTotalDistance() / (speed * 2);
		break;
			
		case Demo.DIFFICULTY_HARD:
			minimum_separation_distance = 128;
			velocity = velocity.scaleBy(3);
			// At high velocities, the aircraft is allowed to turn faster - this helps keep the aircraft on track.
			turn_speed = Math.PI / 2;
			altitude_change_speed = 200;
			base_score = 300;
			addition_to_multiplier = 3;
			optimal_time = flight_plan.getTotalDistance() / (speed * 3);
		break;

		default:
			Exception e = new Exception("Invalid Difficulty: " + difficulty + ".");
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the angle from the plane's position, to its current target.
	 * @return the angle in radians to the plane's current target.
	 */
	private double angleToTarget() {
		if (is_manually_controlled) {
			return (manual_bearing_target == Double.NaN) ? getBearing(): manual_bearing_target;
		} else {
			return Math.atan2(current_target.getY() - position.getY(), current_target.getX() - position.getX());
		}
	}

	public boolean isOutOfAirspaceBounds() {
		double x = position.getX();
		double y = position.getY();
		return (x < RADIUS || x > window.width() + RADIUS - 32 || y < RADIUS || y > window.height() + RADIUS - 176);
	}

	public boolean isAt(Vector point) {
		double dy = point.getY() - position.getY();
		double dx = point.getX() - position.getX();
		return dy*dy + dx*dx < 6*6;
	}

	public boolean isTurningLeft() {
		return currently_turning_by < 0;
	}

	public boolean isTurningRight() {
		return currently_turning_by > 0;
	}
	
	/**
	 * Edits the plane's path by changing the waypoint it will go to at a certain stage in its route.
	 * @param routeStage the stage at which the new waypoint will replace the old.
	 * @param newWaypoint the new waypoint to travel to.
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		if (routeStage > -1) {
			flight_plan.alterPath(routeStage, newWaypoint);
			if (!is_manually_controlled)
				resetBearing();
			if (routeStage == current_route_stage) {
				current_target = newWaypoint.getLocation();
				turnTowardsTarget(0);
			}
		}
	}

	public boolean isMouseOver(int mx, int my) {
		double dx = position.getX() - mx;
		double dy = position.getY() - my;
		return dx * dx + dy * dy < MOUSE_LENIANCY * MOUSE_LENIANCY;
	}

	/**
	 * Calls {@link isMouseOver()} using {@link input.mouseX()} and {@link input.mouseY()} as the arguments.
	 * @return true, if the mouse is close enough to this plane. False, otherwise.
	 */
	public boolean isMouseOver() {
		return isMouseOver(input.mouseX() - Demo.airspace_view_offset_x, input.mouseY() - Demo.airspace_view_offset_y);
	}
	
	public boolean isAtDestination() {
		if (flight_plan.getDestination().equals(Demo.airport.getLocation())) { // At airport
			return Demo.airport.isWithinArrivals(position, false); // Within Arrivals rectangle
		} else {
			return isAt(flight_plan.getDestination()); // Very close to destination
		}
	}

	/**
	 * Updates the plane's position and bearing, the stage of its route, and whether it has finished its flight.
	 * @param time_difference
	 */
	public void update(double time_difference) {
		if (has_finished) return;
		
		// Update altitude
		if (is_landing) {
			if (position.getZ() > 100) { 
				position.setZ(position.getZ() - 2501 * time_difference); // Decrease altitude rapidly (2501/second), ~11 seconds to fully descend
			} else { // Gone too low, land it now
				Demo.airport.is_active = false;
				has_finished = true;
			}
		} else {
			switch (altitude_state) {
			case -1:
				fall();
				break;
			case 0:
				break;
			case 1:
				climb();
				break;
			}
		}

		// Update position
		Vector dv = velocity.scaleBy(time_difference);
		position = position.add(dv);

		currently_turning_by = 0;

		// Update target		
		if (current_target.equals(flight_plan.getDestination()) && isAtDestination()) { // At finishing point
			if (!is_waiting_to_land) { // Ready to land
				has_finished = true;
				if (flight_plan.getDestination().equals(Demo.airport.getLocation())) { // Landed at airport
					Demo.airport.is_active = false;
				}
			}
		} else if (isAt(current_target)) {
			current_route_stage++;
			// Next target is the destination if you're at the end of the plan, otherwise it's the next waypoint
			current_target = current_route_stage >= flight_plan.getRoute().length ? flight_plan.getDestination() : flight_plan.getRoute()[current_route_stage].getLocation();
		}

		// Update bearing
		if (Math.abs(angleToTarget() - getBearing()) > 0.01) {
			turnTowardsTarget(time_difference);
		}
	}

	public void turnLeft(double time_difference) {
		turnBy(time_difference * -turn_speed);
		manual_bearing_target = Double.NaN;
	}

	public void turnRight(double time_difference) {
		turnBy(time_difference * turn_speed);
		manual_bearing_target = Double.NaN;
	}

	/**
	 * Turns the plane by a certain angle (in radians). Positive angles turn the plane clockwise.
	 * @param angle the angle by which to turn.
	 */
	private void turnBy(double angle) {
		currently_turning_by = angle;
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		double x = velocity.getX();
		double y = velocity.getY();
		velocity = new Vector(x*cosA - y*sinA, y*cosA + x*sinA, velocity.getZ());
	}

	private void turnTowardsTarget(double time_difference) {
		// Get difference in angle
		double angle_difference = (angleToTarget() % (2 * Math.PI)) - (getBearing() % (2 * Math.PI));
		boolean crossesPositiveNegativeDivide = angle_difference < -Math.PI * 7 / 8;
		// Correct difference
		angle_difference += Math.PI;
		angle_difference %= (2 * Math.PI);
		angle_difference -= Math.PI;
		// Get which way to turn.
		int angle_direction = (int) (angle_difference /= Math.abs(angle_difference));
		if (crossesPositiveNegativeDivide)
			angle_direction *= -1;
		double angle_magnitude = Math.min(Math.abs((time_difference * turn_speed)), Math.abs(angle_difference));
		turnBy(angle_magnitude * angle_direction);
	}

	/**
	 * Draws the plane and any warning circles if necessary.
	 * @param The altitude to highlight aircraft at
	 */
	public void draw(int highlighted_altitude) {
		double alpha;
		if (position.getZ() >= 28000 && position.getZ() <= 29000) { // 28000-29000
			alpha = highlighted_altitude == 28000 ? 255 : 128; // 255 if highlighted, else 128
		} else if (position.getZ() <= 30000 && position.getZ() >= 29000) { // 29000-30000
			alpha = highlighted_altitude == 30000 ? 255 : 128; // 255 if highlighted, else 128
		} else { // If it's not 28000-30000, then it's currently landing
			alpha = 128; 
		}
		double scale = 2*(position.getZ()/30000); // Planes with lower altitude are smaller
		
		// Draw plane image
		graphics.setColour(128, 128, 128, alpha);
		graphics.draw(image, scale, position.getX()-image.width()/2, position.getY()-image.height()/2, getBearing(), 8, 8);
		
		// Draw altitude label
		graphics.setColour(128, 128, 128, alpha/2.5);
		graphics.print(String.format("%.0f", position.getZ()) + "�", position.getX()+8, position.getY()-8); // � displayed as ft
		drawWarningCircles();
	}

	/**
	 * Draws the compass around this plane - Used for manual control
	 */
	public void drawCompass() {
		graphics.setColour(graphics.green);
		
		// Centre positions of aircraft
		Double xpos = position.getX()-image.width()/2 + Demo.airspace_view_offset_x; 
		Double ypos = position.getY()-image.height()/2 + Demo.airspace_view_offset_y;
		
		// Draw the compass circle
		graphics.circle(false, xpos, ypos, COMPASS_RADIUS, 30);
		
		// Draw the angle labels (0, 60 .. 300)
		for (int i = 0; i < 360; i += 60) {
			double r = Math.toRadians(i - 90);
			double x = xpos + (1.1 * COMPASS_RADIUS * Math.cos(r));
			double y = ypos - 2 + (1.1 * COMPASS_RADIUS * Math.sin(r));
			if (i > 170) x -= 24;
			if (i == 180) x += 12;
			graphics.print(String.valueOf(i), x, y);
		}
		
		// Draw the line to the mouse pointer
		double x, y;
		if (is_manually_controlled && input.isMouseDown(input.MOUSE_RIGHT)) {
			graphics.setColour(graphics.green_transp);
			double r = Math.atan2(input.mouseY() - position.getY(), input.mouseX() - position.getX());
			x = xpos + (COMPASS_RADIUS * Math.cos(r));
			y = ypos + (COMPASS_RADIUS * Math.sin(r));
			// Draw several lines to make the line thicker
			graphics.line(xpos, ypos, x, y);
			graphics.line(xpos-1, ypos, x, y);
			graphics.line(xpos, ypos-1, x, y);
			graphics.line(xpos+1, ypos, x, y);
			graphics.line(xpos+1, ypos+1, x, y);
			graphics.setColour(0, 128, 0, 16);
		}

		// Draw current bearing line
		x = xpos + (COMPASS_RADIUS * Math.cos(getBearing()));
		y = ypos + (COMPASS_RADIUS * Math.sin(getBearing()));
		// Draw several lines to make it thicker
		graphics.line(xpos, ypos, x, y);
		graphics.line(xpos-1, ypos, x, y);
		graphics.line(xpos, ypos-1, x, y);
		graphics.line(xpos+1, ypos, x, y);
		graphics.line(xpos+1, ypos+1, x, y);
	}

	/**
	 * Draws warning circles around this plane and any others that are too near.
	 */
	private void drawWarningCircles() {
		for (Aircraft plane : planes_too_near) {
			Vector mid_point = position.add(plane.position).scaleBy(0.5);
			double radius = position.sub(mid_point).magnitude() * 2;
			graphics.setColour(graphics.red);
			graphics.circle(false, mid_point.getX(), mid_point.getY(), radius);
		}
	}

	/**
	 * Draws lines starting from the plane, along its flight path to its destination.
	 */
	public void drawFlightPath(boolean is_selected) {
		if (is_selected) {
			graphics.setColour(0, 128, 128);
		} else {
			graphics.setColour(0, 128, 128, 128);
		}

		Waypoint[] route = flight_plan.getRoute();
		Vector destination = flight_plan.getDestination();
		
		if (current_target != destination) {
			// Draw line from plane to next waypoint
			graphics.line(position.getX()-image.width()/2, position.getY()-image.height()/2, route[current_route_stage].getLocation().getX(), route[current_route_stage].getLocation().getY());
		} else {
			// Draw line from plane to destination
			graphics.line(position.getX()-image.width()/2, position.getY()-image.height()/2, destination.getX(), destination.getY());			
		}
		
		for (int i = current_route_stage; i < route.length-1; i++) { // Draw lines between successive waypoints
			graphics.line(route[i].getLocation().getX(), route[i].getLocation().getY(), route[i+1].getLocation().getX(), route[i+1].getLocation().getY());	
		}
	}

	/**
	 * Visually represents the waypoint being moved.
	 * @param modified the index of the waypoint being modified
	 * @param mouseX current position of mouse
	 * @param mouseY current position of mouse
	 */
	public void drawModifiedPath(int modified, double mouseX, double mouseY) {
		graphics.setColour(0, 128, 128, 128);
		Waypoint[] route = flight_plan.getRoute();
		Vector destination = flight_plan.getDestination();
		if (current_route_stage > modified - 1) {
			graphics.line(getPosition().getX(), getPosition().getY(), mouseX, mouseY);
		} else {
			graphics.line(route[modified-1].getLocation().getX(), route[modified-1].getLocation().getY(), mouseX, mouseY);
		}
		if (current_target == destination) {
			graphics.line(mouseX, mouseY, destination.getX(), destination.getY());
		} else {
			int index = modified + 1;
			if (index == route.length) { // Modifying final waypoint in route
				// Line drawn to final waypoint
				graphics.line(mouseX, mouseY, destination.getX(), destination.getY());
			} else {
				graphics.line(mouseX, mouseY, route[index].getLocation().getX(), route[index].getLocation().getY());
			}
		}
	}

	/**
	 * Updates the number of planes that are violating the separation rule. Also checks for crashes.
	 * @param time_difference the time elapsed since the last frame.
	 * @param aircraftList all aircraft in the airspace
	 * @param global score object used to decrement score if separation is breached
	 * @return index of plane breaching separation distance with this plane, or -1 if no planes are in violation.
	 */
	public int updateCollisions(double time_difference,	ArrayList<Aircraft> aircraftList, Score score) {
		planes_too_near.clear();
		for (int i = 0; i < aircraftList.size(); i++) {
			Aircraft plane = aircraftList.get(i);
			if (plane != this && isWithin(plane, RADIUS)) { // Planes crash
				has_finished = true;
				return i;
			} else if (plane != this && isWithin(plane, minimum_separation_distance)) { // Breaching separation distance
				planes_too_near.add(plane);
				score.increaseMeterFill(-1); // Punishment for breaching separation rules (applies to all aircraft involved - usually 2)
				if (!collision_warning_sound_flag) {
					collision_warning_sound_flag = true;
					WARNING_SOUND.play();
				}
			}
		}
		if (planes_too_near.isEmpty()) {
			collision_warning_sound_flag = false;
		}
		return -1;
	}

	/**
	 * Checks whether an aircraft is within a certain distance from this one.
	 * @param aircraft the aircraft to check.
	 * @param distance the distance within which to care about.
	 * @return true, if the aircraft is within the distance. False, otherwise.
	 */
	private boolean isWithin(Aircraft aircraft, int distance) {
		double dx = aircraft.getPosition().getX() - position.getX();
		double dy = aircraft.getPosition().getY() - position.getY();
		double dz = aircraft.getPosition().getZ() - position.getZ();
		return dx*dx + dy*dy + dz*dz < distance*distance;
	}

	public void toggleManualControl() {
		if (is_landing) { // Can't manually control while landing
			is_manually_controlled = false;
		} else {
			is_manually_controlled = !is_manually_controlled;
			if (is_manually_controlled) {
				setBearing(getBearing());
			} 
			else {
				resetBearing();
			}
		}
	}

	private void resetBearing() {
		if (current_route_stage < flight_plan.getRoute().length & flight_plan.getRoute()[current_route_stage] != null) {
			current_target = flight_plan.getRoute()[current_route_stage].getLocation();
		}
		turnTowardsTarget(0);
	}

	private void climb() {
		if (position.getZ() < 30000 && altitude_state == ALTITUDE_CLIMB)
			setAltitude(altitude_change_speed);
		if (position.getZ() >= 30000) {
			setAltitude(0);
			altitude_state = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 30000);
		}
	}

	private void fall() {
		if (position.getZ() > 28000 && altitude_state == ALTITUDE_FALL)
			setAltitude(-altitude_change_speed);
		if (position.getZ() <= 28000) {
			setAltitude(0);
			altitude_state = ALTITUDE_LEVEL;
			position = new Vector(position.getX(), position.getY(), 28000);
		}
	}

	public void land() {
		is_waiting_to_land = false;
		is_landing = true;
		is_manually_controlled = false;
		Demo.airport.is_active = true;
	}

	public void takeOff() {
		Demo.airport.is_active = true;
		Demo.takeOffSequence(this);
		creation_time = System.currentTimeMillis() / 1000; // Reset creation time
	}

	/**
	 * Checks if an aircraft is close to an its parameter (entry point).
	 * @param position of a waypoint
	 * @return True it if it close
	 */
	public boolean isCloseToEntry(Vector position) {
		double x = this.getPosition().getX() - position.getX();
		double y = this.getPosition().getY() - position.getY();
		return x*x + y*y <= 300*300;
	}
}