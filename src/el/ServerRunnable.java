package el;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import el.serv.ClientCommands;
import el.serv.ServerCommands;
import el.serv.TextProxy;

/**
 * A client connection to the server
 */
public class ServerRunnable implements Runnable {

	private static final PrintStream out = System.out;

	private final Socket socket;
	private final BufferedReader br;
	private final Model model;
	private final String name;
	private final ServerCommands proxy;

	public ServerRunnable(Socket socket, Model model, String name) throws Exception {
		this.socket = socket;
		this.model = model;
		this.name = name;
		// create input/output here so it is less likely to throw exception in run
		OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"));
		PrintWriter pw = new PrintWriter(osw);
		this.proxy = TextProxy.createProxy(ServerCommands.class, pw);
		InputStreamReader isr = new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8"));
		this.br = new BufferedReader(isr);
	}
	
	/**
	 * server proxy - for calling methods on server
	 */
	public ServerCommands getProxy() {
		return proxy;
	}
	
	@Override
	public void run() {
		try {
			// have to send name on connect
			proxy.setName(name);

			// read commands from server...
			String line;
			while ((line = br.readLine()) != null) {
				out.println("server: " + line);
				final String fline = line;
				// put on event queue so we don't have to synchronize model
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							TextProxy.unproxy(ClientCommands.class, model, fline);
						} catch (Exception e) {
							e.printStackTrace(out);
							ClientMain.handleException("Unproxy", e);
						}
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace(out);
			ClientMain.handleException("Server", e);
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ClientMain.disconnect();
	}
	
}
