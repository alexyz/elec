package el;
import java.io.PrintStream;
import java.util.*;

import el.phys.Circle;
import el.phys.IntersectionFunction;
import el.phys.Intersection;
import el.phys.Trans;
import el.phys.cs.CSIntersect;
import el.bg.*;
import el.fg.*;
import static el.FMath.*;

/** 
 * 1M squared.
 * 
 * By convention, variables starting with:
 * sx, sy or vx, vy - screen co-ordinates
 * mx, my - model co-ordinates
 * qx, qy - model quadrant co-ordinates (for some quadrant size)
 * dx, dy - deltas in px/sec
 * 
 * TODO radar
 * 
 * */
public class Model {
	
	static final PrintStream log = System.out; 
	public static final int centrex = 500000, centrey = 500000;
	
	//
	// constants
	//
	
	private final ArrayList<FgObject> objects = new ArrayList<FgObject>();
	private final LinkedList<FgObject> transObjects = new LinkedList<FgObject>();
	private final HashMap<String,Runnable> actionMap = new HashMap<String,Runnable>();
	private final ArrayList<Runnable> actions = new ArrayList<Runnable>();
	private final FgObject modelObj = new ModelObject();
	private final long startt = System.nanoTime();
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
	private final IntersectionFunction i = new CSIntersect();
	
	//
	// variables
	//
	
	private float elapsedt;
	private FgObject focusObj = modelObj;
	private long servertime = 0;
	private ServerThread st;
	
	// need two layers
	public Model() {
		// TODO a separate actions object
		actionMap.put("up", new Runnable() {
			public void run() {
				focusObj.up();
			}
			public String toString() {
				return "up";
			}
		});
		actionMap.put("down", new Runnable() {
			public void run() {
				focusObj.down();
			}
			public String toString() {
				return "down";
			}
		});
		actionMap.put("left", new Runnable() {
			public void run() {
				focusObj.left();
			}
			public String toString() {
				return "left";
			}
		});
		actionMap.put("right", new Runnable() {
			public void run() {
				focusObj.right();
			}
			public String toString() {
				return "right";
			}
		});
		actionMap.put("fire1", new Runnable() {
			public void run() {
				focusObj.fire(0);
			}
			public String toString() {
				return "fire1";
			}
		});
		actionMap.put("fire2", new Runnable() {
			public void run() {
				focusObj.fire(1);
			}
			public String toString() {
				return "fire2";
			}
		});
		actionMap.put("fire3", new Runnable() {
			public void run() {
				focusObj.fire(2);
			}
			public String toString() {
				return "fire3";
			}
		});
		// TODO request this from server
		addFgObject(new Ship(centrex - 20, centrey));
		addFgObject(new Ship(centrex + 20, centrey));
	}
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
		float t = (System.nanoTime() - this.startt) / 1000000000.0f;
		float td = t - this.elapsedt;
		elapsedt = t;
		
		if (actions.size() > 0) {
			for (Runnable r : actions)
				r.run();
		}
		
		for (FgObject o : objects) {
			o.update(t, td);
		}
		
		if (transObjects.size() > 0) {
			Iterator<FgObject> i = transObjects.iterator();
			while (i.hasNext()) {
				FgObject o = i.next();
				// may inflict damage to objects
				o.update(t, td);
				if (o.remove())
					i.remove();
			}
		}
		
	}
	
	public float getTime() {
		return elapsedt;
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
		Runnable action = actionMap.get(name);
		if (action == null)
			throw new RuntimeException(name);
		if (!actions.contains(action))
			actions.add(action);
	}
	void unaction(String name) {
		Runnable action = actionMap.get(name);
		if (action == null)
			throw new RuntimeException(name);
		actions.remove(action);
	}

	@Override
	public String toString() {
		return String.format("Model[x,y=%d,%d t=%.2f objs=%d,%d]%s", 
				getX(), getY(), elapsedt, objects.size(), transObjects.size(), actions);
	}
	public FgObject getFocus() {
		return focusObj;
	}
	public void addFgObject(FgObject obj) {
		obj.setModel(this);
		objects.add(obj);
	}
	public void addTransObject(FgObject obj) {
		log.println("addTransObject " + obj);
		
		if (st == null) {
			obj.setModel(this);
			transObjects.add(obj); 
			
		} else {
			// TODO inform server
			long t = st.getServerTime();
			String msg = String.format("%d %f addTrans %s", t, getTime(), obj.write());
			log.println("  --> "  + msg);
			st.post(msg);
			// simulate lag
			String[] tk = msg.split(" ");
			Ordnance o = Ordnance.read(0, tk, 4);
			o.setModel(this);
			o.update(getTime(), 0);
			transObjects.add(o);
			log.println("  object is " + o);
			
			// Client2 [servertime] [localtime] addTrans Ordnance type endt dx dy x y
			// need to include intended server time 
			// need to translate time
			// wait for it to come back in
		}
	}
	
	public IntersectionFunction getIntersect() {
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
		this.st = st;
	}
}
