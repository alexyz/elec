package el;

import java.util.TreeMap;

import el.fg.FgObject;

/**
 * actions that a user can perform on a foreground object (ship)
 */
public class ActionMap extends TreeMap<String,FgRunnable> {
	static final String FIRE3 = "fire3";
	static final String FIRE2 = "fire2";
	static final String FIRE1 = "fire1";
	static final String RIGHT = "right";
	static final String LEFT = "left";
	static final String DOWN = "down";
	static final String UP = "up";

	public ActionMap() {
		put(UP, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.up();
			}
			@Override
			public String toString() {
				return UP;
			}
		});
		put(DOWN, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.down();
			}
			@Override
			public String toString() {
				return DOWN;
			}
		});
		put(LEFT, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.left();
			}
			@Override
			public String toString() {
				return LEFT;
			}
		});
		put(RIGHT, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.right();
			}
			@Override
			public String toString() {
				return RIGHT;
			}
		});
		put(FIRE1, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.fire(0);
			}
			@Override
			public String toString() {
				return FIRE1;
			}
		});
		put(FIRE2, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.fire(1);
			}
			@Override
			public String toString() {
				return FIRE2;
			}
		});
		put(FIRE3, new FgRunnable() {
			@Override
			public void run(FgObject obj) {
				obj.fire(2);
			}
			@Override
			public String toString() {
				return FIRE3;
			}
		});
	}
}
