package el.fg;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class Gun {
	static final Color[] colours = {
		new Color(1f, 0f, 0f),
		new Color(1f, 1f, 0f),
		new Color(0f, 0f, 1f),
		new Color(1f, 0f, 1f)
	};
	
	enum Type {
		gun1(0, 2),
		gun2(1, 2),
		gun3(2, 2),
		gun4(3, 2),
		bomb1(0, 5), 
		bomb2(1, 5),
		bomb3(2, 5),
		bomb4(3, 5);
		
		final Color colour;
		final int radius;
		private Type(int colour, int radius) {
			this.radius = radius;
			this.colour = colours[colour];
		}
	}
	
	final Mount[] mounts;
	final float period;
	final float velocity;
	final Type type;
	Gun(Type type, float period, float velocity, Mount... mounts) {
		this.period = period;
		this.velocity = velocity;
		this.type = type;
		this.mounts = mounts;
	}
}