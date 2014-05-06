package cls;

import java.io.File;

import lib.jog.graphics;

public class Airport extends Waypoint {

	private static graphics.Image airport;
	// All location values are absolute and based on the current version of the airport image.
	private static double 
		arrivals_width = 105, arrivals_height = 52,
		departures_width = 50, departures_height = 36;
	
	private double scale;
	
	private double
		x_location, y_location,
		arrivals_x_location, arrivals_y_location,
		runway_end_x_location, runway_end_y_location, 
		departures_x_location, departures_y_location;

	public boolean is_active = false; // True if there is an aircraft Landing/Taking off
	
	private boolean 
		is_arrivals_clicked = false,
		is_departures_clicked = false;
	
	public java.util.ArrayList<Aircraft> aircraft_waiting_to_land = new java.util.ArrayList<Aircraft>();
	/**
	 * Time entered is directly related to the aircraft hangar and stores the time each aircraft entered the hangar
	 * this is used to determine score multiplier decrease if aircraft is in the hangar for too long
	 */
	public java.util.ArrayList<Aircraft> aircraft_hangar = new java.util.ArrayList<Aircraft>();
	public java.util.ArrayList<Double> time_entered = new java.util.ArrayList<Double>();
	private final int hangar_size = 3;
	
	
	public Airport(String name, double x_location, double y_location, double scale) {
		super(x_location, y_location, true, name);
		if (airport == null){
			loadImage();
		}
		this.x_location = x_location;
		this.y_location = y_location;
		this.scale = scale;
		arrivals_x_location = x_location + (97 -airport.width()/2)*scale;
		arrivals_y_location = y_location + (86 -airport.height()/2)*scale;
		
		runway_end_x_location = x_location +(120 -airport.width()/2)*scale;
		runway_end_y_location = y_location +(69 -airport.height()/2)*scale;
		
		departures_x_location = x_location +(4 -airport.width()/2)*scale;
		departures_y_location = y_location +(54 -airport.height()/2)*scale;
	}
	
	private static void loadImage() {
		airport = graphics.newImage("gfx" +File.separator +"Airport.png");
	}
	
	@Override
	@Deprecated
	public void draw(){
		super.draw();
		System.out.println("call to Airport.draw(); this is deprecated."
				+"\nUse Airport.draw(double current_time) to properly display the airport.");
	}
	
