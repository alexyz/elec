package el.fg;

import static el.phys.FloatMath.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.StringTokenizer;

import el.ClientMain;
import el.phys.Circle;

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
		image = ClientMain.getImage(type.img);
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
	protected void paintAuto(Graphics2D g) {
		int r = (int) radius;
		g.setColor(Color.gray);
		g.drawOval(-r, -r, r*2, r*2);
		
		if (energy < maxenergy) {
			g.setColor(Color.orange);
			g.drawString(String.format("%.1f", energy), r, -r);
		}
		
		g.rotate(f);
		g.drawImage(image,(int)-radius,(int)-radius,null);
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
			
			model.addTransObject(new Exhaust(x, y, dx, dy, t), false);
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
					
					BulletObject bullet = new BulletObject(gun.type, t, x, y, dx, dy);
					bullet.setFreq(freq);
					model.addTransObject(bullet, true);
				}
			}
		}
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
