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
	
	private static final PrintStream out = System.out;
	/**
	 * list of connected clients. any reads or writes to this list should be synchronized
	 */
	private static final List<ClientThread> clients = new ArrayList<ClientThread>();
	
	private static final MapBgObject map = new MapBgObject();
	
	private static long startTime;

	public static void main(String[] args) throws Exception {
		map.basic();
		startTime = System.nanoTime();
		ServerSocket serverSocket = new ServerSocket(8111);
		
		while (true) {
			out.println("listening on " + serverSocket);
			Socket socket = serverSocket.accept();
			out.println("accepted " + socket);
			socket.setKeepAlive(true);
			//out.println("  keepalive: " + socket.getKeepAlive() + " nodelay: " + socket.getTcpNoDelay() + " timeout: " + socket.getSoTimeout());
			ClientThread client = new ClientThread(socket);
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
		sendAll(ClientThread.ENTER, client.id);
	}
	
	/** tell all clients the client has begun spectating */
	public static void clientSpec(ClientThread client) {
		client.entered = false;
		sendAll(ClientThread.SPEC, client.id);
	}
	
	/** update the state of the client object on the other clients */
	public static void clientUpdate(ClientThread client, String data) {
		client.lastData = data;
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					c.send(ClientThread.UPDATEOBJ , client.id, data);
				}
			}
		}
	}
	
	/** send current state of server to new client */
	public static void clientInit(ClientThread client) {
		client.send(ClientThread.ID, client.id);
		client.send(ClientThread.TIME, time());
		
		// send map
		client.send(ClientThread.MAPDATA, map.write(new StringBuilder()));
		
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client && c.entered && c.lastData != null) {
					client.send(ClientThread.ENTER, c.id);
					// FIXME last data might be very old, really need to just force all clients to send update
					// could do that with a new connect command
					client.send(ClientThread.UPDATEOBJ, c.id, c.lastData);
				}
			}
		}
	}
	
	/** tell client to update map tile */
	public static void clientUpdateMap(ClientThread client, int x, int y, int act) {
		map.place(x, y, act);
		sendAll(ClientThread.MAPTILE, x, y, act);
	}
	
	public static void clientFireReq(ClientThread client, String data) {
		synchronized (clients) {
			for (ClientThread c : clients) {
				if (c != client) {
					c.send(ClientThread.FIRE, data);
				}
			}
		}
	}
	
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
	public static void removeClient(ClientThread client) {
		synchronized (clients) {
			out.println("removing " + client);
			clients.remove(client);
			sendAll(ClientThread.SPEC, client.id);
		}
	}
	
	
}
