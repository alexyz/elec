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
	
	/**
	 * image used to draw ship
	 */
	private final Image image;
	/**
	 * Last time guns were fired
	 */
	private final float[] guntime = new float[4];
	/**
	 * Guns and mountings
	 */
	private final Gun[] guns;
	/**
	 * ships energy
	 */
	private float energy = 2000f;
	/**
	 * last time thrusters were drawn
	 */
	private float thrusttime;
    
    public Ship(ShipType type, int mx, int my) {
    	super(new Circle(mx, my, 24));
    	
    	image = ClientMain.getImage(type.img);
    	maxv = type.maxv;
    	xres = type.xres;
    	yres = type.yres;
    	rotres = type.rotres;
    	guns = type.guns;
    }
    
    @Override
	public int read(String[] args, int i) {
    	i = super.read(args, i);
    	energy = Float.parseFloat(args[i++]);
    	return i;
    }
    
    @Override
	public void write(StringBuilder sb) {
    	super.write(sb);
    	sb.append(energy).append(" ");
    }
    
    @Override
	protected void paintAuto(Graphics2D g) {
        g.rotate(f);
        g.drawImage(image,(int)-c.r,(int)-c.r,null);
    }
    
    @Override
	public void left() {
    	f -= pi / 32f;
    }
    
    @Override
	public void right() {
    	f += pi / 32f;
    }
    
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