	/** draw function with extra parameters to allow extra functionality */
	public void draw(double current_time) { 
		// Draw the airport image
		graphics.setColour(255, 255, 255);
		graphics.draw(airport, scale, x_location, y_location, 0,  airport.width()/2, airport.height()/2);
				
		int	green_fine = 128,
			green_danger = 0,
			red_fine = 0,
			red_danger = 128;
		
		// Draw the hangar button if plane is waiting (departing flights)
		if (aircraft_hangar.size() > 0) {
			// Colour fades from green (fine) to red (danger) over 5 seconds as plane is waiting
			int time_waiting = (int)(current_time -time_entered.get(0));
			// Assume it hasn't been waiting
			int	green_now = green_fine, 
				red_now = red_fine;
			if (time_waiting >= 5) { // Cap at 5 seconds
				green_now = green_danger;
				red_now = red_danger;
			} else {
				// Colour between fine and danger, scaled by time_waiting
				green_now = green_fine - (int)(Math.abs(green_fine-green_danger) * (time_waiting/5.0)); 
				red_now = (int)(Math.abs(red_fine-red_danger) * (time_waiting/5.0));
			}

			// Draw border, draw as filled if clicked
			graphics.setColour(red_now, green_now, 0, 256);
			graphics.rectangle(is_departures_clicked, departures_x_location, departures_y_location, departures_width*scale, departures_height*scale);

			// Draw box
			graphics.setColour(red_now, green_now, 0, 64);
			graphics.rectangle(true, departures_x_location + 1, departures_y_location + 1, departures_width*scale - 2, departures_height*scale - 2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraft_hangar.size()), departures_x_location + 23*scale, departures_y_location + 15*scale);
		}
		graphics.setColour(0, 128, 0, 128);
		// Draw the arrivals button if at least one plane is waiting (arriving flights)
		if (aircraft_waiting_to_land.size() > 0) {
			// Draw border, draw as filled if clicked
			graphics.rectangle(is_arrivals_clicked, arrivals_x_location, arrivals_y_location, arrivals_width*scale, arrivals_height*scale);
			graphics.setColour(128, 128, 0, 64);			
			// Draw box
			graphics.rectangle(true, arrivals_x_location + 1, arrivals_y_location + 1, arrivals_width*scale -2, arrivals_height*scale -2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraft_waiting_to_land.size()), arrivals_x_location + 50*scale, arrivals_y_location + 26*scale);
		}	
		//graphics.setColour(255, 255, 255, 128);
		//graphics.print(Integer.toString(1), x_location+120, y_location-65);
	}
	
	public double getLongestTimeInHangar(double currentTime) {
		return aircraft_hangar.isEmpty() ? 0 : currentTime-time_entered.get(0);
	}
	
	/**
	 *  Arrivals is the portion of the airport image which is used to issue the land command
	 * @param gamePosition is the point to be tested
	 * @return true if point is within the rectangle that defines the arrivals portion of the airport
	 */
	public boolean isWithinArrivals(Vector gamePosition) {
		return isWithinRect((int)gamePosition.getX(), (int)gamePosition.getY(),
				(int)(arrivals_x_location), (int)(arrivals_y_location),
				(int)(arrivals_width*scale), (int)(arrivals_height*scale));
	}
	
	// Used for calculating if an aircraft is within the airspace for landing - offset should not be applied
	public boolean isWithinArrivals(Vector position, boolean apply_offset) {
		return (apply_offset ? isWithinArrivals(position) : isWithinRect((int)position.getX(), (int)position.getY(),(int)(arrivals_x_location), (int)(arrivals_y_location), (int)(arrivals_width*scale), (int)(arrivals_height*scale)));
	}
	
	/**
	 * Departures is the portion of the airport image which is used to issue the take off command
	 * @param gamePosition is the point to be tested
	 * @return true if point is within the rectangle that defines the departures portion of the airport
	 */
	public boolean isWithinDepartures(Vector gamePosition) {
		return isWithinRect((int)gamePosition.getX(), (int)gamePosition.getY(),
				(int)(departures_x_location), (int)(departures_y_location), 
				(int)(departures_width*scale), (int)(departures_height*scale));
	}
	
	public boolean isWithinRect(int test_x, int test_y, int x, int y, int width, int height) {
		return x <= test_x && test_x <= x + width && y <= test_y && test_y <= y + height;
	}
	
	/**
	 * Adds aircraft to the back of the hangar and records the time in the time_entered list
	 * will only add the aircraft if the current size is less than the maximum denoted by hangar_size
	 * @param aircraft
	 */
	public void addToHangar(Aircraft aircraft, double time) {
		if (aircraft_hangar.size() < hangar_size) {
			aircraft_hangar.add(aircraft);
			time_entered.add(time);
		}
	}
	
	/** 
	 * Causes the oldest Aircraft in the hangar to take off, if any.
	 * @return that Aircraft instance, else null
	 */
	public Aircraft signalTakeOff() {
		if (!aircraft_hangar.isEmpty()) {
			Aircraft aircraft = aircraft_hangar.remove(0);
			time_entered.remove(0);
			aircraft.takeOff();
			return aircraft;
		}
		else return null;
	}
	  
	/** 
	 * Decides whether to draw the radius around the airport by checking if any aircraft which are landing are close
	 * @param demo For getting aircraft list
	 */
	public void update(java.util.List<Aircraft> aircraftInAirspace) {
		aircraft_waiting_to_land.clear();
		for (Aircraft a : aircraftInAirspace) {
			if (a.current_target.equals(this.getLocation())) {
				aircraft_waiting_to_land.add(a);
			}
		}
	}
	
	public int getHangarSize() {
		return hangar_size;
	}
	public Vector getRunwayLocation(){
		return new Vector(runway_end_x_location, runway_end_y_location, 0);
	}
	
	/** notify this instance that the arrivals zone has received input
	 * (for use with {@link isWithinArrivals(Vector)} */
	public void arrivalsTriggered(){
		is_arrivals_clicked = true;
	}
	
	/** notify this instance that the departures zone has received input
	 * (for use with {@link isWithinDepartures(Vector)) */
	public void departuresTriggered(){
		is_departures_clicked = true;
	}


	/** notify this instance that any trigger has been removed */
	public void releaseTriggered(){
		is_arrivals_clicked = false;
		is_departures_clicked = false;
	}
	
	
	// Used in testing avoiding the need to have a demo instance
	@Deprecated
	public void signalTakeOffTesting() {
		if (aircraft_hangar.size() > 0) {
			aircraft_hangar.remove(0);
			time_entered.remove(0);
		}	
	}

	public void clear() {
		aircraft_hangar.clear();
		time_entered.clear();
	}
}
