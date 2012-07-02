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
	/**
	 * intersection parameter
	 */
	public float p;
	@Override
	public String toString() {
		return String.format("Is[p=%f i=%.1f,%.1f r=%.1f,%f v=%.1f,%.1f]", p, itx, ity, rtx, rty, vx, vy);
	}
}
