package el.serv;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * electron server
 */
public class ServerMain {

	static final Server server = new Server();
	static final PrintStream out = System.out;
	static final int lag = 500;

	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(8111);
		while (true) {
			out.println("listening: " + ss);
			Socket s = ss.accept();
			out.println("accepted: " + s);
			server.join(new ClientThread(server, s));
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
