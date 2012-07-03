package el;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

import javax.swing.SwingUtilities;

import el.serv.ClientCommands;
import el.serv.ServerCommands;
import el.serv.TextProxy;

/**
 * A client connection to the server
 */
public class ServerRunnable implements Runnable {

	private final Socket socket;
	private final BufferedReader br;
	private final Model model;
	private final String name;
	private final ServerCommands serverProxy;
	private final TextProxy.Unproxy clientUnproxy;

	public ServerRunnable(Socket socket, Model model, String name) throws Exception {
		this.socket = socket;
		this.model = model;
		this.name = name;
		// create input/output here so it is less likely to throw exception in run
		OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"));
		PrintWriter pw = new PrintWriter(osw);
		this.serverProxy = TextProxy.createProxy(ServerCommands.class, pw);
		InputStreamReader isr = new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8"));
		this.br = new BufferedReader(isr);
		this.clientUnproxy = TextProxy.createUnproxy(ClientCommands.class, model);
	}
	
	/**
	 * server proxy - for calling methods on server
	 */
	public ServerCommands getServerProxy() {
		return serverProxy;
	}
	
	@Override
	public void run() {
		try {
			// have to send name on connect
			serverProxy.setName(name);

			// read commands from server...
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println("server: " + line);
				final String fline = line;
				// put on event queue so we don't have to synchronize model
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							clientUnproxy.call(fline);
						} catch (Exception e) {
							e.printStackTrace(System.out);
							ClientFrame.handleException("Unproxy", e);
						}
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace(System.out);
			ClientFrame.handleException("Server", e);
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ClientFrame.getInstance().disconnect();
	}
	
}
