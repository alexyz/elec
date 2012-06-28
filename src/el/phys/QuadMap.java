
package el.phys;

import java.util.*;

/**
 * A map of XY co-ordinates from 0-1M to list of objects
 * 
 * TODO some method to get objs within square rather than within radius? for benefit of map tiles
 */
public class QuadMap<T extends QuadMap.XYR> {
	
	public interface XYR {
		
		public int getX();
		
		public int getY();
		
		public int getR();
	}
	
	// total 20 bits, 12 for quad, 8 within quad
	private static final Integer[] ints = new Integer[1 << 12];
	private static final int quadbits = 8;
	
	static {
		for (int n = 0; n < ints.length; n++) {
			ints[n] = Integer.valueOf(n);
		}
	}
	
	public static void main(String[] args) {
		Random r = new Random();
		QuadMap<MyXY> q = new QuadMap<MyXY>();
		ArrayList<MyXY> l = new ArrayList<MyXY>();
		for (int n = 0; n < 10000; n++) {
			final int x = r.nextInt(10000) + 495000;
			final int y = r.nextInt(10000) + 495000;
			MyXY obj = new MyXY(x, y, 10);
			q.add(obj);
			l.add(obj);
		}
		System.out.println("quadmap is " + q);
		//ArrayList<MyXY> l = q.slowget(495000, 495000, 1000);
		//System.out.println("slow query returns " + l);
		ArrayList<MyXY> l2 = q.get(495000, 495000, 1000);
		System.out.println("fast query returns " + l2);
		for (int n = 0; n < 10; n++) {
			System.out.println("updating");
			for (MyXY obj : l) {
				int oldx = obj.x, oldy = obj.y;
				obj.x = r.nextInt(10000) + 495000;
				obj.y = r.nextInt(10000) + 495000;
				q.update(obj, oldx, oldy);
			}
			System.out.println("quadmap is " + q);
		}
	}
	
	/** map of x quadrant to map of y quadrant to list of objects */
	private final TreeMap<Integer, TreeMap<Integer, ArrayList<T>>> xmap;
	
	/** create a new quad map */
	public QuadMap() {
		xmap = new TreeMap<Integer, TreeMap<Integer, ArrayList<T>>>();
	}
	
	/**
	 * empty the quad map
	 */
	public void clear() {
		// prob not very gc friendly
		xmap.clear();
	}
	
	/**
	 * update the xy value of the given object
	 */
	public void update(T obj, int oldx, int oldy) {
		// TODO should compare quadrants not co-ordinates
		if (obj.getX() == oldx && obj.getY() == oldy) {
			//System.out.println("  no need to update");
		} else {
			remove(obj, oldx, oldy);
			add(obj);
		}
	}
	
	/**
	 * remove the given object
	 */
	public void remove(T obj) {
		remove(obj, obj.getX(), obj.getY());
	}
	
	/**
	 * remove the given object that had the given XY value when it was added/updated
	 */
	private void remove(T obj, int oldx, int oldy) {
		Integer oldqx = ints[oldx >> quadbits];
		Integer oldqy = ints[oldy >> quadbits];
		TreeMap<Integer, ArrayList<T>> ymap = xmap.get(oldqx);
		ArrayList<T> list = ymap.get(oldqy);
		if (!list.remove(obj)) {
			throw new RuntimeException("could not find " + obj + " in list " + list);
		}
		if (list.size() == 0) {
			ymap.remove(oldqy);
			if (ymap.size() == 0) {
				xmap.remove(oldqx);
			}
		}
	}
	
	/**
	 * add the given object
	 */
	public void add(T obj) {
		int x = obj.getX();
		int y = obj.getY();
		Integer qx = ints[x >> quadbits];
		Integer qy = ints[y >> quadbits];
		TreeMap<Integer, ArrayList<T>> ymap = xmap.get(qx);
		if (ymap == null) {
			xmap.put(qx, ymap = new TreeMap<Integer, ArrayList<T>>());
		}
		ArrayList<T> list = ymap.get(qy);
		if (list == null) {
			// default size is 10, too many?
			ymap.put(qy, list = new ArrayList<T>());
		}
		list.add(obj);
	}
	
	public ArrayList<T> get(QuadMap.XYR obj) {
		return get(obj.getX(), obj.getY(), obj.getR());
	}
	
	/**
	 * get all objects within r distance of given XY.
	 * returns null if none.
	 */
	public ArrayList<T> get(final int x, final int y, final int r) {
		ArrayList<T> ret = null;
		int xql = (x - r) >> quadbits;
		int xqh = ((x + r) >> quadbits);
		int yql = (y - r) >> quadbits;
		int yqh = ((y + r) >> quadbits);
		for (int xq = xql; xq <= xqh; xq++) {
			final TreeMap<Integer, ArrayList<T>> ymap = xmap.get(ints[xq]);
			if (ymap != null) {
				for (int yq = yql; yq <= yqh; yq++) {
					final ArrayList<T> list = ymap.get(ints[yq]);
					if (list != null) {
						for (T obj : list) {
							if (StrictMath.hypot(x - obj.getX(), y - obj.getY()) <= r + obj.getR()) {
								if (ret == null) {
									ret = new ArrayList<T>();
								}
								ret.add(obj);
							}
						}
					}
				}
			}
		}
		/*
		if (ret != null) {
			System.out.println(String.format("get [%d-%d, %d-%d] => %s", 
				xql * 256, 
				(xqh + 1) * 256 - 1, 
				yql * 256,
				(yqh + 1) * 256 - 1,
				ret));
		}
		*/
		return ret;
	}
	
	/**
	 * As get
	 */
	private ArrayList<T> getslow(final int x, final int y, final int r) {
		ArrayList<T> ret = new ArrayList<T>();
		for (TreeMap<Integer, ArrayList<T>> ymap : xmap.values()) {
			for (ArrayList<T> list : ymap.values()) {
				for (T obj : list) {
					if (StrictMath.hypot(x - obj.getX(), y - obj.getY()) <= r + obj.getR()) {
						ret.add(obj);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public String toString() {
		int l = 0, s = 0, t = 0;
		for (TreeMap<Integer, ArrayList<T>> ymap : xmap.values()) {
			for (ArrayList<T> list : ymap.values()) {
				l++;
				t += list.size();
				if (list.size() > s) {
					s = list.size();
				}
			}
		}
		return String.format("QM[ymaps=%d lists=%d maxlistsz=%d objs=%d]", xmap.size(), l, s, t);
	}
}

class MyXY implements QuadMap.XYR {
	
	public int x;
	public int y;
	public int r;
	
	public MyXY(int x, int y, int r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getR() {
		return r;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", x, y);
	}
}
