package el.fg;
import java.awt.Graphics2D;
import java.util.StringTokenizer;

import el.phys.Circle;

/**
 * Bomb or bullet
 */
public class Bullet extends TransMovingFgObject {
	
	static final float lifet = 3f;
	
	private Gun.Type type;
	
	public Bullet(StringTokenizer tokens) {
		super(new Circle(), 0, 0);
		read(tokens);
	}
	
	public Bullet(Gun.Type type, float t, float x, float y, float dx, float dy) {
		super(new Circle(x, y, type.radius), t, t + lifet);
		this.type = type;
		this.vx = dx;
		this.vy = dy;
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
	