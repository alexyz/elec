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
	
	static final Map<String,Type> types = new TreeMap<String,Type>();
	
	static {
		List<Type> l = Arrays.asList(Type.gun1, Type.gun2, Type.gun3, Type.gun4, Type.bomb1, Type.bomb2, Type.bomb3, Type.bomb4);
		for (Type t : l) {
			types.put(t.toString(), t);
		}
	}
	
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
	float period;
	float velocity;
	Type type;
	Gun(Type type, float period, float velocity, Mount... mounts) {
		this.period = period;
		this.velocity = velocity;
		this.type = type;
		this.mounts = mounts;
	}
}