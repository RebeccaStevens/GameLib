package gamelib.game;

import gamelib.GameManager;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;


public abstract class Camera {
	
	protected PVector location;
	protected PVector rotation;
	protected PVector scale;
	
	private Level level;

	public Camera(Level level){
		location = new PVector(0, 0, 0);
		rotation = new PVector(0, 0, 0);
		scale = new PVector(1, 1, 1);
		setLevel(level);
	}
	
	public abstract void update(double delta);

	void apply(PGraphics g){
		if(getLevel().is3D()){
			apply3D(g);
		}
		else{
			apply2D(g);
		}
	}
	
	private void apply3D(PGraphics g) {
		g.translate(-location.x+g.width/2, -location.y+g.height/2, -location.z);
		g.rotateX(-rotation.x);
		g.rotateY(-rotation.y);
		g.rotateZ(-rotation.z);
		g.scale(scale.x, scale.y, scale.z);
	}

	private void apply2D(PGraphics g){
		// TODO change camera mode to use positive y as down
		PApplet sketch = GameManager.getMe().getSketch();
		g.translate(sketch.width/2, sketch.height/2);
		g.rotate(rotation.x);
		g.scale(1, -1);
		g.translate(-2*location.x, -2*location.y);
		g.scale(scale.x, scale.y);
	}
	
	final void setLevel(Level level){
		this.level = level;
	}
	
	public final Level getLevel(){
		return level;
	}
}
