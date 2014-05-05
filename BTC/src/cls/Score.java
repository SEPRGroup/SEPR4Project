package cls;

import java.util.Arrays;

import lib.jog.graphics;

public class Score {
	
	private int width, height;
	
	private final int MAX_SCORE = 9999999;
	private final int MAX_DIGITS_IN_SCORE = (int)Math.log10(MAX_SCORE) + 1;

	private int total_score = 0; // Records the total score the user has achieved at a given time.
	private int target_score = 0;

	private boolean meter_draining = false; // Used to decide if meter should be red or green
	/**
	 * Initially set to 1. This is the main multiplier for score. As more planes leave airspace 
	 * it may be incremented based on the value of multiplierVariable (the interval it is currently in).
	 */	
	private int multiplier = 1; 
	
	/**
	 * Initially 0 (i.e. the meter is empty when you start the game).
	 * Set the level at which to fill the multiplier meter on the GUI.
	 * Used to increase and decrease the multiplier when it exceeds certain bounds -> currently less than 0 and greater than 256.
	 */
	private int meter_fill = 0;	
	private int target_meter_fill = 0;

	/**
	 * This variable determines the current level of the multiplier. Each level has an associated multiplier value
	 * e.g. multiplier_level = 1 -> multiplier = 1, multiplier_level = 2 -> multiplier = 3, multiplier_level = 3 -> multiplier = 5.
	 * Increased when meter_fill >= 256, decreased when meter_fill < 0.
	 * Also used in Demo to vary max_aircraft and aircraft_spawn rates.
	 */
	private int multiplierLevel = 1;
	
	// Variables used in multiplier bar
	private int	
		bar_segments = 16,
		bar_segment_dif,
		bar_x_offset = 280 +8,
		bar_y_offset = 0,
		bar_segment_width,
		bar_segment_height;
	
	
	public Score(int width, int height) {
		this.width = width;
		this.height = height;
		setSize();
	}
	
	/** set up positioning based on width, height */
	private void setSize(){
		int bar_width = width -bar_x_offset -64;
		bar_segment_dif = bar_width / bar_segments;
		bar_segment_width = bar_segment_dif *2 /3;
		bar_segment_height = height;
	}
	
	
	public int getTotalScore() {
		if (total_score > MAX_SCORE)
			total_score = MAX_SCORE;
		return total_score;
	}
	
	public int getTargetScore() {
		if (target_score > MAX_SCORE)
			target_score = MAX_SCORE;
		return target_score;
	}
	
	public int getMultiplierLevel() {
		return multiplierLevel;
	}
	
	public int getMultiplier() {
		return multiplier;
	}
	
	public int getMeterFill() {
		return meter_fill;
	}
	public int getTargetMeterFill() {
		return target_meter_fill;
	}
	
	public void increaseTotalScore(int amount) {
		if (amount > 0) // Score cannot decrease
			target_score += amount;
	}
	
	/**
	 * Takes an aircraft and calculates it's score.
	 * Score per plane is based on a base score (which varies with difficulty) for the plane,
	 * and how efficient the player has been in navigating the aircraft to it's destination.
	 * A minimum of the base score is always awarded with a bonus of up to base_score/3.
	 */
	public int calculateAircraftScore(Aircraft aircraft) {
		double efficiency = efficiencyFactor(aircraft);
		int base_score = aircraft.getBaseScore();
		int bonus = (int)((base_score*efficiency)/3);
		return base_score +bonus;
	}
	
	/**
	 * calculates how optimal the player was, by taking the ratio of the time to traverse the shortest path to the actual time taken.
	 * @param optimalTime - Ideal time, not really possible to achieve.  
	 * @param timeTaken - Total time a plane spent in the airspace. 
	 * @return the extent to which the player achieved optimal time.
	 */
	private double efficiencyFactor(Aircraft aircraft) {
		double optimal_time = aircraft.getOptimalTime();
		double time_taken = System.currentTimeMillis()/1000 - aircraft.getTimeOfCreation();
		double efficiency = optimal_time/time_taken;
		return efficiency;
	}
	
	/** Resets the multiplier_level to 1 and empties the meter. */	
	public void resetMultiplier() {
		multiplierLevel = 1;
		multiplier = 1;
		target_meter_fill = 0;
		meter_fill = 0;
	} 	
	
	// This method should only be used publically for unit testing
	public void increaseMultiplierLevel() {
		if (multiplierLevel <= 5) {
			multiplierLevel += 1;
			setMultiplier();
		}
	}
	
	// This method should only be used publically for unit testing
	public void decreaseMultiplierLevel() {
		if (multiplierLevel >= 1) {
			multiplierLevel -= 1;
			setMultiplier();
		}
	}
		
