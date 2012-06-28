package el;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import el.fg.BulletObject;
import el.fg.ShipObject;
import el.serv.ClientCommands;
import el.serv.ServerCommands;

/**
 * A client connection to the server
 */
public class ServerRunnable implements Runnable {

	private static final PrintStream out = System.out;

	//
	// final fields
	//

	private final Socket socket;
	private final BufferedReader serverIn;
	private final PrintStream serverOut;
	private final Model model;
	private final String name;

	//
	// mutable fields
	//

	private int id;
	private long serverTime, clientTime;

	public ServerRunnable(Socket socket, Model model, String name) throws Exception {
		this.socket = socket;
		this.model = model;
		this.name = name;
		serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		serverOut = new PrintStream(socket.getOutputStream());
	}

	@Override
	public void run() {
		try {
			// have to send name on connect
			sendName(name);

			// read commands from server...
			String line;
			while ((line = serverIn.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(line);
				String cmd = tokens.nextToken();
				out.println("server: " + line);

				if (cmd.equals(ClientCommands.UPDATEOBJ)) {
					int id = Integer.parseInt(tokens.nextToken());
					synchronized (model) {
						model.updateFgObject(id, tokens);
					}

				} else if (cmd.equals(ClientCommands.FIRE)) {
					int freq = Integer.parseInt(tokens.nextToken());
					BulletObject bullet = new BulletObject(tokens);
					bullet.setFreq(freq);
					synchronized (model) {
						model.addTransObject(bullet, false);
					}

				} else if (cmd.equals(ClientCommands.TIME)) {
					// TODO measure latency from connect to here
					long t = Long.parseLong(tokens.nextToken());
					serverTime = t;
					clientTime = System.nanoTime();
					// update here to refresh time?
					synchronized (model) {
						model.update();
					}

				} else if (cmd.equals(ClientCommands.ENTER)) {
					int id = Integer.parseInt(tokens.nextToken());
					int freq = Integer.parseInt(tokens.nextToken());
					synchronized (model) {
						model.enter(id, freq);
					}

				} else if (cmd.equals(ClientCommands.SPEC)) {
					int id = Integer.parseInt(tokens.nextToken());
					// remove from model
					synchronized (model) {
						model.spec(id);
					}

				} else if (cmd.equals(ClientCommands.ID)) {
					id = Integer.parseInt(tokens.nextToken());
					synchronized (model) {
						model.setId(id, name);
					}

				} else if (cmd.equals(ClientCommands.MAPDATA)) {
					synchronized (model) {
						model.getMap().read(tokens);
					}

				} else if (cmd.equals(ClientCommands.MAPTILE)) {
					int x = Integer.parseInt(tokens.nextToken());
					int y = Integer.parseInt(tokens.nextToken());
					int act = Integer.parseInt(tokens.nextToken());
					synchronized (model) {
						model.getMap().updateMapTile(x, y, act);
					}

				} else if (cmd.equals(ClientCommands.TALK)) {
					int id = Integer.parseInt(tokens.nextToken());
					String msg = line.substring(line.indexOf(" ", line.indexOf(" ") + 1));
					synchronized (model) {
						String name = id != 0 ? model.getPlayers().get(id) : "(server)";
						model.getMsgs().add(new Msg(0, name, msg));
					}

				} else if (cmd.equals(ClientCommands.CONNECTED)) {
					int id = Integer.parseInt(tokens.nextToken());
					String name = tokens.nextToken();
					synchronized (model) {
						model.addPlayer(id, name);
					}

				} else if (cmd.equals(ClientCommands.EXIT)) {
					int id = Integer.parseInt(tokens.nextToken());
					synchronized (model) {
						model.removePlayer(id);
					}

				} else {
					out.println("unknown server message: " + line);
				}
			}

		} catch (IOException e) {
			e.printStackTrace(out);
			JOptionPane.showMessageDialog(ClientMain.frame, 
					e.toString() + ": " + e.getMessage(), 
					"Server Thread Exception", 
					JOptionPane.ERROR_MESSAGE);
		}

		model.setServer(null);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * send name. this must be first command and can only be sent once.
	 */
	public void sendName(String name) {
		send(ServerCommands.NAME, name);
	}

	/**
	 * Get current server time (doesn't call server).
	 * Server time starts at 0.
	 */
	public long getTime() {
		long t = System.nanoTime();
		return serverTime + t - clientTime;
	}

	/**
	 * Send enter request to server
	 */
	public void sendEnterReq() {
		send(ServerCommands.ENTERREQ);
	}

	/**
	 * Send spectate to server
	 */
	public void sendSpec() {
		send(ServerCommands.SPEC);
	}

	/**
	 * send map update request to server
	 */
	public void sendMapTileReq(int x, int y, int act) {
		send(ServerCommands.MAPTILEREQ, x, y, act);
	}

	public void sendFire(BulletObject bullet) {
		send(ServerCommands.FIREREQ, bullet.write(new StringBuilder()));
	}

	/**
	 * Send ship update to server
	 */
	public void sendUpdate(ShipObject ship) {
		send(ServerCommands.UPDATE, ship.write(new StringBuilder()));
	}

	/**
	 * send the client talk msg to the server
	 */
	public void sendTalkReq(String msg) {
		send(ServerCommands.TALKREQ, msg);
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
