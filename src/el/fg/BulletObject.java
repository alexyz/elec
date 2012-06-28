package el.fg;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.StringTokenizer;

import el.phys.Circle;

/**
 * Bomb or bullet
 */
public class BulletObject extends TransObject {
	
	static final float lifet = 3f;
	
	private Gun.Type type;
	
	public BulletObject(StringTokenizer tokens) {
		super(new Circle(), 0, 0);
		read(tokens);
	}
	
	public BulletObject(Gun.Type type, float t, float x, float y, float dx, float dy) {
		super(new Circle(x, y, type.radius), t, t + lifet);
		this.type = type;
		this.vx = dx;
		this.vy = dy;
    }
	
	public int getProximity() {
		return type.prox;
	}
	
	@Override
	public void read(StringTokenizer tokens) {
		super.read(tokens);
		type = Gun.Type.valueOf(tokens.nextToken());
	}
	
	@Override
	public StringBuilder write(StringBuilder sb) {
		super.write(sb);
		sb.append(type).append(" ");
		return sb;
	}
	
	@Override
	protected void collision() {
		if (hit == 1) {
			reflect = false;
		} else if (hit > 1) {
			// TODO fire explosion transient?
			remove = true;
			ExplodeObject ex = new ExplodeObject(c.x, c.y, c.r * 3, model.getTime());
			// FIXME concurrent mod ex
			//model.addTransObject(ex, false);
		}
	}
	
	@Override
	public void paint(Graphics2D g) {
		//g.setColor(hit > 0 ? Color.red : Color.yellow);
		//g.drawOval(-4, -4, 4, 4);
		
		g.setColor(type.colour);
		int r = type.radius;
		g.fillOval(-r, -r, r * 2, r * 2);
		
		int p = type.prox;
		if (p > r) {
			g.setColor(Color.darkGray);
			g.drawOval(-p, -p, p*2, p*2);
		}
	}
	
	@Override
	public String toString() {
		return String.format("Bullet[%s]", type) + super.toString();
	}
	
	
}
	