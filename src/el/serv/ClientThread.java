package el.serv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Server connection thread to client.
 * Each client has at most one foreground object identified by id.
 */
public class ClientThread extends Thread {
	
	private static final PrintStream out = System.out;
	
	/** client unique identifier */
	public final int id;
	/** server instance */
	private final ServerMain server;
	/** network socket */
	private final Socket socket;
	/** input reader for receiving data from the client */
	private final BufferedReader clientIn;
	/** output stream for sending data to client */
	private final PrintStream clientOut;
	
	/** is this client entered */
	public boolean entered;
	/** clients name */
	public String name;
	/** client's frequency */
	public int freq;

	public ClientThread(ServerMain server, Socket socket, int id) throws Exception {
		this.server = server;
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY);
		this.socket = socket;
		this.id = id;
		setName("ClientThread-" + id);
		clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		clientOut = new PrintStream(socket.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			// wait for first command from client
			String line = clientIn.readLine();
			out.println(this + ": received " + line);
			
			if (line.startsWith(ServerCommands.NAME)) {
				name = line.substring(line.indexOf(" ") + 1);
			} else {
				throw new Exception("client did not send name");
			}
			
			// send server state to new client...
			server.clientInit(this);
			
			// read commands from client...
			while ((line = clientIn.readLine()) != null) {
				out.println(this + ": received " + line);
				StringTokenizer tokens = new StringTokenizer(line);
				String cmd = tokens.nextToken();
				
				// commands from client to server
				
				if (cmd.equals(ServerCommands.UPDATE)) {
					server.clientUpdate(this, line.substring(line.indexOf(" ") + 1));
					
				} else if (cmd.equals(ServerCommands.FIREREQ)) {
					server.clientFireReq(this, line.substring(line.indexOf(" ") + 1));
					
				} else if (cmd.equals(ServerCommands.TALKREQ)) {
					String msg = line.substring(line.indexOf(" ") + 1).trim();
					if (msg.length() > 0) {
						server.clientTalkReq(this, msg);
					}
					
				} else if (cmd.equals(ServerCommands.ENTERREQ)) {
					server.clientEnterReq(this);
					
				} else if (cmd.equals(ServerCommands.SPEC)) {
					server.clientSpec(this);
					
				} else if (cmd.equals(ServerCommands.MAPTILEREQ)) {
					int x = Integer.parseInt(tokens.nextToken());
					int y = Integer.parseInt(tokens.nextToken());
					int act = Integer.parseInt(tokens.nextToken());
					server.clientMapTileReq(this, x, y, act);
					
				} else {
					out.println(this + ": unknown command " + line);
				}
			}
			
		} catch (Exception e) {
			out.println(this + ": " + e);
			server.clientExit(this);
		}
		
		// close connection
		try {
			socket.close();
		} catch (Exception e) {
			e.printStackTrace(out);
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
