package el.serv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server connection thread to client.
 * Each client has at most one foreground object identified by id.
 */
class ClientThread extends Thread {
	
	/** update time on client (long) */
	public static final String TIME = "time";
	/** tell client with id to enter (int) */
	public static final String ENTER = "enter";
	/** tell client to spectate (int) */
	public static final String SPEC = "spectate";
	/** tell client its identifier (int) */
	public static final String ID = "id";
	/** tell client to update the given object (int) (FGObject) */
	public static final String UPDATEOBJ = "update-obj";
	
	private static final PrintStream out = System.out;
	private static final AtomicInteger idSequence = new AtomicInteger();
	
	/** client identifier */
	public final int id;
	/** input reader for receiving data from the client */
	private final BufferedReader clientIn;
	/** output stream for sending data to client */
	private final PrintStream clientOut;
	
	public ClientThread(Socket socket) throws Exception {
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
			send(ID + " " + id);
			
			String line;
			while ((line = clientIn.readLine()) != null) {
				out.println(this + ": received " + line);
				String[] tk = line.split("\\s+");
				String cmd = tk[0];
				
				// commands from client to server
				
				if (cmd.equals(ServerThread.ENTERREQ)) {
					ServerMain.enterReq(this);
					
				} else if (cmd.equals(ServerThread.SPEC)) {
					ServerMain.spec(this);
					
				} else if (cmd.equals(ServerThread.UPDATE)) {
					ServerMain.update(this, line.substring(line.indexOf(" ") + 1));
					
				} else {
					out.println(this + ": unknown command " + line);
				}
			}
			
		} catch (IOException e) {
			out.println(this + ": " + e);
		}
	}
	
	public void send(String line) {
		out.println(this + ": send " + line);
		clientOut.println(line);
	}
	
}
