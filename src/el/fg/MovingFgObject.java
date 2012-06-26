package el.fg;
import static el.phys.FloatMath.*;

import java.util.StringTokenizer;

import el.phys.Circle;
import el.phys.Intersection;

/**
 * Object with a position delta, facing angle and facing delta.
 */
abstract class MovingFgObject extends FgObject  {
	
	/**
	 * Max velocity
	 */
	protected float maxv = 0f;
	/**
	 * X wind resistance (0=max 1=none)
	 */
	protected float xres = 1f;
	/**
	 * Y wind resistance
	 */
	protected float yres = 1f;
	/**
	 * Rotation resistance
	 */
	protected float rotres = 1f;
	
	/** movement vector in pixels/sec */
	protected float vx, vy;
	
	/** rotation vector in radians/sec */
	protected float fd;
	
	/** facing angle */
	protected float f;
	
	/**
	 * Should reflect on collision (requires collide=true)
	 */
	protected boolean reflect = true;
	
	/**
	 * Should detect collisions
	 */
	protected boolean collide = true;
	
	/**
	 * Number of times collided
	 */
	protected int hit;
	
	/**
	 * Create object with given location and radius
	 */
	public MovingFgObject(Circle c) {
		super(c);
	}
	
	@Override
	public void read(StringTokenizer tokens) {
		super.read(tokens);
		vx = Float.parseFloat(tokens.nextToken());
		vy = Float.parseFloat(tokens.nextToken());
		fd = Float.parseFloat(tokens.nextToken());
		f = Float.parseFloat(tokens.nextToken());
	}
	
	@Override
	public StringBuilder write(StringBuilder sb) {
		super.write(sb);
		sb.append(vx).append(" ");
		sb.append(vy).append(" ");
		sb.append(fd).append(" ");
		sb.append(f).append(" ");
		return sb;
	}
	
	/**
	 * Accelerate in facing direction in pixels/sec
	 */
	protected void accel(float d) {
		vx += d * sin(f);
		vy -= d * cos(f);
		
		if (maxv > 0f && velocity() > maxv) {
			float a = atan2(vx,vy);
			vx = sin(a)*maxv;
			vy = cos(a)*maxv;
		}
	}
	
	/** 
	 * Update location, check for collision, reflect or stop
	 */
	@Override
	public void update(float floatTime, float floatTimeDelta) {
		float dx = vx * floatTimeDelta;
		float dy = vy * floatTimeDelta;
		Intersection r;
		if (collide && (r = model.intersectbg(this, dx, dy)) != null) {
			hit++;
			collision();
			if (reflect) {
				// reflected position
				c.x += r.rtx;
				c.y += r.rty;
				// change velocity direction
				vx *= r.vx;
				vy *= r.vy;
				
			} else {
				// hit position
				c.x += r.itx;
				c.y += r.ity;
				// zero velocity
				vx = 0f;
				vy = 0f;
			}
			
		} else {
			// just update the position
			c.x += dx;
			c.y += dy;
		}
		
		// erode velocity
		f = (f + fd * floatTimeDelta) % twopi;
		if (xres != 1f)
			vx *= pow(xres, floatTimeDelta);
		if (yres != 1f)
			vy *= pow(yres, floatTimeDelta);
		if (rotres != 1f)
			fd *= pow(rotres, floatTimeDelta);
	}
	
	/**
	 * get x position of an object emerging from this object at the given translation from origin
	 */
	protected float getTransX(float tx, float ty) {
		return c.x + cos(f) * tx - sin(f) * ty;
	}
	
	/**
	 * get y position of an object emerging from this object at the given translation from origin
	 */
	protected float getTransY(float tx, float ty) {
		return c.y + sin(f) * tx + cos(f) * ty;
	}
	
	/**
	 * get x velocity of an object emerging from this object at the given angle and speed
	 */
	protected float getTransDX(float tf, float tv) {
		return sin(f + tf) * tv + vx;
	}
	
	/**
	 * get y velocity of an object emerging from this object at the given angle and speed
	 */
	protected float getTransDY(float tf, float tv) {
		return -cos(f + tf) * tv + vy;
	}
	
	/**
	 * Called on collision.
	 * Subclasses might care, e.g. if a bomb can only bounce n times.
	 */
	protected void collision() {
		//
	}
	
	/**
	 * Returns the current velocity
	 */
	protected float velocity() {
		return hypot(vx, vy);
	}
	
	/**
	 * Returns the current velocity angle (not the facing angle!)
	 */
	protected float angle() {
		return atan2(vx, -vy);
	}
	
	@Override
	public String toString() {
		return String.format("MovingFgObject[d=%.1f,%.1f fa=%.1f va=%.1f v=%.1f]", 
				vx, vy, deg(f), deg(angle()), velocity()) + super.toString();
	}
	
}

