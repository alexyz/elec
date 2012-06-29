package el.fg;

import java.awt.Color;

@Deprecated
public class Gun {
	static final Color[] colours = {
		new Color(1f, 0f, 0f),
		new Color(1f, 1f, 0f),
		new Color(0f, 0f, 1f),
		new Color(1f, 0f, 1f)
	};
	
	@Deprecated
	public enum Type {
		gun1(0, 2, 2),
		gun2(1, 2, 2),
		gun3(2, 2, 2),
		gun4(3, 2, 2),
		bomb1(0, 5, 20), 
		bomb2(1, 5, 20),
		bomb3(2, 5, 20),
		bomb4(3, 5, 20);
		
		public final Color colour;
		public final int radius;
		public final int prox;
		private Type(int colour, int radius, int prox) {
			this.radius = radius;
			this.prox = prox;
			this.colour = colours[colour];
		}
	}
	
	final Type type;
	final Mount[] mounts;
	final float period;
	final float velocity;
	final float damage;
	
	Gun(Type type, float damage, float period, float velocity, Mount... mounts) {
		this.damage = damage;
		this.period = period;
		this.velocity = velocity;
		this.type = type;
		this.mounts = mounts;
	}
}
