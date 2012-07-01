
package el;

import java.util.*;

import el.phys.*;
import el.serv.ClientCommands;
import el.bg.*;
import el.fg.*;

/** 
 * <p>Client model.
 * Play area is 1M pixels squared. Co-ordinates are represented by floats, 
 * so any more than 1M would start to lose precision.
 * 
 * <p>By convention, variables starting with:
 * <br>sx, sy or vx, vy - screen co-ordinates
 * <br>mx, my - model co-ordinates
 * <br>qx, qy - model quadrant co-ordinates (for some quadrant size)
 * <br>dx, dy - deltas in px/sec
 * 
 * <p>This class is not thread safe. Concurrent access must be externally synchronized
 * (i.e. from AWT event thread and ServerThread instance).
 * 
 * <p>Methods specified by ClientCommands interface should not send 
 * the same command back to the server, unless you want an infinite loop
 * 
 * <p>There are two types of time - nano seconds represented by long, and seconds 
 * represented by float. Don't get them confused.
 */
public class Model implements ClientCommands {
	
	/** Nanoseconds in second */
	private static final float NS_IN_S = 1000000000.0f;
	/** centre co-ordinates */
	public static final int centrex = 500000, centrey = 500000, maxx = 1000000, maxy = 1000000;
	
	//
	// final fields
	//
	
	/** permanent foreground objects (ships) */
	private final ArrayList<FgObject> objects = new ArrayList<FgObject>();
	/** transient foreground objects (bullets) */
	private final ArrayList<TransObject> transObjects = new ArrayList<TransObject>();
	private final QuadMap<BulletObject> transMap = new QuadMap<BulletObject>(new BulletQuadMapKey());
	private final ActionMap actionMap = new ActionMap();
	private final ArrayList<FgAction> actions = new ArrayList<FgAction>();
	private final ModelObject modelObj = new ModelObject();
	/** The map (fg objects and transients may collide with it) */
	private final MapBgObject map = new MapBgObject();
	private final List<BgObject> bgObjects = new ArrayList<BgObject>();
	/** talk messages from server */
	private final ArrayList<Msg> msgs = new ArrayList<Msg>();
	/** known players */
	private final Map<Integer,String> players = new TreeMap<Integer,String>();
	
	//
	// mutable fields
	//
	
	/** server time at the last time it was set (nanoseconds) */
	private long serverNanoTime;
	/** client time at the last time the server time was set, or model was created (nanoseconds) */
	private long clientNanoTime;
	/** time of last call to update (seconds) */
	private float updateTime;
	/** time the last focus object update was sent to server (seconds) */
	private float serverUpdateTime;
	/** time of last call to ping() */
	private long pingNanoTime;
	/** Issue a ship server update on next call to update() */
	private boolean forceUpdate;
	/** currently focused object */
	private FgObject focusObj = modelObj;
	/** the server, if any */
	private ServerRunnable server;
	/** users id, also used for ship */
	private int id;
	
	public Model() {
		clientNanoTime = System.nanoTime();
		// should really be down to zone
		bgObjects.add(new StarBgObject2());
		bgObjects.add(map);
	}
	
