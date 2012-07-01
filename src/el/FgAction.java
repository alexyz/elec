package el;

import el.fg.FgObject;

abstract class FgAction {
	/** perform action on fg object. return true if server update required */
	abstract boolean run(FgObject obj, float t, float dt);
}
