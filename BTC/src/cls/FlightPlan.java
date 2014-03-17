package cls;

import java.util.ArrayList;
import java.util.Random;

public class FlightPlan {	
	private Waypoint[] route;
	private String origin_name;
	private String destination_name;
	private Vector destination;
	
	public FlightPlan(Waypoint[] route, String origin_name, String destination_name, Waypoint origin_point, Waypoint destination_point) {
		this.route = findGreedyRoute(origin_point, destination_point, route);
		this.origin_name = origin_name;
		this.destination_name = destination_name;
		this.destination = destination_point.getLocation();
	}
	
	public Waypoint[] getRoute() {
		return route;
	}
	
	public Vector getDestination() {
		return destination;
	}
	
	public String getDestinationName() {
		return destination_name;
	}
	
	public String getOriginName() {
		return origin_name;
	}
	
	/**
	 * Edits the plane's path by changing the waypoint it will go to at a certain stage in its route.
	 * @param routeStage the stage at which the new waypoint will replace the old.
	 * @param newWaypoint the new waypoint to travel to.
	 */
	public void alterPath(int routeStage, Waypoint newWaypoint) {
		route[routeStage] = newWaypoint;
	}
	
	/**
	 * Calculates optimal distance for a plane - Used for scoring
	 * @return total distance a plane needs to pass based on its flight plan to get to its exit point
	 */
	public int getTotalDistance() {
		int dist = 0;

		for (int i = 0; i < getRoute().length - 1; i++) {
			dist += Waypoint.getCostBetween(getRoute()[i], getRoute()[i + 1]);
		}

		return dist;
	}
	
	/**
	 * Creates a sensible route from an origin to a destination from an array of
	 * waypoints. Waypoint costs are considered according to distance from
	 * current aircraft location Costs are further weighted by distance from
	 * waypoint to destination.
	 * @param origin the waypoint from which to begin.
	 * @param destination the waypoint at which to end.
	 * @param waypoints the waypoints to be used.
	 * @return a sensible route between the origin and the destination, using a sensible amount of waypoint.
	 */
	private Waypoint[] findGreedyRoute(Waypoint origin, Waypoint destination, Waypoint[] waypoints) {
		// To hold the route as we generate it.
		ArrayList<Waypoint> selected_waypoints = new ArrayList<Waypoint>();
		// Will hold the waypoints can be selected and omit all the waypoints
		// that aren't necessary
		ArrayList<Waypoint> remaining_waypoints = new ArrayList<Waypoint>();
		
		double originalCost = 0;
		
		// Add all waypoints to list omitting redundant entry and exit points and the
		// original waypoint
		for (Waypoint wp : waypoints){
			if (!(wp.getLocation().equals(origin.getLocation())) &&
					(wp.getLocation().equals(destination.getLocation()) || !wp.isEntryAndExit()))
			remaining_waypoints.add(wp);
		}
				
		// Initialise the origin as the first point in the route.
		// SelectedWaypoints.add(origin);
		// To track our position as we generate the route. Initialise to the start of the route
		Waypoint current_position = origin;

		// To track the closest next waypoint
		double cost = Double.MAX_VALUE;
		Waypoint cheapest = remaining_waypoints.get(0);
		Waypoint secondCheapest = null;
		// To track if the route is complete
		boolean at_destination = false;

		while (!at_destination) {
			// loop through remaining waypoints finding the two closest waypoints
			// to get to the destination
			for (Waypoint wp: remaining_waypoints) {
				
				originalCost = wp.getDistanceFrom(current_position);
				secondCheapest = cheapest;
				
				if (originalCost < cost){
					// if it is not the destination then select the closest waypoint otherwise select the destination waypoint
					if (!(Waypoint.getCostBetween(current_position, destination) < Waypoint.getCostBetween(wp, destination))) {
						cheapest = wp;
						cost = originalCost;						
					}
				}		
			}
			
			assert cheapest != null;
			
			// assign a random waypoint from the two closest waypoints to the list
			// of waypoints
			if (cheapest.getLocation().equals(destination.getLocation())) {
				at_destination = true;
				selected_waypoints.add(cheapest);
			} else {
				// {!} This may need checking, visually cannot tell if it works
				Random rand = new Random();
				int x = rand.nextInt(1);
				
				Waypoint selected = (x == 1) ? cheapest : secondCheapest;
				selected_waypoints.add(selected);
				current_position = cheapest;
				cost = Double.MAX_VALUE;
				
				// remove a waypoint that has already been selected
				remaining_waypoints.remove(cheapest);
			}		
		} // End while
				
		// Create a Waypoint[] to hold the new route
		Waypoint[] route = new Waypoint[selected_waypoints.size()];
		// Fill route with the selected waypoints
		for (int i = 0; i < selected_waypoints.size(); i++) {
			route[i] = selected_waypoints.get(i);
		}
		return route;
	}
	
	public int indexOfWaypoint(Waypoint waypoint) {
		int index = -1;
		for (int i = 0; i < getRoute().length; i++) {
			if (getRoute()[i] == waypoint) index = i;
		}
		return index;
	}
}