package el.fg;

import static el.phys.FloatMath.*;

import java.awt.Graphics2D;
import java.awt.Image;
import el.ClientMain;
import el.phys.Circle;

/**
 * Simple subclass of moving object, can draw a ship and spawn bullets
 */
public class Ship extends MovingFgObject {
	private final Image i;
	
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
    	super(new Circle(mx, my, 24));
    	i = ClientMain.getImage("/img/ship1.png");
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
    
    @Override
	protected void paintAuto(Graphics2D g) {
        g.rotate(f);
        g.drawImage(i,(int)-c.r,(int)-c.r,null);
    }
    
    @Override
	public void left() {
    	//fd -= 0.1;
    	f -= pi / 30f;
    }
    
    @Override
	public void right() {
    	//fd += 0.1;
    	f += pi / 30f;
    }
    
    @Override
	public void up() {
    	accel(2f);
    }
    
    @Override
	public void down() {
    	accel(-2f);
    }
    
    @Override
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
    
    @Override
	public String toString() {
    	return String.format("Ship[%.0f]", energy) + super.toString();
    }
    
}