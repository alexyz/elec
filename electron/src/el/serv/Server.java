package el.serv;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class Server {
	private static final PrintStream out = System.out;
	private final List<ClientThread> clients = new ArrayList<ClientThread>();
	private final long startTime;

	public Server() {
		startTime = System.nanoTime();
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
		return System.nanoTime() - this.startTime;
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
		out.println("client " + c + " joined");
	}
	
	public synchronized void leave(ClientThread c) {
		clients.remove(c);
		out.println("client " + c + " left");
	}
	
}
