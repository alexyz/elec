package el.fg;

import static el.phys.FloatMath.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.StringTokenizer;

import el.ClientFrame;

/**
 * Simple subclass of moving object, can draw a ship and spawn bullets
 */
public class ShipObject extends MovingObject {
	
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
	@Deprecated
	private final Gun[] guns;
	/**
	 * ships energy
	 */
	private float energy = 2000f;
	@Deprecated
	private float maxenergy = 2000f;
	@Deprecated
	private float recharge = 100f;
	/**
	 * last time thrusters were drawn
	 */
	private float thrusttime;
	
	public ShipObject(ShipType type, float mx, float my) {
		this.x = mx;
		this.y = my;
		this.radius = 24;
		
		// this should really be cloned from shiptype
		image = ClientFrame.getImage(type.img);
		maxv = type.maxv;
		xres = type.xres;
		yres = type.yres;
		rotres = type.rotres;
		guns = type.guns;
	}
	
	@Override
	protected void readImpl(StringTokenizer tokens) {
		super.readImpl(tokens);
		energy = Float.parseFloat(tokens.nextToken());
	}
	
	@Override
	protected StringBuilder writeImpl(StringBuilder sb) {
		super.writeImpl(sb);
		sb.append(energy).append(" ");
		return sb;
	}
	
	@Override
	public void update(float floatTime, float floatTimeDelta) {
		super.update(floatTime, floatTimeDelta);
		energy = Math.min(energy + recharge * floatTimeDelta, maxenergy); 
		// don't really need this
		model.foregroundCollision(this);
	}
	
	@Override
	public void paint(Graphics2D g) {
		int r = (int) radius;
		g.setColor(Color.gray);
		g.drawOval(-r, -r, r*2, r*2);
		
		if (energy < maxenergy) {
			g.setColor(Color.orange);
			g.drawString(String.format("%.1f", energy), r, -r);
		}
		
		AffineTransform t = g.getTransform();
		g.rotate(f);
		g.drawImage(image,(int)-radius,(int)-radius,null);
		g.setTransform(t);
	}
	
	@Override
	public void left(float t, float dt) {
		f -= pi * dt;
	}
	
	@Override
	public void right(float t, float dt) {
		f += pi * dt;
	}
	
	@Override
	public void up(float t, float dt) {
		accel(80f * dt);
		
		// rate limit
		if (t - thrusttime > 0.0625) {
			thrusttime = t;
			// thruster position
			float x = getTransX(0, 20);
			float y = getTransY(0, 20);
			// thruster angle
			float dx = getTransDX(pi, 75);
			float dy = getTransDY(pi, 75);
			
			model.addTransObject(new Exhaust(x, y, dx, dy, t), false);
		}
	}
	
	@Override
	public void down(float t, float dt) {
		accel(-80f * dt);
	}
	
	@Override
	public boolean fire(int n, float t, float dt) {
		Gun gun = guns[n];
		if (gun != null) {
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
					
					BulletObject bullet = new BulletObject(gun.type, t, x, y, dx, dy);
					bullet.setFreq(freq);
					model.addTransObject(bullet, true);
				}
			}
		}
		return false;
	}
	
	public void setEnergy(float energy) {
		this.energy = energy;
	}
	
	public float getEnergy() {
		return energy;
	}
	
	@Override
	public String toString() {
		return String.format("Ship[%.0f]", energy) + super.toString();
	}
	
}
