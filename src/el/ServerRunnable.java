package el;

import java.io.*;
import java.net.Socket;

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
	private final BufferedReader serverIn;
	private final PrintWriter serverOut;
	private final Model model;
	private final String name;
	private final ServerCommands proxy;

	public ServerRunnable(Socket socket, Model model, String name) throws Exception {
		this.socket = socket;
		this.model = model;
		this.name = name;
		this.serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.serverOut = new PrintWriter(socket.getOutputStream());
		this.proxy = TextProxy.createProxy(ServerCommands.class, this.serverOut);
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
			while ((line = serverIn.readLine()) != null) {
				out.println("server: " + line);
				final String fline = line;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						TextProxy.unproxy(ClientCommands.class, model, fline);
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace(out);
			JOptionPane.showMessageDialog(ClientMain.frame, 
					e.toString() + ": " + e.getMessage(), 
					"Server Thread Exception", 
					JOptionPane.ERROR_MESSAGE);
		}

		model.setServer(null);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
}
