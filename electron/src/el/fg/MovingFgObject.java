package el.fg;
import static el.FMath.*;
import el.phys.Circle;
import el.phys.Intersection;

/**
 * Object with a position delta, facing angle and facing delta.
 */
abstract class MovingFgObject extends FgObject  {

	/**
	 * Max velocity
	 */
	float maxv = 0f;
	/**
	 * X wind resistance (0=max 1=none)
	 */
	float xres = 1f;
	/**
	 * Y wind resistance
	 */
	float yres = 1f;
	/**
	 * Rotation resistance
	 */
	float rotres = 1f;
	
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
    
	public MovingFgObject(Circle c) {
		super(c);
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
     * FIXME needs to use t, not dt, in case of lag
     */
    @Override
	public void update(float _t, float dt) {
        float dx = vx * dt;
        float dy = vy * dt;
        // FIXME intersect fg needs to be optional
        Intersection r;
		if (collide && (r = model.intersectbg(c, dx, dy)) != null) {
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
        f = (f + fd * dt) % twopi;
        if (xres != 1f)
    		vx *= pow(xres, dt);
    	if (yres != 1f)
    		vy *= pow(yres, dt);
    	if (rotres != 1f)
    		fd *= pow(rotres, dt);
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
    public float velocity() {
    	return hypot(vx, vy);
    }
    
    /**
     * Returns the current velocity angle (not the facing angle!)
     */
    public float angle() {
    	return atan2(vx, -vy);
    }
    
    public String toString() {
    	return String.format("MovingFgObject[d=%2.0f,%2.0f f=%3.0f a=%3.0f v=%2.0f]", 
    			vx, vy, deg(f), deg(angle()), velocity()) + super.toString();
    }
    
}

