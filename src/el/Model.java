package el;

import java.io.PrintStream;
import java.util.*;

import el.phys.*;
import el.phys.cs.CSIntersect;
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
	
	static final PrintStream out = System.out; 
	public static final int centrex = 500000, centrey = 500000;
	
	//
	// constants
	//
	
	/**
	 * permanent foreground objects (ships)
	 */
	private final ArrayList<FgObject> objects = new ArrayList<FgObject>();
	/**
	 * transient foreground objects (bullets)
	 */
	private final LinkedList<FgObject> transObjects = new LinkedList<FgObject>();
	private final ActionMap actionMap = new ActionMap();
	private final ArrayList<FgRunnable> actions = new ArrayList<FgRunnable>();
	private final FgObject modelObj = new ModelObject();
	private final long startTime = System.nanoTime();
	/**
	 * The map (transients may collide with it)
	 */
	private final BgObject map = new MapBgObject();
	/**
	 * The background (non interacting)
	 */
	private final BgObject stars = new StarBgObject();
	private final List<BgObject> bgObjects = Arrays.asList(stars, map);
	/**
	 * Collision detection
	 */
	private final Intersect i = new CSIntersect();
	
	//
	// variables
	//
	
	private float elapsedTime;
	private FgObject focusObj = modelObj;
	private long servertime = 0;
	private ServerThread serverThread;
	
	// need two layers
	public Model() {
		// TODO request this from server
		addFgObject(new Ship(centrex - 20, centrey));
		addFgObject(new Ship(centrex + 20, centrey));
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
	
	void update() {
		float time = (System.nanoTime() - this.startTime) / 1000000000.0f;
		float timeDelta = time - this.elapsedTime;
		elapsedTime = time;
		
		// apply actions for focused object
		if (actions.size() > 0) {
			for (FgRunnable r : actions) {
				r.run(focusObj);
			}
		}
		
		for (FgObject o : objects) {
			o.update(time, timeDelta);
		}
		
		if (transObjects.size() > 0) {
			Iterator<FgObject> i = transObjects.iterator();
			while (i.hasNext()) {
				FgObject o = i.next();
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
	
	List<FgObject> getTransFgObjects() {
		return transObjects;
	}
	
	void action(String name) {
		FgRunnable action = actionMap.get(name);
		if (action == null)
			throw new RuntimeException(name);
		if (!actions.contains(action))
			actions.add(action);
	}
	
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
	
	public void addFgObject(FgObject obj) {
		obj.setModel(this);
		objects.add(obj);
	}
	
	public void addTransObject(FgObject obj) {
		out.println("addTransObject " + obj);
		
		if (serverThread == null) {
			obj.setModel(this);
			transObjects.add(obj); 
			
		} else {
			// TODO inform server
			long t = serverThread.getServerTime();
			String msg = String.format("%d %f addTrans %s", t, getTime(), obj.write());
			out.println("  --> "  + msg);
			serverThread.post(msg);
			// simulate lag
			String[] tk = msg.split(" ");
			Bullet o = Bullet.read(0, tk, 4);
			o.setModel(this);
			o.update(getTime(), 0);
			transObjects.add(o);
			out.println("  object is " + o);
			
			// Client2 [servertime] [localtime] addTrans Ordnance type endt dx dy x y
			// need to include intended server time 
			// need to translate time
			// wait for it to come back in
		}
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
	
	
	public void setServerThread(ServerThread st) {
		this.serverThread = st;
	}
}
