package el.serv;

import java.util.*;
import java.io.*;
import java.net.*;

import el.Model;
import el.bg.MapBgObject;

/**
 * electron server 2
 */
public class ServerMain {
	
	private static final PrintStream out = System.out;
	
	public static void main(String[] args) throws Exception {
		ServerMain server = new ServerMain();
		server.run(8111);
	}
	
	/**
	 * list of connected clients. any reads or writes to this list must be synchronised
	 */
	private final List<ClientRunnable> clients = new ArrayList<ClientRunnable>();
	/**
	 * Current map state
	 */
	private final MapBgObject map = new MapBgObject();
	
	private int nextClientId = 1000;
	
	private long startTime;
	
	private int maxFreqs = 3;

	/**
	 * Create a new server
	 */
	public ServerMain() {
		map.init();
		startTime = System.nanoTime();
	}
	
	/**
	 * Run the server
	 * This method never exits except on server failure
	 */
	private void run(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while (true) {
				out.println("listening on " + serverSocket);
				Socket socket = serverSocket.accept();
				out.println("accepted " + socket);
				socket.setKeepAlive(true);
				//TODO do this to disconnect silent clients
				//socket.setSoTimeout(10000);
				ClientRunnable client = new ClientRunnable(socket, nextClientId++);
				Thread t = new Thread(client);
				t.setDaemon(true);
				t.setPriority(Thread.NORM_PRIORITY);
				t.setName("Client-" + client.id);
				t.start();
				// XXX need to wrap nextClientId 
			}
			
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}
	
	/**
	 * get current server time
	 */
	private long time() {
		return System.nanoTime() - startTime;
	}
	
	/**
	 * Server connection thread to client.
	 * Each client has at most one foreground object identified by id.
	 */
	private class ClientRunnable implements Runnable, ServerCommands {
		
		/** client unique identifier */
		public final int id;
		/** is this client entered */
		public boolean entered;
		/** clients name */
		public String name;
		/** client's frequency */
		public int freq;
		
		/** network socket */
		private final Socket socket;
		/** input reader for receiving data from the client */
		private final BufferedReader clientIn;
		/** client model proxy */
		private final ClientCommands proxy;

		public ClientRunnable(Socket socket, int id) throws Exception {
			this.socket = socket;
			this.id = id;
			clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			proxy = TextProxy.createProxy(ClientCommands.class, new PrintWriter(socket.getOutputStream()));
		}
		
		@Override
		public void run() {
			try {
				// wait for first command from client
				String line = clientIn.readLine();
				out.println(this + ": received " + line);
				
				if (line.startsWith("setName")) {
					TextProxy.unproxy(ServerCommands.class, this, line);
				} else {
					throw new Exception("client did not send name");
				}
				
				// send server state to new client...
				init();
				
				// read commands from client...
				while ((line = clientIn.readLine()) != null) {
					out.println(this + ": received " + line);
					// calls ServerCommands method in this class by reflection
					TextProxy.unproxy(ServerCommands.class, this, line);
				}
				
			} catch (Exception e) {
				out.println(this + ": " + e);
				deinit();
			}
			
			// close connection
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace(out);
			}
		}
		
		/** send current state of server to new client */
		private void init() {
			proxy.setId(id, name);
			proxy.setTime(time());
			
			// send map
			proxy.setMapData(map.write(new StringBuilder()).toString());
			
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					if (c != this) {
						// introduce clients to each other
						c.proxy.playerConnected(id, name);
						proxy.playerConnected(c.id, c.name);
						
						if (c.entered) {
							// FIXME don't know position
							proxy.playerEntered(c.id, c.freq, 1000, 1000, false);
						}
					}
				}
			}
			
			// start sending updates to new client...
			synchronized (clients) {
				out.println("adding " + this);
				clients.add(this);
			}
		}
		
		/** remove client after disconnection */
		private void deinit() {
			synchronized (clients) {
				out.println("removing " + this);
				clients.remove(this);
				synchronized (clients) {
					for (ClientRunnable c : clients) {
						c.proxy.playerExited(id);
					}
				}
			}
		}
		
		@Override
		public void setName(String name) {
			this.name = name;
		}
		
		@Override
		public void enterReq() {
			// count numbers on each frequency
			int[] freqCount = new int[maxFreqs];
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					freqCount[c.freq]++;
				}
			}
			
			// find smallest frequency
			int minFreq = 0;
			int minCount = Integer.MAX_VALUE;
			for (int f = 1; f < freqCount.length; f++) {
				if (freqCount[f] < minCount) {
					minCount = freqCount[f];
					minFreq = f;
				}
			}
			
			float x = Model.centrex + (float) (Math.random() * 640 - 320);
			float y = Model.centrey + (float) (Math.random() * 480 - 240);
			boolean msg = true;
			
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.proxy.playerEntered(id, minFreq, x, y, msg);
				}
			}
			
			freq = minFreq;
			entered = true;
		}

		@Override
		public void spec() {
			entered = false;
			freq = 0;
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.proxy.playerSpectated(id);
				}
			}
		}

		@Override
		public void update(String data) {
			if (!entered) {
				out.println("client " + this + " not entered!!");
				proxy.addMsg(0, "you are not entered");
				return;
			}
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					if (c != this) {
						c.proxy.updateFgObject(id, data);
					}
				}
			}
		}

		@Override
		public void setMapTile(int x, int y, int action) {
			synchronized (clients) {
				map.setMapTile(x, y, action);
				for (ClientRunnable c : clients) {
					c.proxy.setMapTile(x, y, action);
				}
			}
		}

		@Override
		public void addBullet(String data) {
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					if (c != this) {
						c.proxy.addBullet(freq, data);
					}
				}
			}
		}

		@Override
		public void addMsg(String msg) {
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.proxy.addMsg(id, msg);
				}
			}
		}

		@Override
		public void playerHit(int transId) {
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.proxy.bulletExploded(transId);
				}
			}
		}

		@Override
		public void playerKilled(int killerId, float x, float y) {
			float newx = Model.centrex + (float) (Math.random() * 640 - 320);
			float newy = Model.centrey + (float) (Math.random() * 480 - 240);
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.proxy.playerKilled(id, killerId, x, y);
					// TODO really needs to send delay
					c.proxy.playerEntered(id, freq, newx, newy, false);
				}
			}
		}
		
	}

	
}
