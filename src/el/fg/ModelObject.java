package el.fg;

import java.awt.Graphics2D;

import el.Model;

/**
 * Meta-object that represents the current view port into the model
 */
public class ModelObject extends FgObject {
	private static final int v = 320;
	public ModelObject() {
		this.x = Model.centrex;
		this.y = Model.centrey;
		setId(-1);
	}
	
	@Override
	public void down(float t, float dt) {
		y += v * dt;
	}
	
	@Override
	public void left(float t, float dt) {
		x -= v * dt;
	}
	
	@Override
	public void right(float t, float dt) {
		x += v * dt;
	}
	
	@Override
	public void up(float t, float dt) {
		y -= v * dt;
	}
	
	@Override
	public void paint(Graphics2D g) {
		//
	}
	
	@Override
	public String toString() {
		return "ModelObject[]" + super.toString();
	}
}
