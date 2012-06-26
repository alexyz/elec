package el;

import java.io.PrintStream;
import java.util.*;

import el.phys.*;
import el.phys.cs.CSIntersect;
import el.serv.ServerThread;
import el.bg.*;
import el.fg.*;

/** 
 * Client model.
 * Play area is 1M pixels squared. Co-ordinates are represented by floats, 
 * so any more than 1M would start to lose precision.
 * 
 * By convention, variables starting with:
 * sx, sy or vx, vy - screen co-ordinates
 * mx, my - model co-ordinates
 * qx, qy - model quadrant co-ordinates (for some quadrant size)
 * dx, dy - deltas in px/sec
 * 
 * This class is not thread safe. Concurrent access must be externally synchronized
 * (i.e. from AWT event thread and ServerThread instance).
 */
public class Model {
	
	/** Nanoseconds in second */
	private static final float NS_IN_S = 1000000000.0f;

	/** centre co-ordinates */
	public static final int centrex = 500000, centrey = 500000;
	
	private static final PrintStream out = System.out;
	
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
	private final ArrayList<TransMovingFgObject> transObjects = new ArrayList<TransMovingFgObject>();
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
	/**
	 * Collision detection function
	 */
	private final Intersect i = new CSIntersect();
	
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
	 * Issue a server update on next call to update()
	 */
	private boolean forceUpdate;
	/**
	 * currently focused object
	 */
	private FgObject focusObj = modelObj;
	/**
	 * the server, if any
	 */
	private ServerThread server;
	/**
	 * users ship id, if any
	 */
	private int selfId;
	
	public Model() {
		//
	}
	
	public int getId() {
		return selfId;
	}
	
	/**
	 * enter the user into the game
	 */
	public void enter(int id) {
		Ship ship = new Ship(ShipType.types[0], centrex - 20, centrey);
		this.selfId = id;
		focusObj = ship;
		// TODO need to put focused object on top
		addFgObject(ship, id);
		forceUpdate = true;
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
			this.selfId = 0;
		}
	}
	
	/**
	 * focus next foreground object. in live game you should only be able to
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
		if (actions.size() > 0 && (focusObj == modelObj || focusObj.getId() == selfId)) {
			// FIXME don't apply actions unless it's players own ship
			for (FgRunnable r : actions) {
				r.run(focusObj);
			}
			if ((floatTime - lastUpdate) > 0.125) {
				forceUpdate = true;
			}
		}

		for (FgObject o : objects) {
			o.update(floatTime, floatTimeDelta);
		}
		
		// send to server - after possible reflect
		if (server != null && focusObj instanceof Ship && forceUpdate) {
			// update focused ship on server
			server.update((Ship) focusObj);
			lastUpdate = floatTime;
			forceUpdate = false;
		}
		
		if (transObjects.size() > 0) {
			Iterator<TransMovingFgObject> i = transObjects.iterator();
			while (i.hasNext()) {
				TransMovingFgObject o = i.next();
				// may inflict damage to objects
				o.update(floatTime, floatTimeDelta);
				if (o.remove()) {
					i.remove();
				}
			}
		}
		
	}
	
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
	
	List<TransMovingFgObject> getTransFgObjects() {
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
		// for benefit of client that has just entered
		forceUpdate = true;
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
	
	public void addTransObject(TransMovingFgObject obj, boolean send) {
		obj.setModel(this, -1);
		transObjects.add(0, obj); 
		if (send && server != null && obj instanceof Bullet) {
			server.fireReq((Bullet)obj);
		}
	}
	
	public Intersect getIntersect() {
		return i;
	}
	
	public Intersection intersectbg(FgObject obj, float tx, float ty) {
		// check collision with map
		Intersection r = map.intersects(i, obj.getPosition(), tx, ty);
		if (r != null && obj == focusObj) {
			forceUpdate = true;
		}
		return r;
	}
	
	public Intersection intersectfg(Circle c, float tx, float ty, float dam) {
		// FIXME do this
		
		// check collision with ships
		
		return null;
	}
	
	public void setServer(ServerThread server) {
		this.server = server;
		// should probably clear here
	}
	
	public MapBgObject getMap() {
		return map;
	}
	
	public void updateMap(int x, int y, int action) {
		if (server != null) {
			server.updateMapReq(x, y, action);
		} else {
			map.place(x, y, action);
		}
	}
	
	@Override
	public String toString() {
		float serverTime = 0;
		if (server != null) {
			serverTime = server.getTime() / NS_IN_S;
		}
		return String.format("Model[id=%d pos=%s objs=%d,%d t=%.1f st=%.1f %s]", 
				selfId,
				focusObj.getPosition(),
				objects.size(), transObjects.size(), 
				updateTime, 
				serverTime, 
				actions);
	}
	
}
