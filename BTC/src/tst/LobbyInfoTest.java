package tst;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import svc.LobbyInfo;

public class LobbyInfoTest {

	private LobbyInfo testLobbyInfo;
	
	@Before
	public void setUp() throws Exception {
		testLobbyInfo = new LobbyInfo("testLobby", "a Description", 1);
	}

	@After
	public void tearDown() throws Exception {
		testLobbyInfo = null;
	}
	
	
	@Test
	public void testLobbyInfo() {
		testLobbyInfo = new LobbyInfo();
		String s = "LobbyInfo [name=null, description=null, difficultyto=-1]";
		assertEquals(s, testLobbyInfo.toString());
	}
	
	@Rule public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testLobbyInfoString() throws Exception {
		testLobbyInfo = new LobbyInfo("name@description@1");
		String s = "LobbyInfo [name=name, description=description, difficultyto=1]";
		assertEquals(s, testLobbyInfo.toString());
		
		thrown.expect(Exception.class);
		thrown.expectMessage("Failed to interpret commandString as a LobbyInfo instance");
		
		testLobbyInfo = new LobbyInfo("blahbl@ahdf@hgdf");
		testLobbyInfo = new LobbyInfo("blahblah1");	
	}

	@Test
	public void testGetCommandString() {
		String commandString = "testLobby" + "@"
					+ "a Description" + "@" + Integer.toString(1);
		assertEquals(commandString, testLobbyInfo.getCommandString());
	
	}

	@Test
	public void testToString() {
		String s = "LobbyInfo [name=testLobby, description=a Description, difficultyto=1]";
		assertEquals(s, testLobbyInfo.toString()); 
	}

}
