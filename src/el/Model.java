package el;

import java.io.PrintStream;
import java.util.*;

import el.phys.*;
import el.phys.cs.CSIntersect;
import el.serv.Server;
import el.serv.ServerThread;
import el.bg.*;
import el.fg.*;

/** 
 * 1M squared.
 * 
 * By convention, variables starting with:
 * sx, sy or vx, vy - screen co-ordinates
 * mx, my - model co-ordinates
 * qx, qy - model quadrant co-ordinates (for some quadrant size)
 * dx, dy - deltas in px/sec
 */
public class Model {
	
	public static final int centrex = 500000, centrey = 500000;
	
	private static final PrintStream out = System.out;
	
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
	
	/**
	 * time of last update
	 */
	private float elapsedTime;
	/**
	 * currently focused object
	 */
	private FgObject focusObj = modelObj;
	/**
	 * the server, if any
	 */
	private Server server;
	
	public Model() {
		//
	}
	
	/**
	 * enter the user into the game
	 */
	public void enter(int id) {
		Ship ship = new Ship(ShipType.types[0], centrex - 20, centrey);
		focusObj = ship;
		// TODO need to put focused object on top
		addFgObject(ship, id);
	}
	
	/**
	 * remove the user from the game
	 */
	public void spec() {
		if (focusObj != null) {
			objects.remove(focusObj);
			focusObj = modelObj;
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
	
	float lastUpdate;
	
	/**
	 * update the world
	 */
	void update() {
		float time = (System.nanoTime() - this.startTime) / 1000000000.0f;
		float timeDelta = time - this.elapsedTime;
		elapsedTime = time;
		
		// apply actions for focused object
		if (actions.size() > 0) {
			for (FgRunnable r : actions) {
				r.run(focusObj);
			}
			if (server != null && (elapsedTime - lastUpdate) > 0.5 && focusObj instanceof Ship) {
				// update server - only if there is currently an action
				server.update((Ship) focusObj);
				lastUpdate = elapsedTime;
			}
		}
		
		for (FgObject o : objects) {
			o.update(time, timeDelta);
		}
		
		if (transObjects.size() > 0) {
			Iterator<TransMovingFgObject> i = transObjects.iterator();
			while (i.hasNext()) {
				TransMovingFgObject o = i.next();
				// may inflict damage to objects
				o.update(time, timeDelta);
				if (o.remove()) {
					i.remove();
				}
			}
		}
		
	}
	
	public float getTime() {
		return elapsedTime;
	}
	
	/**
	 * Get model x location of currently focused object
	 */
	int getX() {
		return focusObj.getX();
	}
	
	/**
	 * Get model y location of currently focused object
	 */
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
		if (action == null)
			throw new RuntimeException(name);
		actions.remove(action);
	}

	@Override
	public String toString() {
		return String.format("Model[x,y=%d,%d t=%.2f objs=%d,%d]%s", 
				getX(), getY(), elapsedTime, objects.size(), transObjects.size(), actions);
	}
	
	public FgObject getFocus() {
		return focusObj;
	}
	
	public void addFgObject(FgObject obj, int id) {
		obj.setModel(this, id);
		objects.add(obj);
	}
	
	public void updateFgObject(int id, String[] data, int i) {
		for (FgObject obj : objects) {
			if (obj.getId() == id) {
				obj.read(data, i);
				return;
			}
		}
		System.out.println("could not find object " + id);
	}
	
	public void addTransObject(TransMovingFgObject obj) {
		obj.setModel(this, -1);
		transObjects.add(0, obj); 
		
		// TODO send to server
	}
	
	public Intersect getIntersect() {
		return i;
	}
	
	public Intersection intersectbg(Circle c, float tx, float ty) {
		// check collision with map
		return map.intersects(i, c, tx, ty);
	}
	
	public Intersection intersectfg(Circle c, float tx, float ty, float dam) {
		// FIXME do this
		
		// check collision with ships
		
		return null;
	}
	
	public void setServer(Server server) {
		this.server = server;
	}
	
	public MapBgObject getMap() {
		return map;
	}
}
