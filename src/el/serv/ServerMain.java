package el.serv;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * electron server
 */
public class ServerMain {
	
	private static final PrintStream out = System.out;
	private static final List<ClientThread> clients = new ArrayList<ClientThread>();
	
	// TODO map
	
	private static long startTime;

	public static void main(String[] args) throws Exception {
		startTime = System.nanoTime();
		ServerSocket serverSocket = new ServerSocket(8111);
		
		while (true) {
			out.println("listening: " + serverSocket);
			Socket socket = serverSocket.accept();
			out.println("accepted: " + socket);
			socket.setKeepAlive(true);
			//out.println("  keepalive: " + socket.getKeepAlive() + " nodelay: " + socket.getTcpNoDelay() + " timeout: " + socket.getSoTimeout());
			ClientThread client = new ClientThread(socket);
			clients.add(client);
			client.start();
		}
	}
	
	public static void clientEnterReq(ClientThread client) {
		// allow client to enter game
		// tell all clients
		client.entered = true;
		sendAll(ClientThread.ENTER + " " + client.id);
	}
	
	/** tell all clients the client has begun spectating */
	public static void clientSpec(ClientThread client) {
		client.entered = false;
		sendAll(ClientThread.SPEC + " " + client.id);
	}
	
	/** update the state of the client object on the other clients */
	public static void clientUpdate(ClientThread client, String data) {
		client.lastData = data;
		for (ClientThread c : clients) {
			if (c != client) {
				//c.send(ClientThread.TIME + " " + time());
				c.send(ClientThread.UPDATEOBJ + " " + client.id + " " + data);
			}
		}
	}
	
	/** send current state of server to new client */
	public static void clientInit(ClientThread client) {
		client.send(ClientThread.ID + " " + client.id);
		client.send(ClientThread.TIME + " " + time());
		
		// TODO send map
		
		for (ClientThread c : clients) {
			if (c != client && c.entered && c.lastData != null) {
				client.send(ClientThread.ENTER + " " + c.id);
				client.send(ClientThread.UPDATEOBJ + " " + c.id + " " + c.lastData);
			}
		}
	}
	
	private static long time() {
		return System.nanoTime() - startTime;
	}
	
	private static void sendAll(String line) {
		for (ClientThread c : clients) {
			//c.send(ClientThread.TIME + " " + time());
			c.send(line);
		}
	}
	
	
}
