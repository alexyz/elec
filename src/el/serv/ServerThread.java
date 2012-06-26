package el.serv;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

import el.Model;
import el.fg.Bullet;
import el.fg.Ship;
import el.fg.ShipType;

/**
 * A client connection to the server
 */
public class ServerThread extends Thread {
	
	/** ask server if we can enter (void) */
	public static final String ENTERREQ = "enter-req";
	/** tell server we are going to spectate (void) */
	public static final String SPEC = "spec";
	/** update details of players ship */
	public static final String UPDATE = "update";
	/** update map tile request */
	public static final String MAPTILEREQ = "map-tile-req";
	/** fire transient */
	public static final String FIREREQ = "fire-req";
	
	private static final PrintStream out = System.out;
	
	private final BufferedReader serverIn;
	private final PrintStream serverOut;
	private final Model model;
	
	private int id;
	private long serverTime, clientTime;
	
	public ServerThread(Socket socket, Model model) throws Exception {
		setPriority(Thread.NORM_PRIORITY);
		setDaemon(true);
		this.model = model;
		serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		serverOut = new PrintStream(socket.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			String line;
			while ((line = serverIn.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(line);
				String cmd = tokens.nextToken();
				out.println("server: " + line);
				
				if (cmd.equals(ClientThread.UPDATEOBJ)) {
					int id = Integer.parseInt(tokens.nextToken());
					synchronized (model) {
						model.updateFgObject(id, tokens);
					}
					
				} else if (cmd.equals(ClientThread.FIRE)) {
					Bullet bullet = new Bullet(tokens);
					model.addTransObject(bullet, false);
					
				} else if (cmd.equals(ClientThread.TIME)) {
					// TODO measure latency from connect to here
					long t = Long.parseLong(tokens.nextToken());
					serverTime = t;
					clientTime = System.nanoTime();
					// update here to refresh time?
					model.update();
					
				} else if (cmd.equals(ClientThread.ENTER)) {
					int id = Integer.parseInt(tokens.nextToken());
					if (this.id == id) {
						out.println("will enter");
						synchronized (model) {
							model.enter(id);
						}
						
					} else {
						out.println("someone else entered");
						synchronized (model) {
							model.addFgObject(new Ship(ShipType.types[0], Model.centrex, Model.centrey), id);
						}
					}
					
				} else if (cmd.equals(ClientThread.SPEC)) {
					int id = Integer.parseInt(tokens.nextToken());
					if (this.id == id) {
						out.println("spectate self");
					} else {
						out.println("spectate other");
					}
					// remove from model
					synchronized (model) {
						model.spec(id);
					}
					
				} else if (cmd.equals(ClientThread.ID)) {
					id = Integer.parseInt(tokens.nextToken());
					
				} else if (cmd.equals(ClientThread.MAPDATA)) {
					out.println("update map");
					model.getMap().read(tokens);
					
				} else if (cmd.equals(ClientThread.MAPTILE)) {
					out.println("map tile");
					int x = Integer.parseInt(tokens.nextToken());
					int y = Integer.parseInt(tokens.nextToken());
					int act = Integer.parseInt(tokens.nextToken());
					model.getMap().place(x, y, act);
					
				} else {
					out.println("unknown server message: " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			model.setServer(null);
		}
	}
	
	/**
	 * Get current server time (doesn't call server)
	 */
	public long getTime() {
		long t = System.nanoTime();
		return serverTime + t - clientTime;
	}
	
	/**
	 * Send enter request to server
	 */
	public void enterReq() {
		send(ENTERREQ);
	}

	/**
	 * Send spectate to server
	 */
	public void spec() {
		send(SPEC);
	}
	
	/**
	 * send map update request to server
	 */
	public void updateMapReq(int x, int y, int act) {
		send(MAPTILEREQ, x, y, act);
	}
	
	public void fireReq(Bullet bullet) {
		send(FIREREQ, bullet.write(new StringBuilder()));
	}

	/**
	 * Send ship update to server
	 */
	public void update(Ship ship) {
		send(UPDATE, ship.write(new StringBuilder()));
	}
	
	/** send command to server */
	private void send(String command, Object... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(command);
		for (Object o : args) {
			sb.append(" ");
			sb.append(o);
		}
		serverOut.println(sb);
		serverOut.flush();
	}
	
}