	@Override
	public void setId(int id, String name) {
		this.id = id;
		players.put(id, name);
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public void setTime(long time) {
		System.out.println("server time is " + time / NS_IN_S);
		serverNanoTime = time;
		clientNanoTime = System.nanoTime();
		updateTime = serverNanoTime / NS_IN_S;
	}
	
	public void sendMsg(String msg) {
		if (server != null) {
			server.getServerProxy().addMsg(msg);
		} else {
			msgs.add(new Msg(0, "(self)", msg));
		}
	}
	
	public List<Msg> getMsgs() {
		return msgs;
	}
	
	@Override
	public void addMsg(int id, String msg) {
		msgs.add(new Msg(0, players.get(id), msg));
	}
	
	public Map<Integer,String> getPlayers() {
		return players;
	}
	
	/**
	 * player has connected
	 */
	@Override
	public void playerConnected(int id, String name) {
		players.put(id, name);
		forceUpdate = true;
		msgs.add(new Msg(0, null, name + " connected"));
		// for benefit of client that has just entered
		forceUpdate = true;
	}
	
	/**
	 * player has disconnected
	 */
	@Override
	public void playerExited(int id) {
		removeFgObject(id);
		String name = players.remove(id);
		msgs.add(new Msg(0, null, name + " disconnected"));
	}
	
	private void removeFgObject(int id) {
		Iterator<FgObject> i = objects.iterator();
		while (i.hasNext()) {
			if (i.next().getId() == id) {
				i.remove();
				break;
			}
		}
		if (focusObj != null && focusObj.getId() == id) {
			// move spec view to point of explosion
			modelObj.x = focusObj.x;
			modelObj.y = focusObj.y;
			focusObj = modelObj;
		}
	}
	
	@Override
	public void playerKilled(int id, int killerId, float x, float y) {
		removeFgObject(id);
		ExplodeObject ex = new ExplodeObject(x, y, 24, getTime());
		addTransObject(ex, false);
		String name = players.get(id);
		String killerName = players.get(killerId);
		// if two have same freq, should say murdered
		msgs.add(new Msg(0, null, name + " killed by " + killerName));
	}
	
	/**
	 * enter someone, possibly user into the game
	 */
	@Override
	public void playerEntered(int id, int freq, float x, float y, boolean msg) {
		if (id == focusObj.getId()) {
			throw new RuntimeException();
		}
		ShipObject ship = new ShipObject(ShipType.types[0], x, y);
		System.out.println("new ship: " + ship);
		System.out.println("ship.x=" + ship.x + " ship.getx=" + ship.getX());
		ship.setFreq(freq);
		if (this.id == id) {
			// TODO need to put focused object on top
			focusObj = ship;
			forceUpdate = true;
		}
		addFgObject(ship, id);
		if (msg) {
			String name = players.get(id);
			msgs.add(new Msg(0, null, name + " entered on freq " + freq));
		}
	}
	
	/**
	 * remove the given user (possibly self) from the game
	 */
	@Override
	public void playerSpectated(int id) {
		removeFgObject(id);
		String name = players.get(id);
		msgs.add(new Msg(0, null, name + " spectates"));
	}
	
	/**
	 * focus next foreground object.
	 */
	public void focusCycle() {
		// TODO prevent focus cycle when playing
		if (focusObj == modelObj) {
			if (objects.size() > 0)
				focusObj = objects.get(0);
		} else { 
			int i = objects.indexOf(focusObj) + 1;
			focusObj = i < objects.size() ? objects.get(i) : modelObj;
		}
		System.out.println("focus: " + focusObj);
	}
	
	/**
	 * update the world
	 */
	public void update() {
		// calculate time and delta time
		float time, timeDelta;
		if (server != null) {
			long t = serverNanoTime + System.nanoTime() - clientNanoTime;
			time = t / NS_IN_S;
			timeDelta = time - this.updateTime;
		} else {
			time = (System.nanoTime() - this.clientNanoTime) / NS_IN_S;
			timeDelta = time - this.updateTime;
		}
		
		// validate times
		if (timeDelta > 10f) {
			System.out.println("server time is " + (serverNanoTime / NS_IN_S));
			System.out.println("client time is " + ((System.nanoTime() - clientNanoTime) / NS_IN_S));
			throw new RuntimeException("unexpected time " + time + " delta " + timeDelta);
		}
		
		this.updateTime = time;
		
		for (BgObject o : bgObjects) {
			o.update(time, timeDelta);
		}
		
		// apply actions for focused object
		if (actions.size() > 0 && (focusObj == modelObj || focusObj.getId() == id)) {
			for (FgAction r : actions) {
				// run action
				if (r.run(focusObj, time, timeDelta)) {
					// do server update if enough time has passed
					if ((time - serverUpdateTime) > 0.125) {
						forceUpdate = true;
					}
				}
			}
		}
		
		// TODO separate bullets and other trans objects
		if (transObjects.size() > 0) {
			Iterator<TransObject> i = transObjects.iterator();
			while (i.hasNext()) {
				TransObject o = i.next();
				int oldx = o.getX(), oldy = o.getY();
				o.update(time, timeDelta);
				if (o instanceof BulletObject) {
					transMap.update((BulletObject) o, oldx, oldy);
				}
				// see if object wants to be removed due to timeout/collision
				if (o.isRemove()) {
					i.remove();
					if (o instanceof BulletObject) {
						transMap.remove((BulletObject) o);
					}
				}
			}
		}

		for (FgObject o : objects) {
			o.update(time, timeDelta);
		}
		
		// check if players ship has hit any transients
		if (id > 0 && focusObj.getId() == id) {
			ShipObject s = (ShipObject) focusObj;
			ArrayList<BulletObject> l = transMap.get(s.getX(), s.getY(), s.getRadius());
			int killerId = 0;
			if (l != null) {
				for (BulletObject obj : l) {
					if (obj.getFreq() != s.getFreq() && !obj.hitSent) {
						server.getServerProxy().playerHit(obj.getId());
						obj.hitSent = true;
						//FIXME hack until we get proper info types
						s.setEnergy(s.getEnergy() - 100f);
						if (s.getEnergy() < 0) {
							// object id of creator is encoded in transient id
							killerId = obj.getId() / 1000;
						}
					}
				}
			}
			if (s.getEnergy() < 0) {
				// wait for server to send message back for explosion
				server.getServerProxy().playerKilled(killerId, focusObj.x, focusObj.y);
			}
		}
		
		// send to server - after possible reflect
		if (server != null && focusObj instanceof ShipObject && forceUpdate) {
			// update focused ship on server
			server.getServerProxy().update(focusObj.write());
			serverUpdateTime = time;
			forceUpdate = false;
		}
		
	}
	
	/**
	 * Create explosion transient
	 */
	@Override
	public void bulletExploded(int transId) {
		for (TransObject obj : transObjects) {
			if (obj.getId() == transId) {
				// TODO inflict damage on self
				transObjects.remove(obj);
				transMap.remove((BulletObject) obj);
				// TODO get actual hit position
				ExplodeObject ex = new ExplodeObject(obj.getX(), obj.getY(), obj.getRadius(), getTime());
				addTransObject(ex, false);
				return;
			}
		}
		System.out.println("could not find trans id " + transId);
	}
	
	/**
	 * get time at last call to update (or during call to update).
	 */
	public float getTime() {
		return updateTime;
	}
	
	public List<BgObject> getBgObjects() {
		return bgObjects;
	}
	
	public List<FgObject> getFgObjects() {
		return objects;
	}
	
	public List<TransObject> getTransFgObjects() {
		return transObjects;
	}
	
	/**
	 * Apply action to focused object (until unactioned)
	 */
	public void action(String name) {
		FgAction action = actionMap.get(name);
		if (action == null)
			throw new RuntimeException(name);
		if (!actions.contains(action))
			actions.add(action);
	}
	
	/**
	 * Stop applying action to focused object
	 */
	public void unaction(String name) {
		FgAction action = actionMap.get(name);
		// check for null as we won't get a null pointer exception otherwise
		if (action == null) {
			throw new RuntimeException(name);
		}
		actions.remove(action);
		if (actions.size() == 0) {
			// let server know last state of focused object
			// XXX also triggered on fire, should probably only do for movement
			forceUpdate = true;
		}
	}

	public FgObject getFocus() {
		return focusObj;
	}
	
	/** add foreground object with given identifier, sets the model of the object */
	public void addFgObject(FgObject obj, int id) {
		obj.setModel(this);
		obj.setId(id);
		objects.add(obj);
	}
	
	/** update the state of the given object, called by server */
	@Override
	public void updateFgObject(int id, String data) {
		// focused object could be updated if spectating someone else
		for (FgObject obj : objects) {
			if (obj.getId() == id) {
				obj.read(data);
				return;
			}
		}
		System.out.println("could not find object " + id);
	}
	
	private int transIdSeq = 0;
	
	@Override
	public void addBullet(String data) {
		BulletObject bullet = new BulletObject();
		bullet.read(data);
		addTransObject(bullet, false);
	}
	
	/**
	 * Add a transient object, optionally sending to server
	 */
	public void addTransObject(TransObject obj, boolean send) {
		obj.setModel(this);
		transObjects.add(0, obj);
		if (obj instanceof BulletObject) {
			transMap.add((BulletObject) obj);
		}
		if (send && server != null && obj instanceof BulletObject) {
			obj.setId(id * 1000 + transIdSeq);
			transIdSeq = (transIdSeq + 1) % 1000;
			server.getServerProxy().addBullet(obj.write());
		}
	}
	
	public Intersection backgroundCollision(FgObject obj, float tx, float ty) {
		// check collision with map
		Intersection r = map.intersects(obj, tx, ty);
		if (r != null && obj == focusObj) {
			forceUpdate = true;
		}
		return r;
	}
	
	@Deprecated
	public void foregroundCollision(ShipObject ship) {
		ArrayList<BulletObject> l = transMap.get(ship.getX(), ship.getY(), ship.getRadius());
		if (l != null) {
			for (BulletObject obj : l) {
				if (obj.getFreq() != ship.getFreq()) {
					//System.out.println("impact!!");
					obj.collided = true;
					// 
				}
			}
		}
	}
	
	public void setServer(ServerRunnable server) {
		// should probably clear here, or pass in constructor
		this.server = server;
		if (objects.size() > 0) {
			throw new RuntimeException();
		}
	}
	
	public MapBgObject getMap() {
		return map;
	}
	
	@Override
	public void setMapData(String data) {
		map.read(new StringTokenizer(data));
	}
	
	public void setMapTileReq(int x, int y, int action) {
		if (server != null) {
			server.getServerProxy().setMapTile(x, y, action);
		} else {
			map.setMapTile(x, y, action);
		}
	}
	
	@Override
	public void setMapTile(int x, int y, int action) {
		map.setMapTile(x, y, action);
	}
	
	public void ping() {
		pingNanoTime =  System.nanoTime();
		server.getServerProxy().ping();
	}
	
	@Override
	public void pong() {
		long t = System.nanoTime() - pingNanoTime;
		addMsg(0, "ping: " + (t / NS_IN_S));
	}
	
	@Override
	public String toString() {
		return String.format("Model[id=%d objs=%d,%d tm=%s t=%.1f %s]", 
				id,
				objects.size(), transObjects.size(), 
				transMap,
				updateTime, 
				actions);
	}
	
}

