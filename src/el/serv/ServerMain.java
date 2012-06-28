package el.serv;

import java.util.*;
import java.io.*;
import java.net.*;

import el.bg.MapBgObject;

/**
 * electron server
 * 
 * General process of a client->server->client cycle is:
 * 
 * (c) Model/UI calls ServerThread.enterReq()
 * (c) ServerThread writes "enter-req" command to server
 * ~
 * (s) ClientThread reads command
 * (s) ClientThread calls ServerMain.clientEnterReq()
 * (s) ServerMain considers and sends "enter [id]" to all clients
 * ~
 * (c) ServerThread reads command
 * (c) ServerThread calls Model.enter(id)
 */
public class ServerMain {
	
	//
	// final fields
	//
	
	private static final PrintStream out = System.out;
	
	public static void main(String[] args) throws Exception {
		ServerMain server = new ServerMain();
		server.run(8111);
	}
	
	//
	// instance
	//
	
	/**
	 * list of connected clients. any reads or writes to this list should be synchronized
	 */
	private final List<ClientThread> clients = new ArrayList<ClientThread>();
	/**
	 * Current map state
	 */
	private final MapBgObject map = new MapBgObject();
	
	//
	// mutable fields
	//
	
	private int nextClientId = 1000;
	
	private long startTime;
	
	private int maxFreqs = 3;

	/**
	 * Create a new server
	 */
	public ServerMain() {
		map.init();
		startTime = System.nanoTime();
	}
	
	/**
	 * Run the server
	 * This method never exits except on server failure
	 */
	private void run(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while (true) {
				out.println("listening on " + serverSocket);
				Socket socket = serverSocket.accept();
				out.println("accepted " + socket);
				socket.setKeepAlive(true);
				//TODO do this to disconnect silent clients
				//socket.setSoTimeout(10000);
				ClientThread client = new ClientThread(this, socket, nextClientId++);
				client.start();
				synchronized (clients) {
					out.println("adding " + client);
					clients.add(client);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}
	
	/**
	 * client requests enter game, assign a freq and inform other clients
	 */
	public void clientEnterReq(ClientThread client) {
		// count numbers on each frequency
		int[] freqCount = new int[maxFreqs];
		synchronized (clients) {
			for (ClientThread c : clients) {
				freqCount[c.freq]++;
			}
		}
		
		// find smallest frequency
		int minFreq = 0;
		int minCount = Integer.MAX_VALUE;
		for (int f = 1; f < freqCount.length; f++) {
			if (freqCount[f] < minCount) {
				minCount = freqCount[f];
				minFreq = f;
			}
		}
		
		client.freq = minFreq;
		client.entered = true;
		sendAll(ClientCommands.ENTER, client.id, minFreq);
	}
	
	/** tell all clients the client has begun spectating */
	public void clientSpec(ClientThread client) {
		client.entered = false;
		client.freq = 0;
		sendAll(ClientCommands.SPEC, client.id);
	}
	
	/** update the state of the client object on the other clients */
	public void clientUpdate(ClientThread client, String data) {
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					c.send(ClientCommands.UPDATEOBJ , client.id, data);
				}
			}
		}
	}
	
	/** send current state of server to new client */
	public void clientInit(ClientThread client) {
		client.send(ClientCommands.ID, client.id);
		client.send(ClientCommands.TIME, time());
		
		// send map
		client.send(ClientCommands.MAPDATA, map.write(new StringBuilder()));
		
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					// introduce clients to each other
					c.send(ClientCommands.CONNECTED, client.id, client.name);
					client.send(ClientCommands.CONNECTED, c.id, c.name);
					
					if (c.entered) {
						client.send(ClientCommands.ENTER, c.id, c.freq);
					}
				}
			}
		}
		
	}
	
	/** tell client to update map tile */
	public void clientMapTileReq(ClientThread client, int x, int y, int act) {
		map.updateMapTile(x, y, act);
		sendAll(ClientCommands.MAPTILE, x, y, act);
	}
	
	/**
	 * consider fire request and send to clients
	 */
	public void clientFireReq(ClientThread client, String data) {
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					c.send(ClientCommands.FIRE, client.freq, data);
				}
			}
		}
	}
	
	/**
	 * get current server time
	 */
	private long time() {
		return System.nanoTime() - startTime;
	}
	
	/** send command to all clients */
	private void sendAll(String command, Object... args) {
		synchronized (clients) {
			for (ClientThread c : clients) {
				c.send(command, args);
			}
		}
	}

	/** remove client after disconnection */
	public void clientExit(ClientThread client) {
		synchronized (clients) {
			out.println("removing " + client);
			clients.remove(client);
			sendAll(ClientCommands.EXIT, client.id);
		}
	}

	/**
	 * consider talk request and send to clients
	 */
	public void clientTalkReq(ClientThread client, String str) {
		sendAll(ClientCommands.TALK, client.id, str);
	}
	
	
}
