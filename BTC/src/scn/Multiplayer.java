package scn;

import java.io.File;
import java.io.Serializable;

import svc.NetworkIO;

import cls.Aircraft;
import cls.GameWindow;
import cls.TransferBar;
import cls.Waypoint;
import cls.GameWindow.TransferBuffer;
import lib.jog.audio;
import lib.jog.audio.Sound;
import lib.jog.graphics;
import lib.jog.input;
import lib.jog.window;
import btc.Main;


public class Multiplayer extends Scene {
	static final int
		TRANSFER = 1;

	
	/**Music to play during the game scene*/
	private audio.Music music;
	
	private int
		oldWidth, oldHeight;
	private final int difficulty;
	
	private NetworkIO network;
		
	private cls.GameWindow 
		game1, game2;
	private TransferBar transfers;

	
	public Multiplayer(Main main, int difficulty, NetworkIO establishedConnection) {
		super(main);
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
		
		game1.update(timeDifference);
		game2.update(timeDifference);
		
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
					AircraftPacket a = (AircraftPacket) mp.contents;
					Aircraft air = new Aircraft(a.name,
							a.destination_point, a.origin_point,
							null,
							12.0, a.speed,
							new Waypoint[]{a.origin_point, a.destination_point},
							difficulty);
					air.getPosition().setZ( a.altitude);
					transfers.enterRight(air);
					break;
				}
				o = network.pollObjects();
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
	double altitude;

	AircraftPacket(Aircraft aircraft){
		name = aircraft.getName();
		destination_point = aircraft.getFlightPlan().getDestination();
		origin_point = aircraft.getFlightPlan().getOrigin();
		speed = aircraft.getSpeed();
		altitude = aircraft.getPosition().getZ();

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

}