	/**
	 * Updates multiplier based on the multiplierLevel, each multiplierLevel has an associated value to set the multiplier at. 
	 * Is updated whenever multiplierLevel changes.
	 */		
	private void setMultiplier() {
		switch(multiplierLevel) {
		case 1:
			multiplier = 1;
			break;
		case 2:
			multiplier = 3;
			break;
		case 3:
			multiplier = 5;
			break;
		case 4:
			multiplier = 7;
			break;
		case 5:
			multiplier = 10;
			break;
		}
	}
	
	private void updateMultiplierLevel() {
		if (meter_fill >= 256) { // Meter full
			if (multiplierLevel < 5) { // Not at max multiplier
				increaseMultiplierLevel();
				meter_fill -= 256;
				target_meter_fill -= 256;
			} else { // At max multiplier
				meter_fill = 256;
				target_meter_fill = 256;
			}
		}
			
		if (meter_fill < 0) { // Meter drained past empty
			if (multiplierLevel > 1) { // Not at minimum multiplier
				decreaseMultiplierLevel();
				meter_fill += 256;
				target_meter_fill += 256;
			} else { // At minimum multiplier
				meter_fill = 0;
				target_meter_fill = 0;
			}
		}	
	}
	
	public void increaseMeterFill(int change_to_meter) {
		target_meter_fill += change_to_meter;
	}
	
	public void draw() {
		drawScore();
		drawMultiplier();
	}
	
	private void drawScore() {
		// Takes the maximum possible digits in the score and calculates how many of them are currently 0
		int current_digits_in_score = 
				getTotalScore() != 0 ? (int)Math.log10(getTotalScore()) + 1 : 0; // Don't do log10(0) as it's undefined and gives an exception
		char[] chars = new char[MAX_DIGITS_IN_SCORE -current_digits_in_score];
		Arrays.fill(chars, '0');
		String zeros = new String(chars);
		
		// Prints the unused score digits as 0s, and the current score.
		graphics.setColour(graphics.green_transp);
		graphics.print(zeros, 0, -4, 5);
		graphics.setColour(graphics.green);
		if (getTotalScore() != 0) 
			graphics.printRight(String.valueOf(getTotalScore()),  280, -4, 5, 0);		
	}
	
	private void drawMultiplier() {
		// Initially assumed green
		int red = 0;
		int green = 128;		
		if (meter_draining) { // Make it red
			red = 128;
			green = 0;
		}		
		
		int x = bar_x_offset;	//temporary variable for positioning
		for (int i = 0; i < bar_segments; i++) { // Draw each segment
			// Draw background
			graphics.setColour(red, green, 0, 64);
			graphics.rectangle(true, x, bar_y_offset, bar_segment_width, bar_segment_height);
			graphics.setColour(red, green, 0);
			// Draw inside
			drawMultiplierSegment(meter_fill, i, x, bar_y_offset, bar_segment_width, bar_segment_height);
			// Go to next segment
			x += bar_segment_dif;
		}
		graphics.setColour(graphics.green);
		
		// Print multiplier e.g. x 10
		x += 8;
		String mul_var = String.format("%d", multiplier);
		graphics.print("x", x, 9, 3);
		graphics.print(mul_var, x +24, -4, 5);
	}

	private void drawMultiplierSegment(int meter_fill, int segment_number, int bar_x_offset, int bar_y_offset, int segment_width, int segment_height) {
		int start_x = segment_number*segment_width; 
		int end_x = start_x + segment_width;
		
		int scale_meter_fill = (bar_segments*segment_width *meter_fill)/256;
		
		if (scale_meter_fill >= start_x && scale_meter_fill < end_x) { // Partially fill segment
			graphics.rectangle(true, bar_x_offset, bar_y_offset, (scale_meter_fill -start_x), segment_height);
		} else if (scale_meter_fill >= end_x) { // Fill whole segment
			graphics.rectangle(true, bar_x_offset, bar_y_offset, segment_width, segment_height);
		}
	}
	
	public void update() {
		// Bring total score closer to target score
		int increase_amount = multiplier*2 + 1; // Add 1 so it's an odd number and will affect all digits
		if (target_score - total_score <= increase_amount) 
			total_score = target_score;
		else
			total_score += increase_amount;
		
		if (target_meter_fill != meter_fill) {
			if (target_meter_fill > meter_fill) { // Filling
				meter_draining = false;
				meter_fill++;
			} else { // Draining
				meter_draining = true;
				if (meter_fill - target_meter_fill > 2)
					meter_fill -= 2; // Done in increments of 2 so it's faster as draining generally happens faster than filling
				else
					meter_fill--;
			}
			updateMultiplierLevel();
		} else
			meter_draining = false;
	}
}