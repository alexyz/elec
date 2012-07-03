package el.serv;

import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

import el.Model;
import el.bg.ArrayMapBgObject;
import el.bg.MapBgObject;
import el.bg.SparseMapBgObject;

/**
 * electron server 2
 */
public class ServerMain implements Runnable {
	
	public static void main(String[] args) throws Exception {
		ServerMain server = new ServerMain(8111);
		server.run();
	}
	
	/**
	 * list of connected clients. any reads or writes to this list must be synchronised
	 */
	private final List<ClientRunnable> clients = new ArrayList<ClientRunnable>();
	/**
	 * Current map state
	 */
	private final MapBgObject map;
	private final ServerSocket serverSocket;
	
	private int nextClientId = 1000;
	
	private long startTime;
	
	private int maxFreqs = 3;

	/**
	 * Create a new server on the given port
	 */
	public ServerMain(int port) throws IOException {
		map = new ArrayMapBgObject();
		map.read("/maps/alpha.lvl.map.png /maps/alpha.lvl.tiles.png");
		//map.init();
		startTime = System.nanoTime();
		serverSocket = new ServerSocket(port);
	}
	
	/**
	 * Run the server
	 * This method never exits except on server failure
	 */
	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("listening on " + serverSocket);
				Socket socket = serverSocket.accept();
				System.out.println("accepted " + socket);
				//socket.setKeepAlive(true);
				// disconnect silent clients - need to ping regularly
				//socket.setSoTimeout(10000);
				ClientRunnable client = new ClientRunnable(socket, nextClientId++);
				Thread t = new Thread(client);
				t.setDaemon(true);
				t.setPriority(Thread.NORM_PRIORITY);
				t.setName("Client-" + client.id);
				t.start();
				// XXX need to wrap nextClientId, but not reuse any already used, or not yet in clients list
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
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
		/** client input */
		private final BufferedReader br;
		/** client model proxy */
		private final ClientCommands clientProxy;
		private final TextProxy.Unproxy serverUnproxy;

		public ClientRunnable(Socket socket, int id) throws Exception {
			this.socket = socket;
			this.id = id;
			// create input/output here so it is less likely to throw exception in run
			OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"));
			PrintWriter pw = new PrintWriter(osw);
			this.clientProxy = TextProxy.createProxy(ClientCommands.class, pw);
			InputStreamReader isr = new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8"));
			this.br = new BufferedReader(isr);
			this.serverUnproxy = TextProxy.createUnproxy(ServerCommands.class, this);
		}
		
		@Override
		public void run() {
			try {
				// wait for first command from client
				String line = br.readLine();
				System.out.println(this + ": read " + line);
				
				if (line != null && line.startsWith("setName ")) {
					// call method in this class by reflection
					serverUnproxy.call(line);
				} else {
					throw new Exception("client did not send name");
				}
				
				// send server state to new client...
				init();
				
				// read commands from client...
				while ((line = br.readLine()) != null) {
					System.out.println(this + ": read " + line);
					// calls ServerCommands method in this class by reflection
					//TextProxy.unproxy(ServerCommands.class, this, line);
					serverUnproxy.call(line);
				}
				
				System.out.println(this + ": end of stream");
				
			} catch (Exception e) {
				System.out.println(this + ": " + e);
			}
			
			deinit();
			
			// close connection
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
		
		/** send current state of server to new client */
		private void init() {
			clientProxy.setId(id, name);
			clientProxy.setTime(time());
			
			// send map
			clientProxy.setMapData(map.write());
			
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					if (c != this) {
						// introduce clients to each other
						c.clientProxy.playerConnected(id, name);
						clientProxy.playerConnected(c.id, c.name);
						
						if (c.entered) {
							// FIXME don't know position
							clientProxy.playerEntered(c.id, c.freq, Model.centrex, Model.centrey, false);
						}
					}
				}
			}
			
			// start sending updates to new client...
			synchronized (clients) {
				System.out.println("adding " + this);
				clients.add(this);
			}
		}
		
		/** remove client after disconnection */
		private void deinit() {
			synchronized (clients) {
				System.out.println("removing " + this);
				clients.remove(this);
				synchronized (clients) {
					for (ClientRunnable c : clients) {
						c.clientProxy.playerExited(id);
					}
				}
			}
		}
		
		@Override
		public void setName(String name) {
			// TODO check name length/collision
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
					c.clientProxy.playerEntered(id, minFreq, x, y, msg);
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
					c.clientProxy.playerSpectated(id);
				}
			}
		}

		@Override
		public void update(String data) {
			if (!entered) {
				System.out.println("client " + this + " not entered!!");
				clientProxy.addMsg(0, "you are not entered");
				return;
			}
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					if (c != this) {
						c.clientProxy.updateFgObject(id, data);
					}
				}
			}
		}

		@Override
		public void setMapTile(int x, int y, int action) {
			synchronized (clients) {
				map.setMapTile(x, y, action);
				for (ClientRunnable c : clients) {
					c.clientProxy.setMapTile(x, y, action);
				}
			}
		}

		@Override
		public void addBullet(String data) {
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					if (c != this) {
						c.clientProxy.addBullet(data);
					}
				}
			}
		}

		@Override
		public void addMsg(String msg) {
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.clientProxy.addMsg(id, msg);
				}
			}
		}

		@Override
		public void playerHit(int transId) {
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.clientProxy.bulletExploded(transId);
				}
			}
		}

		@Override
		public void playerKilled(int killerId, float x, float y) {
			float newx = Model.centrex + (float) (Math.random() * 640 - 320);
			float newy = Model.centrey + (float) (Math.random() * 480 - 240);
			synchronized (clients) {
				for (ClientRunnable c : clients) {
					c.clientProxy.playerKilled(id, killerId, x, y);
					// TODO really needs to send delay
					c.clientProxy.playerEntered(id, freq, newx, newy, false);
				}
			}
		}
		
		@Override
		public void ping() {
			clientProxy.pong();
		}
		
		@Override
		public String toString() {
			return String.format("Client[%d,%s]", id, name);
		}
		
	}

	
}
