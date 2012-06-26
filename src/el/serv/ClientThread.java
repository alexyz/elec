package el.serv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server connection thread to client.
 * Each client has at most one foreground object identified by id.
 */
class ClientThread extends Thread {
	
	/** tell client new server time (long) */
	public static final String TIME = "time";
	/** tell client with id to enter (int) */
	public static final String ENTER = "enter";
	/** tell client to spectate (int) */
	public static final String SPEC = "spectate";
	/** tell client its identifier (int) */
	public static final String ID = "id";
	/** tell client to update the given object (int) (FGObject) */
	public static final String UPDATEOBJ = "update-obj";
	/** tell client to discard any existing map and use given map data (MapBgObject) */
	public static final String MAPDATA = "map-data";
	/** tell client to update map tile */
	public static final String MAPTILE = "map-tile";
	/** tell client to add bullet */
	public static final String FIRE = "fire";
	
	private static final PrintStream out = System.out;
	private static final AtomicInteger idSequence = new AtomicInteger(1000);
	
	/** client identifier */
	public final int id;
	private final Socket socket;
	/** input reader for receiving data from the client */
	private final BufferedReader clientIn;
	/** output stream for sending data to client */
	private final PrintStream clientOut;
	
	/** is this client entered */
	public boolean entered;
	/** last update received from client */
	public String lastData;
	
	public ClientThread(Socket socket) throws Exception {
		this.socket = socket;
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY);
		this.id = idSequence.getAndIncrement();
		setName("ClientThread-" + id);
		clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		clientOut = new PrintStream(socket.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			// send server state to new client...
			ServerMain.clientInit(this);
			
			String line;
			while ((line = clientIn.readLine()) != null) {
				out.println(this + ": received " + line);
				StringTokenizer tokens = new StringTokenizer(line);
				String cmd = tokens.nextToken();
				
				// commands from client to server
				
				if (cmd.equals(ServerThread.UPDATE)) {
					ServerMain.clientUpdate(this, line.substring(line.indexOf(" ") + 1));
					
				} else if (cmd.equals(ServerThread.FIREREQ)) {
					ServerMain.clientFireReq(this, line.substring(line.indexOf(" ") + 1));
					
				} else if (cmd.equals(ServerThread.ENTERREQ)) {
					ServerMain.clientEnterReq(this);
					
				} else if (cmd.equals(ServerThread.SPEC)) {
					ServerMain.clientSpec(this);
					
				} else if (cmd.equals(ServerThread.MAPTILEREQ)) {
					int x = Integer.parseInt(tokens.nextToken());
					int y = Integer.parseInt(tokens.nextToken());
					int act = Integer.parseInt(tokens.nextToken());
					ServerMain.clientUpdateMap(this, x, y, act);
					
				} else {
					out.println(this + ": unknown command " + line);
				}
			}
			
		} catch (IOException e) {
			out.println(this + ": " + e);
			ServerMain.removeClient(this);
		}
	}
	
	/** send data to client */
	public void send(String command, Object... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(command);
		for (Object o : args) {
			sb.append(" ");
			sb.append(o);
		}
		out.println(this + ": send " + sb);
		clientOut.println(sb);
		clientOut.flush();
	}
	
}
