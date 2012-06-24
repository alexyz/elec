package el.fg;
import java.awt.Graphics2D;

import el.phys.Circle;

/**
 * Bomb or bullet
 */
public class Bullet extends TransMovingFgObject {
	
	static final float lifet = 3f;
	
	/*
    public String write() {
    	// Client2 [servertime] [localtime] addTrans Ordnance type t dx dy x y
    	return String.format("%s %s %f %f %f %f %f",
    			getClass().getSimpleName(),
    			type,
    			endt - lifet,
    			c.x, c.y, vx, vy);
    }
    
    public static Bullet read(float toff, String[] s, int p) {
    	Gun.Type type = Gun.types.get(s[p++]);
    	float t = Float.parseFloat(s[p++]);
    	float x = Float.parseFloat(s[p++]);
    	float y = Float.parseFloat(s[p++]);
    	float dx = Float.parseFloat(s[p++]);
    	float dy = Float.parseFloat(s[p++]);
    	return new Bullet(type, t + toff, x, y, dx, dy);
    }
	*/
	
	private final Gun.Type type;
	
	public Bullet(Gun.Type type, float t, float x, float y, float dx, float dy) {
		super(new Circle(x, y, type.radius), t, t + lifet);
		this.type = type;
		this.vx = dx;
		this.vy = dy;
    }
	
	@Override
	public void paint(Graphics2D g) {
		//g.setColor(hit > 0 ? Color.red : Color.yellow);
		//g.drawOval(-4, -4, 4, 4);
		g.setColor(type.colour);
		int r = type.radius;
		g.drawOval(-r, -r, r * 2, r * 2);
	}
	
	@Override
	public String toString() {
		return String.format("Bullet[%s]", type) + super.toString();
	}
	
	
}
	