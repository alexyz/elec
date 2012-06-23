import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.net.*;

/**
 * electron server
 */
public class ServerMain {

	static final Server svr = new Server();
	static final PrintStream log = System.out;
	static final int lag = 500;

	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(8111);
		while (true) {
			log.println("listening");
			Socket s = ss.accept();
			log.println("accepted: " + s);
			svr.join(new ClientThread(svr, s));
		}
	}
	
	public static void lag() {
		try {
			Thread.sleep(lag);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

class Server {
	private static final PrintStream log = System.out;
	private final List<ClientThread> clients = new ArrayList<ClientThread>();
	private final long startt;

	public Server() {
		startt = System.nanoTime();
		Thread t = new Thread("Server time thread") {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (this) {
						for (ClientThread c : clients) {
							c.post("Server time " + time());
						}
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	public long time() {
		return System.nanoTime() - this.startt;
	}
	public synchronized void post(String id, String cmd) {
		for (ClientThread c : clients) {
			c.post("Server time " + time());
			c.post(id + " " + cmd);
		}
	}
	public synchronized void join(ClientThread c) {
		clients.add(c);
		c.start();
		post("Server", "join " + c.getName());
		log.println("client " + c + " joined");
	}
	public synchronized void leave(ClientThread c) {
		clients.remove(c);
		log.println("client " + c + " left");
	}
}

class ClientThread extends Thread {
	private static PrintStream log = System.out;
	private static AtomicInteger seq = new AtomicInteger();
	private final Socket s;
	private final BufferedReader in;
	private final PrintStream out;
	private final Server sv;
	public ClientThread(Server sv, Socket s) throws Exception {
		super("ClientThread" + seq.getAndIncrement());
		setDaemon(true);
		setPriority(Thread.NORM_PRIORITY);
		this.sv = sv;
		this.s = s;
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintStream(s.getOutputStream());
	}
	public void run() {
		try {
			String line;
			while ((line = in.readLine()) != null) {
				log.println("client " + this + " read " + line);
				ServerMain.lag();
				sv.post(getName(), line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		sv.leave(this);
	}
	public void post(String line) {
		log.println("client " + this + " posting " + line);
		ServerMain.lag();
		out.println(line);
	}
	@Override
	public String toString() {
		return getName();
	}
}





















