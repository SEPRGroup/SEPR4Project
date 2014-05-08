package cls;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import lib.RandomNumber;
import lib.jog.graphics;
import lib.jog.graphics.Image;

public class TransferBar {
	private static Image aircraftImage;
	private static Image[] clouds;
	
	private int	x, y, width, height;
	private int distance;
	private int difficulty;
	
	private Waypoint
		wLeftTop, wRightTop, wLeftBottom, wRightBottom;
	private Waypoint[]
		wTop, wBottom;
	private ArrayList<Aircraft>
		transferStore = new ArrayList<Aircraft>(),
		transfer = new ArrayList<Aircraft>();
	private Queue<Aircraft>
		left = new LinkedList<Aircraft>(),
		right = new LinkedList<Aircraft>();
	private ArrayList<Feature> features = new ArrayList<Feature>();
	private double wind;

	
	public TransferBar(int x, int y, int width, int height, int distance, int difficulty) {
		if (aircraftImage == null){
			aircraftImage = graphics.newImage("gfx/new/plane.png");
			clouds = new Image[]{
					graphics.newImage("gfx/clouds/cloud1.png"),
					graphics.newImage("gfx/clouds/cloud2.png"),
					graphics.newImage("gfx/clouds/cloud3.png"),
					graphics.newImage("gfx/clouds/cloud4.png"),
					graphics.newImage("gfx/clouds/cloud5.png")
			};
		}
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.distance = distance;
		this.difficulty = difficulty;
		
		wLeftTop = new Waypoint(0, height/4, true, "Left-Top");
		wLeftBottom = new Waypoint(0, height*3/4, true, "Left-Bottom");
		wRightTop = new Waypoint(width, height/4, true, "Right-Top");
		wRightBottom = new Waypoint(width, height*3/4, true, "Right-Bottom");
		wTop = new Waypoint[] { wLeftTop, wRightTop };
		wBottom = new Waypoint[] { wLeftBottom, wRightBottom };
		
		//wind = Math.random()*40 -10;
		wind = 20;
		//generate initial clouds
		for (int i=0; i<150; i++ ){
			Image image = clouds[RandomNumber.randInclusiveInt(0, clouds.length-1)];
			int	w = (int)Math.ceil(image.width()),
				h = (int)Math.ceil(image.height());
			double scale = ((double)height/h) * (Math.random() +1);
			double scaleDrift = (Math.random()*0.05 -0.025)*scale;
			double xPos = Math.random() * (width +2*w*scale) -w*scale;
			double yPos = (height + 2*h*scale)*Math.random()
					-h*scale;
			double yDrift = Math.random()*0.02*height -0.01*height;
			
			features.add(new Feature(image, scale, scaleDrift,
					xPos, yPos, yDrift));
		}
	}
	
	
	public void update(double timeDifference){
		for (int i=transfer.size()-1; i>=0; i--){
			Aircraft b = transfer.get(i);
			b.update(timeDifference);

			//handle completed transfer path
			if (b.isFinished()){
				Aircraft a = transferStore.remove(i);
				transfer.remove(i);
				if (b.getFlightPlan().getOrigin() == wLeftTop){
					right.add(a);
				} else {
					left.add(a);
				}
			}
		}

		//wind += ( (float)Math.cos(wind/7) +(float)Math.sin(wind/13) ) *20 *timeDifference;
		for (int i=features.size()-1; i>=0; i--){
			Feature f = features.get(i);
			f.scale += f.scaleDrift * timeDifference;
			f.x += wind * timeDifference;
			f.y += f.yDrift * timeDifference;

			//remove features that are no longer visible
			if (f.x -f.image.width()*f.scale > width+20){
				features.remove(i);
				continue;
			}
			if (f.x +f.image.width()*f.scale +20 < 0){
				features.remove(i);
				continue;
			}

		}

		//generate a new feature ~ every second
		if (Math.random() < timeDifference){
			Image image = clouds[RandomNumber.randInclusiveInt(0, clouds.length-1)];
			int	w = (int)Math.ceil(image.width()),
				h = (int)Math.ceil(image.height());
			double scale = ((double)height/h) * (Math.random() +1);
			double scaleDrift = (Math.random()*0.05 -0.025)*scale;
			double xPos = (wind > 0) ? -w*scale : width +w*scale;
			double yPos = (height + 2*h*scale)*Math.random()
					-h*scale;
			double yDrift = Math.random()*0.02*height -0.01*height;

			Feature f = new Feature(image, scale, scaleDrift,
					xPos, yPos, yDrift);
			features.add(f);
			//System.out.println(features.size() +"+\t" +f.toString());
		}

	}
	
	
	public void draw(){
		graphics.setViewport(x, y, width, height);
		
		//draw background
		graphics.setColour(100, 200, 220);
		graphics.rectangle(true, 0, 0, width, height);
		
		//draw features{
		graphics.setColour(255,255,255, 128);
		for (Feature f : features){
			double
				ox = f.image.width()/2,
				oy = f.image.height()/2;
			graphics.draw(f.image, f.scale, f.x, f.y, 0, ox, oy);
			//graphics.rectangle(false, f.x-(ox*f.scale), f.y-(oy*f.scale), ox*2*f.scale, oy*2*f.scale);
		}
		
		//draw
		for (Aircraft b : transfer){
			b.draw();
		}
		
		//draw outline
		graphics.setColour(graphics.white);
		graphics.rectangle(false, 1, 1, width-1, height-1);
		
		graphics.setViewport();
	}
	
	
	public void enterLeft(Aircraft a){
		//generate internal version of Aircraft to show
		Aircraft b = new Aircraft(a.getName(), 
				wRightTop, wLeftTop, aircraftImage, height / (Aircraft.RADIUS*6),
				(a.getSpeed()*width)/distance,	//scale speed to match scale of bar
				wTop, difficulty);	//limit waypoints to force flightplan
		b.getPosition().setZ( a.getPosition().getZ() );
		
		transferStore.add(a);
		transfer.add(b);
	}
	
	
	public void enterRight(Aircraft a){
		//generate internal version of Aircraft to show
		Aircraft b = new Aircraft(a.getName(), 
				wLeftBottom, wRightBottom, aircraftImage, height / (Aircraft.RADIUS*6),
				(a.getSpeed()*width)/distance,	//scale speed to match scale of bar
				wBottom, difficulty);	//limit waypoints to force flightplan
		b.getPosition().setZ( a.getPosition().getZ() );
		
		transferStore.add(a);
		transfer.add(b);
	}
	
	
	public Aircraft pollLeft(){
		return left.poll();
	}
	
	
	public Aircraft pollRight(){
		return right.poll();
	}
	
	
	public void clearLeft(){
		left.clear();
	}
	
	public void clearRight(){
		right.clear();
	}

	
	private class Feature{
		public final Image image;
		public double scale;
		public double x, y;
		public double yDrift, scaleDrift;
		
		private Feature(Image image){
			this.image = image;
		}
		
		private Feature(Image image, double scale, double scaleDrift, double x, double y, double yDrift){
			this.image = image;
			this.scale = scale;
			this.scaleDrift = scaleDrift;
			this.x = x;
			this.y = y;
			this.yDrift = yDrift;
		}

		@Override
		public String toString() {
			return "Feature [scale=" + scale + ", x=" + x
					+ ", y=" + y + ", yDrift=" + yDrift + ", scaleDrift="
					+ scaleDrift + "]";
		}
	}

}
