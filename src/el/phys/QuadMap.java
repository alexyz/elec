package el.phys;

import java.util.*;

/**
 * A map of integer XY co-ordinates from 0-1M to list of objects.
 * Objects can be retrieved by proximity to a point.
 */
// Note: there should be NO autoboxing in this class.
public class QuadMap<T> {
	
	/** interface for getting the x, y and radius of and object in the map */
	public interface Key<T> {
		/** get x position */
		public int getX(T obj);
		/** get y position */
		public int getY(T obj);
		/** get radius. for bombs this should be prox radius, not physical radius */
		public int getR(T obj);
	}
	
	//
	// static fields
	//
	
	// total 20 bits, 12 for quad, 8 within quad
	/** number of quadrants (log2) */
	private static final int quadbits = 12;
	/** integer object cache */
	private static final Integer[] ints = new Integer[1 << quadbits];
	/** number of locations within quadrant (log2) */
	private static final int subquadbits = 8;
	
	static {
		// could make these on demand instead, but there's not that many of them
		for (int n = 0; n < ints.length; n++) {
			ints[n] = Integer.valueOf(n);
		}
	}
	
	//
	// static methods
	//
	
	/** return quadrant for x or y co-ordinate */
	private static int quad(final int n) {
		return n >> subquadbits;
	}
	
	/** return quadrant key for x or y co-ordinate */
	private static Integer quadKey(final int n) {
		return ints[n >> subquadbits];
	}
	
	//
	// instance fields
	//
	
	/** map of x quadrant to map of y quadrant to list of objects */
	private final TreeMap<Integer, TreeMap<Integer, ArrayList<T>>> xmap;
	/** key function */
	private final Key<T> key;
	
	/** create a new quad map */
	public QuadMap(QuadMap.Key<T> key) {
		this.key = key;
		this.xmap = new TreeMap<Integer, TreeMap<Integer, ArrayList<T>>>();
	}
	
	//
	// instance methods
	//
	
	/**
	 * empty the quad map
	 */
	public void clear() {
		// probably not very gc friendly
		xmap.clear();
	}
	
	/**
	 * update the xy value of the given object
	 */
	public void update(final T obj, final int oldx, final int oldy) {
		// only update if quadrant has changed
		if (quad(key.getX(obj)) != quad(oldx) || quad(key.getY(obj)) != quad(oldy)) {
			remove(obj, quadKey(oldx), quadKey(oldy));
			add(obj);
		}
	}
	
	/**
	 * remove the given object
	 */
	public void remove(final T obj) {
		remove(obj, quadKey(key.getX(obj)), quadKey(key.getY(obj)));
	}
	
	/**
	 * remove the given object that was in the given quadrant when it was
	 * added/updated
	 */
	private void remove(final T obj, final Integer qoldx, final Integer qoldy) {
		TreeMap<Integer, ArrayList<T>> ymap = xmap.get(qoldx);
		ArrayList<T> list = ymap.get(qoldy);
		if (!list.remove(obj)) {
			throw new RuntimeException("could not find " + obj + " in list " + list);
		}
		
		// remove list and map if now empty
		if (list.size() == 0) {
			ymap.remove(qoldy);
			if (ymap.size() == 0) {
				xmap.remove(qoldx);
			}
		}
	}
	
	/**
	 * add the given object
	 */
	public void add(final T obj) {
		Integer qx = quadKey(key.getX(obj));
		TreeMap<Integer, ArrayList<T>> ymap = xmap.get(qx);
		if (ymap == null) {
			xmap.put(qx, ymap = new TreeMap<Integer, ArrayList<T>>());
		}
		
		Integer qy = quadKey(key.getY(obj));
		ArrayList<T> list = ymap.get(qy);
		if (list == null) {
			// default size is 10, too many?
			ymap.put(qy, list = new ArrayList<T>());
		}
		
		list.add(obj);
	}
	
	/**
	 * get all objects within r distance of given XY. returns null if none.
	 */
	public ArrayList<T> get(final int x, final int y, final int r) {
		final int xql = quad(x - r);
		final int xqh = quad(x + r);
		final int yql = quad(y - r);
		final int yqh = quad(y + r);
		if (xql < 0 || xql > ints.length || yql < 0 || yqh > ints.length) {
			throw new RuntimeException(String.format("quadmap get out of bounds: %d, %d, %d", x, y, r));
		}
		
		ArrayList<T> ret = null;
		
		for (int xq = xql; xq <= xqh; xq++) {
			final TreeMap<Integer, ArrayList<T>> ymap = xmap.get(ints[xq]);
			if (ymap != null) {
				for (int yq = yql; yq <= yqh; yq++) {
					final ArrayList<T> list = ymap.get(ints[yq]);
					if (list != null) {
						for (T obj : list) {
							if (StrictMath.hypot(x - key.getX(obj), y - key.getY(obj)) <= r + key.getR(obj)) {
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
		 * if (ret != null) {
		 * System.out.println(String.format("get [%d-%d, %d-%d] => %s", xql *
		 * 256, (xqh + 1) * 256 - 1, yql * 256, (yqh + 1) * 256 - 1, ret)); }
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
					if (StrictMath.hypot(x - key.getX(obj), y - key.getY(obj)) <= r + key.getR(obj)) {
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
