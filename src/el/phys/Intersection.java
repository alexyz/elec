package el.phys;


/**
 * An intersection and reflection of a rectangle
 */
public class Intersection {
	/**
	 * Intersection delta
	 */
	public float itx, ity;
	/**
	 * Reflection delta
	 */
	public float rtx, rty;
	/**
	 * Reflection vector modifier (either 1 or -1)
	 */
	public float vx, vy;
	@Override
	public String toString() {
		return String.format("Ref[i=%f,%f r=%f,%f v=%f,%f", itx, ity, rtx, rty, vx, vy);
	}
}
