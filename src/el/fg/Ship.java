package el.fg;

import static el.phys.FloatMath.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.*;

import el.phys.Circle;


/**
 * Simple subclass of moving object, can draw a ship and spawn bullets
 */
public class Ship extends MovingFgObject {
	// shape
	static final int s = 32;
	static final int[] xp = { 0, s / 2, 0, - s / 2 };
	static final int[] yp = { - s / 2, s / 2, s / 4, s / 2 };
	static final Polygon p = new Polygon(xp, yp, xp.length);
	
	/**
	 * Last time guns were fired
	 */
	private final float[] guntime = new float[4];
	
	/**
	 * Guns and mountings
	 */
	private final Gun[] guns = new Gun[4];
	
	private float energy = 2000f;
    
    public Ship(int mx, int my) {
    	super(new Circle(mx, my, s / 2));
    	init();
    }
    
    private void init() {
    	maxv = 1000f;
    	xres = 0.9f;
    	yres = 0.9f;
    	rotres = 0.25f;
    	guns[0] = new Gun(Gun.Type.gun2, 0.2f, 150f, new Mount(8, 0, 0), new Mount(-8, 0, 0));
    	guns[1] = new Gun(Gun.Type.bomb2, 0.5f, 150f, new Mount(0, -8, 0));
    	guns[2] = new Gun(Gun.Type.bomb4, 1f, 200f, new Mount(0, -8, 0));
    }
    
    protected void paintAuto(Graphics2D g) {
        g.rotate(f);
        g.setColor(Color.blue);
        g.fill(p);
    }
    
    public void left() {
    	//fd -= 0.1;
    	f -= pi / 30f;
    }
    
    public void right() {
    	//fd += 0.1;
    	f += pi / 30f;
    }
    
    public void up() {
    	accel(2f);
    }
    
    public void down() {
    	accel(-2f);
    }
    
    public void fire(int n) {
    	Gun gun = guns[n];
    	if (gun != null) {
    		float t = model.getTime();
    		// rate limit
    		if (t - guntime[n] > gun.period) {
    			guntime[n] = t;

    			for (Mount mount : gun.mounts) {
    				float bdx = sin(f + mount.f) * gun.velocity + vx;
    				float bdy = -cos(f + mount.f) * gun.velocity + vy;
    				
    				// rotate the gun mount offsets
    				float x = c.x + cos(f) * mount.x - sin(f) * mount.y;
    				float y = c.y + sin(f) * mount.x + cos(f) * mount.y;
    				
    				model.addTransObject(new Bullet(gun.type, t, x, y, bdx, bdy));
    			}
    		}
    	}
    }
    
    public String toString() {
    	return String.format("Ship[%.0f]", energy) + super.toString();
    }
    
}