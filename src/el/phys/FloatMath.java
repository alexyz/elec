package el.phys;

/**
 * Float precision math methods
 */
public class FloatMath {
	
	private FloatMath() {
		//
	}
	
	public static final float pi = (float) Math.PI;
	public static final float twopi = (float) (Math.PI * 2.0);
	public static final float halfpi = (float) (Math.PI / 2.0);
	public static final float quarterpi = (float) (Math.PI / 4.0);
	
	/**
	 * float sine
	 */
	public static float sin(float a) {
		return (float) StrictMath.sin(a);
	}
	
	/**
	 * float cosine
	 */
	public static float cos(float a) {
		return (float) StrictMath.cos(a);
	}
	
	public static float sqrt(float n) {
		return (float) StrictMath.sqrt(n);
	}
	
	/** float power */
	public static float pow(float n, float e) {
		return (float) StrictMath.pow(n, e);
	}
	
	public static float hypot(float x, float y) {
		return (float) StrictMath.hypot(x,y);
	}
	
	/**
	 * Returns atan2(x/y)
	 */
	public static float atan2(float x, float y) {
		return (float) StrictMath.atan2(x, y);
	}
	
	public static float deg(float r) {
		return (float) Math.toDegrees(r);
	}
	
	public static float rad(float d) {
		return (float) Math.toRadians(d);
	}
	
	public static float sq(float s) {
		return s * s;
	}
	
}
