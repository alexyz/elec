package el.serv;

import java.io.*;
import java.net.Socket;

import el.Model;
import el.fg.Ship;
import el.fg.ShipType;

/**
 * A client connection to the server
 */
public class ServerThread extends Thread implements Server {
	
	/** ask server if we can enter (void) */
	public static final String ENTERREQ = "enter-req";
	/** tell server we are going to spectate (void) */
	public static final String SPEC = "spec";
	/** update details of players ship */
	public static final String UPDATE = "update";
	
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
				String[] tk = line.split("\\s+");
				String cmd = tk[0];
				out.println("server: " + line);
				
				if (cmd.equals(ClientThread.TIME)) {
					long t = Long.parseLong(tk[1]);
					serverTime = t;
					clientTime = System.nanoTime();
					// XXX push to model
					
				} else if (cmd.equals(ClientThread.ENTER)) {
					int id = Integer.parseInt(tk[1]);
					if (this.id == id) {
						out.println("will enter");
						model.enter(id);
						
					} else {
						out.println("someone else entered");
						model.addFgObject(new Ship(ShipType.types[0], Model.centrex, Model.centrey), id);
					}
					
				} else if (cmd.equals(ClientThread.SPEC)) {
					int id = Integer.parseInt(tk[1]);
					if (this.id == id) {
						out.println("will spectate");
						model.spec();
						
					} else {
						out.println("someone else spectated");
						// remove from model
					}
					
				} else if (cmd.equals(ClientThread.UPDATEOBJ)) {
					int id = Integer.parseInt(tk[1]);
					model.updateFgObject(id, tk, 2);
					
				} else if (cmd.equals(ClientThread.ID)) {
					id = Integer.parseInt(tk[1]);
					
				} else {
					out.println("unknown server message: " + line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public long getTime() {
		long t = System.nanoTime();
		return serverTime + t - clientTime;
	}
	
	@Override
	public void enterReq() {
		serverOut.println(ENTERREQ);
	}

	@Override
	public void specReq() {
		serverOut.println(SPEC);
	}

	@Override
	public void update(Ship ship) {
		StringBuilder sb = new StringBuilder();
		sb.append(UPDATE).append(" ");
		ship.write(sb);
		serverOut.println(sb.toString()); 
	}
	
}
