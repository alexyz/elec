package el;

import java.util.TreeMap;

import el.fg.FgObject;

/**
 * actions that a user can perform on a foreground object (ship)
 */
public class ActionMap extends TreeMap<String,FgAction> {
	static final String FIRE3 = "fire3";
	static final String FIRE2 = "fire2";
	static final String FIRE1 = "fire1";
	static final String RIGHT = "right";
	static final String LEFT = "left";
	static final String DOWN = "down";
	static final String UP = "up";

	public ActionMap() {
		put(UP, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				obj.up(t, dt);
				return true;
			}
			@Override
			public String toString() {
				return UP;
			}
			
		});
		put(DOWN, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				obj.down(t, dt);
				return true;
			}
			@Override
			public String toString() {
				return DOWN;
			}
		});
		put(LEFT, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				obj.left(t, dt);
				return true;
			}
			@Override
			public String toString() {
				return LEFT;
			}
		});
		put(RIGHT, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				obj.right(t, dt);
				return true;
			}
			@Override
			public String toString() {
				return RIGHT;
			}
		});
		put(FIRE1, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				return obj.fire(0, t, dt);
			}
			@Override
			public String toString() {
				return FIRE1;
			}
		});
		put(FIRE2, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				return obj.fire(1, t, dt);
			}
			@Override
			public String toString() {
				return FIRE2;
			}
		});
		put(FIRE3, new FgAction() {
			@Override
			boolean run(FgObject obj, float t, float dt) {
				return obj.fire(2, t, dt);
			}
			@Override
			public String toString() {
				return FIRE3;
			}
		});
	}
}
