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
    	guns[0] = new Gun(Gun.Type.gun2, 0.2f, 150f, new Mount(10, -15, 0), new Mount(-10, -15, 0));
    	guns[1] = new Gun(Gun.Type.bomb2, 0.5f, 150f, new Mount(0, -15, 0));
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
    
    float thrusttime;
    
    @Override
    public void up() {
    	accel(2f);
    	float t = model.getTime();
    	// rate limit
    	if (t - thrusttime > 0.0625) {
    		thrusttime = t;
    		// thruster position
    		float x = getTransX(0, 20);
    		float y = getTransY(0, 20);
    		// thruster angle
    		float dx = getTransDX(pi, 75);
    		float dy = getTransDY(pi, 75);
    		model.addTransObject(new Exhaust(x, y, dx, dy, t));
    	}
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
    				// gun position, rotate the gun mount offsets
    				float x = getTransX(mount.x, mount.y);
    				float y = getTransY(mount.x, mount.y);
    				
    				// bullet velocity vector
    				float dx = getTransDX(mount.f, gun.velocity);
    				float dy = getTransDY(mount.f, gun.velocity);
    				
    				model.addTransObject(new Bullet(gun.type, t, x, y, dx, dy));
    			}
    		}
    	}
    }

    @Override
	public String toString() {
    	return String.format("Ship[%.0f]", energy) + super.toString();
    }
    
}