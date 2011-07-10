package el.fg;
import java.awt.Graphics2D;

import el.phys.Circle;

/**
 * Bomb or bullet
 * TODO tracers, lasers, thors
 * TODO burst
 * TODO shrap
 */
public class Ordnance extends MovingFgObject {
	
	static final float lifet = 3f;
	
    public String write() {
    	// Client2 [servertime] [localtime] addTrans Ordnance type t dx dy x y
    	return String.format("%s %s %f %f %f %f %f",
    			getClass().getSimpleName(),
    			type,
    			endt - lifet,
    			c.x, c.y, vx, vy);
    }
    
    public static Ordnance read(float toff, String[] s, int p) {
    	Gun.Type type = Gun.types.get(s[p++]);
    	float t = Float.parseFloat(s[p++]);
    	float x = Float.parseFloat(s[p++]);
    	float y = Float.parseFloat(s[p++]);
    	float dx = Float.parseFloat(s[p++]);
    	float dy = Float.parseFloat(s[p++]);
    	return new Ordnance(type, t + toff, x, y, dx, dy);
    }
	
	/**
	 * Start and end time
	 */
	private final float endt;
	private final Gun.Type type;
	
	public Ordnance(Gun.Type type, float t, float x, float y, float dx, float dy) {
		super(new Circle(x, y, type.radius));
		this.type = type;
		this.vx = dx;
		this.vy = dy;
		this.endt = t + lifet;
    }
	
    @Override
	public void update(float t, float td) {
		if (t > endt) {
			remove = true;
			
		} else {
			super.update(t, td);
		}
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
		return String.format("Ord[%s]", type) + super.toString();
	}
	
	
}
	