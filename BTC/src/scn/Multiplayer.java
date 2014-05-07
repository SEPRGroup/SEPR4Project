package scn;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import svc.NetworkIO;

import cls.Aircraft;
import cls.GameWindow;
import cls.TransferBar;
import cls.Vector;
import cls.Waypoint;
import cls.GameWindow.TransferBuffer;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics.Image;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;


public class Multiplayer extends Scene {
	static final int
		TRANSFER  = 1,
		GAMESTATE = 2;
	
	static Image aircraftImage;
	

	/**Music to play during the game scene*/
	private audio.Music music;
	
	private int
		oldWidth, oldHeight;
	private final int difficulty;
	
	private NetworkIO network;
		
	private cls.GameWindow 
		game1, game2;
	private TransferBar transfers;
	private double sinceSync = 0;

	
	public Multiplayer(Main main, int difficulty, NetworkIO establishedConnection) {
		super(main);
		if (aircraftImage == null){
			aircraftImage = graphics.newImage("gfx" +File.separator +"plane.png");
		}
		
		this.difficulty = difficulty;
		network = establishedConnection;
		
		music = audio.newMusic("sfx" + File.separator + "Gypsy_Shoegazer.ogg");
	}
	
	
	@Override
	public void start() {
		oldWidth = window.width();
		oldHeight = window.height();
		window.setSize(Main.TARGET_WIDTH*3 /2, Main.TARGET_HEIGHT);
		graphics.resize();
		
		int	w = window.width(),
			h = window.height(),
			spacing = 8;
		int	gw = (w -3*spacing) /2,
			gh = h -150,
			tw = w -2*spacing,
			th = h -gh -3*spacing;

		game1 = new GameWindow(spacing, spacing, gw, gh, difficulty);
		game2 = new GameWindow((w +spacing)/2, spacing, gw, gh, difficulty);
		transfers = new TransferBar(spacing, gh +2*spacing, tw, th, 2500, difficulty);
		
		music.play();
		game1.setControllable(true);

	}


	@Override
	public void close() {
		music.stop();
		//if game is still running, restore size.
			//otherwise, closing will be faster
		if (!window.isClosed()){
			window.setSize(oldWidth, oldHeight);
			graphics.resize();
		}
	}

		
	@Override
	public void update(double timeDifference) {
		//gameover checks
		
		if (!game1.isGameOver()) game1.update(timeDifference);
		if (!game2.isGameOver()) game2.update(timeDifference);
		
		{	//handle transfers out
			for (TransferBuffer tb : game1.transfers){
				if (tb.name == "City of Rightson"){
					Aircraft a = tb.pollOut();
					while (a != null) {
						System.out.println(a.getName() +" transferred out");
						transfers.enterLeft(a);
						network.sendObject(new MultiplayerPacket(new AircraftPacket(a)));
						a = tb.pollOut();
					};
				}
				else tb.clearOut();
			}
		}
		
		transfers.update(timeDifference);
		
		{	//handle transfers in
			Aircraft a = transfers.pollLeft();
			while (a != null) {
				for (TransferBuffer tb : game1.transfers){
					if (tb.name == "South Sea"){
						System.out.println(a.getName() +" transferred in");
						tb.transferIn(a);
						break;
					}
				}
				a = transfers.pollLeft();
			}
			transfers.clearRight();
		}
		
		{	//handle any network events
			Object o = network.pollObjects();
			while (o != null){
				MultiplayerPacket mp = (MultiplayerPacket) o;
				
				switch (mp.code){
				case TRANSFER:
					AircraftPacket ap = (AircraftPacket) mp.contents;
					Aircraft air = AircraftPacket.fromPacket(ap, game1.getScale(),difficulty);
					air.getPosition().setVector(ap.position.getX(),ap.position.getY(),ap.position.getZ());
					transfers.enterRight(air);
					break;
				case GAMESTATE:
					AircraftPacket[] airspace = (AircraftPacket[]) mp.contents;
					List<Aircraft> as = game2.getAircraftList();
					as.clear();
					for(int i=0; i<airspace.length; i++){
						Aircraft a = AircraftPacket.fromPacket(airspace[i], game1.getScale(),difficulty);
						a.getPosition().setVector(airspace[i].position.getX(), airspace[i].position.getY(), airspace[i].position.getZ());
						as.add(a);
					}
					break;
				}
				o = network.pollObjects();
			}
		}
		
		{	
			//synchronize game
			sinceSync += timeDifference;
			if(sinceSync > 0.1){
				System.out.println("Syncing");
				AircraftPacket[] airspace = new AircraftPacket[game1.getAircraftList().size()];
				List<Aircraft> as = game1.getAircraftList();
				for(int i=0; i<as.size(); i++){
					airspace[i] = new AircraftPacket(as.get(i));
				}
				network.sendObject(new MultiplayerPacket(airspace));
				sinceSync -= 0.1;
			}	
		}

		//synchronize scores
		//talk to network stuff
	}

	
	@Override
	public void draw() {
		game1.draw();
		game2.draw();
		transfers.draw();
	}
		

	@Override
	public void mousePressed(int key, int x, int y) {
		game1.mousePressed(key, x, y);
	}

	
	@Override
	public void mouseReleased(int key, int x, int y) {
		game1.mouseReleased(key, x, y);
	}

	
	@Override
	public void keyPressed(int key) {
		game1.keyPressed(key);
	}

	
	@Override
	public void keyReleased(int key) {
		switch (key) {
		case input.KEY_ESCAPE :
			main.closeScene();
			break;
		}

		game1.keyReleased(key);
	}
	
	
	@Override
	public void playSound(Sound sound) {
		sound.stop();
		sound.play();
		
	}

	

}



class AircraftPacket implements Serializable{
	/**
	 * 
	 */
	static final long serialVersionUID = 2494803046641211835L;
	String name;
	Waypoint destination_point;
	Waypoint origin_point;
	double speed;
	Vector position;
	double bearing;
	Waypoint[] flightPlan;

	AircraftPacket(Aircraft aircraft){
		name = aircraft.getName();
		Waypoint o = aircraft.getFlightPlan().getDestination();
		destination_point = new Waypoint(o.getLocation().getX(),o.getLocation().getY(),true,o.name) ;;
		o = aircraft.getFlightPlan().getOrigin();
		origin_point = new Waypoint(o.getLocation().getX(),o.getLocation().getY(),true,o.name) ;
		speed = aircraft.getSpeed();
		position = new Vector(aircraft.getPosition());
		bearing = aircraft.getBearing();
		//flightPlan = aircraft.getFlightPlan().getRoute().clone();
		System.out.println();
	}
	
	static Aircraft fromPacket(AircraftPacket ap, double scale, int difficulty){
		Aircraft a = new Aircraft(ap.name,
				ap.destination_point, ap.origin_point,
				Multiplayer.aircraftImage,
				scale, ap.speed,
				new Waypoint[]{ap.origin_point, ap.destination_point},
				difficulty);
		
		a.initialize(ap.position, ap.bearing);
		return a;
	}

}

class MultiplayerPacket implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2463331822414916107L;
	final int code;
	final Object contents;	

	MultiplayerPacket(int code, Object contents){
		this.code = code;
		this.contents = contents;
	}

	MultiplayerPacket(AircraftPacket transferAircraft){
		this.code = Multiplayer.TRANSFER;
		this.contents = transferAircraft;
	}
	
	MultiplayerPacket(AircraftPacket[] transferAircraft){
		this.code = Multiplayer.GAMESTATE;
		this.contents = transferAircraft;
	} 

}
