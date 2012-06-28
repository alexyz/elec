
package el;

import java.util.*;

import el.phys.*;
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
 */
public class Model {
	
	/** Nanoseconds in second */
	private static final float NS_IN_S = 1000000000.0f;

	/** centre co-ordinates */
	public static final int centrex = 500000, centrey = 500000;
	
	//
	// final fields
	//
	
	/**
	 * permanent foreground objects (ships)
	 */
	private final ArrayList<FgObject> objects = new ArrayList<FgObject>();
	/**
	 * transient foreground objects (bullets)
	 */
	private final ArrayList<TransObject> transObjects = new ArrayList<TransObject>();
	private final QuadMap<BulletObject> transMap = new QuadMap<BulletObject>(new BulletQuadMapKey());
	private final ActionMap actionMap = new ActionMap();
	private final ArrayList<FgRunnable> actions = new ArrayList<FgRunnable>();
	private final ModelObject modelObj = new ModelObject();
	private final long startTime = System.nanoTime();
	/**
	 * The map (transients may collide with it)
	 */
	private final MapBgObject map = new MapBgObject();
	/**
	 * The background (non interacting)
	 */
	private final StarBgObject stars = new StarBgObject();
	private final List<BgObject> bgObjects = Arrays.asList(stars, map);
	/** talk messages from server */
	private final ArrayList<Msg> msgs = new ArrayList<Msg>();
	/** known players */
	private final Map<Integer,String> players = new TreeMap<Integer,String>();
	
	//
	// mutable fields
	//
	
	/**
	 * time of last call to update
	 */
	private float updateTime;
	/**
	 * time the last focus object update was sent to server
	 */
	private float lastUpdate;
	/**
	 * Issue a focused object server update on next call to update()
	 */
	private boolean forceUpdate;
	/**
	 * currently focused object
	 */
	private FgObject focusObj = modelObj;
	/**
	 * the server, if any
	 */
	private ServerRunnable server;
	/**
	 * users id, also used for ship
	 */
	private int id;
	
	public Model() {
		//
	}
	
	public void setId(int id, String name) {
		this.id = id;
		players.put(id, name);
	}
	
	public int getId() {
		return id;
	}
	
	public void sendMsg(String msg) {
		if (server != null) {
			server.sendTalkReq(msg);
		} else {
			msgs.add(new Msg(0, "(self)", msg));
		}
	}
	
	public List<Msg> getMsgs() {
		return msgs;
	}
	
	public Map<Integer,String> getPlayers() {
		return players;
	}
	
	/**
	 * player has connected
	 */
	public void addPlayer(int id, String name) {
		players.put(id, name);
		forceUpdate = true;
		msgs.add(new Msg(0, null, name + " connected"));
		// for benefit of client that has just entered
		forceUpdate = true;
	}
	
	/**
	 * player has disconnected
	 */
	public void removePlayer(int id) {
		spec(id);
		String name = players.remove(id);
		msgs.add(new Msg(0, null, name + " disconnected"));
	}
	
	/**
	 * enter someone, possibly user into the game
	 */
	public void enter(int id, int freq) {
		ShipObject ship = new ShipObject(ShipType.types[0], centrex, centrey);
		ship.setFreq(freq);
		if (this.id == id) {
			// TODO need to put focused object on top
			focusObj = ship;
			forceUpdate = true;
		}
		addFgObject(ship, id);
		String name = players.get(id);
		msgs.add(new Msg(0, null, name + " entered on freq " + freq));
	}
	
	/**
	 * remove the given user (possibly self) from the game
	 */
	public void spec(int id) {
		Iterator<FgObject> i = objects.iterator();
		while (i.hasNext()) {
			if (i.next().getId() == id) {
				i.remove();
				break;
			}
		}
		if (focusObj != null && focusObj.getId() == id) {
			focusObj = modelObj;
		}
		String name = players.get(id);
		msgs.add(new Msg(0, null, name + " spectates"));
	}
	
