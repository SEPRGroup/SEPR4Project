package svc;

import java.text.ParseException;

public class LobbyInfo {
	public String name;
	public String description;
	public int difficulty;

	public LobbyInfo(){
		name = null;
		description = null;
		difficulty = -1;
	};

	public LobbyInfo(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public LobbyInfo(String commandString) throws Exception{
		String[] data = commandString.split("@");
		try {
			name = data[0];
			description = data[1];
			difficulty = Integer.valueOf(data[2]);
		}
		catch (Exception e){
			throw new Exception("Failed to interpret commandString as a LobbyInfo instance");
		}
	}
	
	public String getCommandString(){
		return name +"@"
				+description +"@"
				+Integer.toString(difficulty);
	}

	@Override
	public String toString() {
		return "LobbyInfo [name=" +name +", description=" +description
				+", difficulty=" +difficulty +"]";
	}

}
