package el.fg;

import el.Model;
import el.phys.Circle;


/**
 * Meta-object that represents the current viewport into the model
 */
public class ModelObject extends FgObject {
	public ModelObject() {
		super(new Circle(Model.centrex, Model.centrey, 0));
	}
	@Override
	public void down() {
		c.y += 5;
	}
	@Override
	public void left() {
		c.x -= 5;
	}
	@Override
	public void right() {
		c.x += 5;
	}
	@Override
	public void up() {
		c.y -= 5;
	}
}
