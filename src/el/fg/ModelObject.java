package el.fg;

import el.Model;

/**
 * Meta-object that represents the current view port into the model
 */
public class ModelObject extends FgObject {
	public ModelObject() {
		this.x = Model.centrex;
		this.y = Model.centrey;
		setId(-1);
	}
	
	@Override
	public void down() {
		y += 5;
	}
	
	@Override
	public void left() {
		x -= 5;
	}
	
	@Override
	public void right() {
		x += 5;
	}
	
	@Override
	public void up() {
		y -= 5;
	}
	
	@Override
	public String toString() {
		return "ModelObject[]" + super.toString();
	}
}