	/**
	 * focus next foreground object. while entered you should only be able to
	 * focus one (your ship).
	 */
	void focusCycle() {
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
		
		float floatTime, floatTimeDelta;
		if (server != null) {
			floatTime = server.getTime() / NS_IN_S;
			floatTimeDelta = floatTime - this.updateTime;
			
		} else {
			floatTime = (System.nanoTime() - this.startTime) / NS_IN_S;
			floatTimeDelta = floatTime - this.updateTime;
		}
		
		this.updateTime = floatTime;
		
		// apply actions for focused object
		if (actions.size() > 0 && (focusObj == modelObj || focusObj.getId() == id)) {
			for (FgRunnable r : actions) {
				r.run(focusObj);
			}
			if ((floatTime - lastUpdate) > 0.125) {
				forceUpdate = true;
			}
		}
		
		// FIXME need seperate bullets and other trans objects
		if (transObjects.size() > 0) {
			Iterator<TransObject> i = transObjects.iterator();
			while (i.hasNext()) {
				TransObject o = i.next();
				int oldx = o.getX(), oldy = o.getY();
				o.update(floatTime, floatTimeDelta);
				if (o instanceof BulletObject) {
					transMap.update((BulletObject) o, oldx, oldy);
				}
				if (o.isRemove()) {
					i.remove();
					if (o instanceof BulletObject) {
						transMap.remove((BulletObject) o);
					}
				}
			}
		}

		for (FgObject o : objects) {
			o.update(floatTime, floatTimeDelta);
		}
		
		// send to server - after possible reflect
		if (server != null && focusObj instanceof ShipObject && forceUpdate) {
			// update focused ship on server
			server.sendUpdate((ShipObject) focusObj);
			lastUpdate = floatTime;
			forceUpdate = false;
		}
		
		
	}
	
	/**
	 * get time at last call to update (or during call to update).
	 */
	public float getTime() {
		return updateTime;
	}
	
	int getX() {
		return focusObj.getX();
	}
	
	int getY() {
		return focusObj.getY();
	}
	
	List<BgObject> getBgObjects() {
		return bgObjects;
	}
	
	List<FgObject> getFgObjects() {
		return objects;
	}
	
	List<TransObject> getTransFgObjects() {
		return transObjects;
	}
	
	/**
	 * Apply action to focused object (until unactioned)
	 */
	void action(String name) {
		FgRunnable action = actionMap.get(name);
		if (action == null)
			throw new RuntimeException(name);
		if (!actions.contains(action))
			actions.add(action);
	}
	
	/**
	 * Stop applying action to focused object
	 */
	void unaction(String name) {
		FgRunnable action = actionMap.get(name);
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
		obj.setModel(this, id);
		objects.add(obj);
	}
	
	/** update the state of the given object, called by server */
	public void updateFgObject(int id, StringTokenizer tokens) {
		// focused object could be updated if spectating someone else
		for (FgObject obj : objects) {
			if (obj.getId() == id) {
				obj.read(tokens);
				return;
			}
		}
		System.out.println("could not find object " + id);
	}
	
	/**
	 * Add a transient object, optionally sending to server
	 */
	public void addTransObject(TransObject obj, boolean send) {
		obj.setModel(this, -1);
		transObjects.add(0, obj);
		if (obj instanceof BulletObject) {
			transMap.add((BulletObject) obj);
		}
		if (send && server != null && obj instanceof BulletObject) {
			server.sendFire((BulletObject)obj);
		}
	}
	
	public Intersection backgroundCollision(FgObject obj, float tx, float ty) {
		// check collision with map
		Intersection r = map.intersects(obj.getPosition(), tx, ty);
		if (r != null && obj == focusObj) {
			forceUpdate = true;
		}
		return r;
	}
	
	public void foregroundCollision(ShipObject ship) {
		ArrayList<BulletObject> l = transMap.get(ship.getX(), ship.getY(), ship.getRadius());
		if (l != null) {
			for (TransObject obj : l) {
				if (obj.getFreq() != ship.getFreq()) {
					// FIXME set remove, do something interesting
					System.out.println("impact!!");
				}
			}
		}
	}
	
	public void setServer(ServerRunnable server) {
		// TODO should probably clear here, or pass in constructor
		this.server = server;
	}
	
	public MapBgObject getMap() {
		return map;
	}
	
	public void updateMapTile(int x, int y, int action) {
		if (server != null) {
			server.sendMapTileReq(x, y, action);
		} else {
			map.updateMapTile(x, y, action);
		}
	}
	
	@Override
	public String toString() {
		float serverTime = 0;
		if (server != null) {
			serverTime = server.getTime() / NS_IN_S;
		}
		return String.format("Model[id=%d pos=%s objs=%d,%d t=%.1f st=%.1f %s]", 
				id,
				focusObj.getPosition(),
				objects.size(), transObjects.size(), 
				updateTime, 
				serverTime, 
				actions);
	}
	
}

