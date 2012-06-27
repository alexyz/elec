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
	/**
	 * list of connected clients. any reads or writes to this list should be synchronized
	 */
	private static final List<ClientThread> clients = new ArrayList<ClientThread>();
	/**
	 * Current map state
	 */
	private static final MapBgObject map = new MapBgObject();
	
	//
	// mutable fields
	//
	
	private static int nextClientId = 1000;
	
	private static long startTime;

	public static void main(String[] args) throws Exception {
		map.init();
		startTime = System.nanoTime();
		ServerSocket serverSocket = new ServerSocket(8111);
		
		while (true) {
			out.println("listening on " + serverSocket);
			Socket socket = serverSocket.accept();
			out.println("accepted " + socket);
			socket.setKeepAlive(true);
			//TODO
			//socket.setSoTimeout(5000);
			//out.println("  keepalive: " + socket.getKeepAlive() + " nodelay: " + socket.getTcpNoDelay() + " timeout: " + socket.getSoTimeout());
			ClientThread client = new ClientThread(socket, nextClientId++);
			client.start();
			synchronized (clients) {
				out.println("adding " + client);
				clients.add(client);
			}
		}
	}
	
	public static void clientEnterReq(ClientThread client) {
		// allow client to enter game
		// tell all clients
		client.entered = true;
		sendAll(ClientCommands.ENTER, client.id);
	}
	
	/** tell all clients the client has begun spectating */
	public static void clientSpec(ClientThread client) {
		client.entered = false;
		sendAll(ClientCommands.SPEC, client.id);
	}
	
	/** update the state of the client object on the other clients */
	public static void clientUpdate(ClientThread client, String data) {
		client.lastData = data;
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					c.send(ClientCommands.UPDATEOBJ , client.id, data);
				}
			}
		}
	}
	
	/** send current state of server to new client */
	public static void clientInit(ClientThread client) {
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
						client.send(ClientCommands.ENTER, c.id);
					}
				}
			}
		}
		
	}
	
	/** tell client to update map tile */
	public static void clientMapTileReq(ClientThread client, int x, int y, int act) {
		map.place(x, y, act);
		sendAll(ClientCommands.MAPTILE, x, y, act);
	}
	
	/**
	 * consider fire request and send to clients
	 */
	public static void clientFireReq(ClientThread client, String data) {
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					c.send(ClientCommands.FIRE, data);
				}
			}
		}
	}
	
	/**
	 * get current server time
	 */
	private static long time() {
		return System.nanoTime() - startTime;
	}
	
	/** send command to all clients */
	private static void sendAll(String command, Object... args) {
		synchronized (clients) {
			for (ClientThread c : clients) {
				c.send(command, args);
			}
		}
	}

	/** remove client after disconnection */
	public static void clientExit(ClientThread client) {
		synchronized (clients) {
			out.println("removing " + client);
			clients.remove(client);
			sendAll(ClientCommands.EXIT, client.id);
		}
	}

	/**
	 * consider talk request and send to clients
	 */
	public static void clientTalkReq(ClientThread client, String str) {
		sendAll(ClientCommands.TALK, client.id, str);
	}
	
	
}
