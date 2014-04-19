package cls;

import java.awt.Rectangle;

public class GameWindow {
			
	/*
	private static int airspace_view_offset_x = 16;
	public static int airspace_view_offset_y = 48;
		=> x, y
	*/
	
	// Position of things drawn to window  
	
	private int x, y, width, height;
	private final Rectangle
		planeInfo, altimeter, airportControl, ordersBox;
	
	/** Sets up control areas based on x, y, width, height */
	private void setAreas(){
		//precalculate control position increments
		int cSpacing = 8,
			cWidth = width -(cSpacing*7),
			cHeight = 112,
			cY = height -(cHeight +cSpacing);
		
		planeInfo.setRect(cSpacing*2, cY, 
				cWidth/4, cHeight );
		altimeter.setRect(cSpacing*3 +cWidth/4, cY, 
				cWidth/5, cHeight );
		airportControl.setRect(cSpacing*4 +(cWidth*9/20), cY, 
				cWidth/5, cHeight );
		ordersBox.setRect(cSpacing*5 +(cWidth*13/20), cY, 
				cWidth*7/20, cHeight );
	}

	
	public GameWindow(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		//set up window controls
		planeInfo = new Rectangle();
		altimeter = new Rectangle();
		airportControl = new Rectangle();
		ordersBox = new Rectangle();
		setAreas();
		
	}

}
