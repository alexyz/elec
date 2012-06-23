package el.serv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

class ClientThread extends Thread {
	private static PrintStream log = System.out;
	private static AtomicInteger sequence = new AtomicInteger();
	
	private final Socket socket;
	private final BufferedReader clientIn;
	private final PrintStream clientOut;
	private final Server server;
	
	public ClientThread(Server sv, Socket s) throws Exception {
		super("ClientThread" + sequence.getAndIncrement());
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY);
		this.server = sv;
		this.socket = s;
		clientIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
		clientOut = new PrintStream(s.getOutputStream());
	}
	
	@Override
	public void run() {
		try {
			String line;
			while ((line = clientIn.readLine()) != null) {
				log.println("client " + this + " read " + line);
				ServerMain.lag();
				server.post(getName(), line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.leave(this);
	}
	
	public void post(String line) {
		log.println("client " + this + " posting " + line);
		ServerMain.lag();
		clientOut.println(line);
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
