package el;

import java.io.*;
import java.net.Socket;

/**
 * A client connection to the server
 */
class ServerThread extends Thread {
	private static final PrintStream log = System.out;
	private final Socket s;
	private final BufferedReader in;
	private final PrintStream out;
	private final Model m;
	private long servertime, localtime;
	public ServerThread(Socket s, Model m) throws Exception {
		this.s = s;
		this.m = m;
		setPriority(Thread.NORM_PRIORITY);
		setDaemon(true);
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintStream(s.getOutputStream());
	}
	@Override
	public void run() {
		try {
			String line;
			while ((line = in.readLine()) != null) {
				String[] tk = line.split("\\s+");
				if (tk[0].equals("Server")) {
					if (tk[1].equals("time")) {
						long t = Long.parseLong(tk[2]);
						log.println("server time is " + t);
						servertime = t;
						localtime = System.nanoTime();
						continue;
					}
				}
				// TODO parse messages from self
				log.println("unknown server message: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public long getServerTime() {
		long t = System.nanoTime();
		return servertime + t - localtime;
	}
	public void post(String line) {
		out.println(line);
	}
}
