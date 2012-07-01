package el.fg;
import static el.phys.FloatMath.*;

import java.util.StringTokenizer;

import el.Model;
import el.phys.Intersection;

/**
 * Object with a position delta, facing angle and facing delta.
 */
public abstract class MovingObject extends FgObject  {
	
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
	 * Should detect collisions with background
	 */
	protected boolean collideBackground = true;
	
	/**
	 * Number of times collided
	 */
	protected int hit;
	
	/**
	 * players team (must be >= 1)
	 */
	protected int freq;
	
	public MovingObject() {
		//
	}
	
	public void setFreq(int freq) {
		this.freq = freq;
	}
	
	public int getFreq() {
		return freq;
	}
	
	@Override
	protected void readImpl(StringTokenizer tokens) {
		super.readImpl(tokens);
		// should really validate freq changes
		freq = Integer.parseInt(tokens.nextToken());
		vx = Float.parseFloat(tokens.nextToken());
		vy = Float.parseFloat(tokens.nextToken());
		fd = Float.parseFloat(tokens.nextToken());
		f = Float.parseFloat(tokens.nextToken());
	}
	
	@Override
	protected StringBuilder writeImpl(StringBuilder sb) {
		super.writeImpl(sb);
		sb.append(freq).append(" ");
		sb.append(vx).append(" ");
		sb.append(vy).append(" ");
		sb.append(fd).append(" ");
		sb.append(f).append(" ");
		return sb;
	}
	
	/**
	 * Accelerate (vx/vy) in facing direction (f) in pixels/sec
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
		
		if (collideBackground && (r = model.backgroundCollision(this, dx, dy)) != null) {
			if (reflect) {
				// reflected position
				x += r.rtx;
				y += r.rty;
				// change velocity direction
				vx *= r.vx;
				vy *= r.vy;
				
			} else {
				// hit position
				x += r.itx;
				y += r.ity;
				// zero velocity
				vx = 0f;
				vy = 0f;
			}
			
			hit++;
			collision();
			
		} else {
			// just update the position
			x += dx;
			y += dy;
		}
		
		if (x < 0 || x > Model.maxx || y < 0 || y > Model.maxx) {
			throw new RuntimeException("object out of bounds");
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
		return x + cos(f) * tx - sin(f) * ty;
	}
	
	/**
	 * get y position of an object emerging from this object at the given translation from origin
	 */
	protected float getTransY(float tx, float ty) {
		return y + sin(f) * tx + cos(f) * ty;
	}
	
	/**
	 * get x velocity of an object emerging from this object at the given angle and velocity in pixels per second
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
		return String.format("Moving[d=%.1f,%.1f fa=%.1f va=%.1f v=%.1f]", 
				vx, vy, deg(f), deg(angle()), velocity()) + super.toString();
	}
	
}

