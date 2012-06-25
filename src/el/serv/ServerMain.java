package el.serv;

import java.util.*;
import java.io.*;
import java.net.*;

import el.bg.BgObject;
import el.fg.FgObject;

/**
 * electron server
 */
public class ServerMain {
	
	private static final PrintStream out = System.out;
	private static final List<ClientThread> clients = new ArrayList<ClientThread>();
	
	private static final ArrayList<BgObject> bgObjects = new ArrayList<BgObject>();
	
	// TODO list of last update strings for each object
	//private static final ArrayList<FgObject> objects = new ArrayList<FgObject>();
	
	private static long startTime;

	public static void main(String[] args) throws Exception {
		startTime = System.nanoTime();
		ServerSocket serverSocket = new ServerSocket(8111);
		
		while (true) {
			out.println("listening: " + serverSocket);
			Socket socket = serverSocket.accept();
			out.println("accepted: " + socket);
			ClientThread client = new ClientThread(socket);
			clients.add(client);
			client.start();
		}
	}
	
	public static void enterReq(ClientThread client) {
		// allow client to enter game
		// tell all clients
		sendAll(ClientThread.ENTER + " " + client.id);
	}
	
	/** tell all clients the client has begun spectating */
	public static void spec(ClientThread client) {
		sendAll(ClientThread.SPEC + " " + client.id);
	}
	
	/** update the state of the client object on the other clients */
	public static void update(ClientThread client, String objstr) {
		for (ClientThread c : clients) {
			if (c != client) {
				c.send(ClientThread.TIME + " " + time());
				c.send(ClientThread.UPDATEOBJ + " " + client.id + " " + objstr);
			}
		}
	}
	
	private static long time() {
		return System.nanoTime() - startTime;
	}
	
	private static void sendAll(String line) {
		for (ClientThread c : clients) {
			c.send(ClientThread.TIME + " " + time());
			c.send(line);
		}
	}
	
	
}
