package gamelib.game;

import gamelib.GameManager;
import gamelib.scenes.GameScene;
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
		GameScene gs = GameManager.getMe().getGameScene();
		g.translate(gs.getGameWidth() / 2, gs.getGameHeight() / 2);
		g.rotate(rotation.x);
		g.translate(-location.x, -location.y);
		g.scale(scale.x, scale.y);
	}
	
	final void setLevel(Level level){
		this.level = level;
	}
	
	public final Level getLevel(){
		return level;
	}

	/**
	 * Get the x location of this camera.
	 * 
	 * @return the x location
	 */
	public float getX(){
		return location.x;
	}

	/**
	 * Get the y location of this camera.
	 * 
	 * @return the y location
	 */
	public float getY(){
		return location.y;
	}

	/**
	 * Get the z location of this camera.
	 * 
	 * @return the z location
	 */
	public float getZ(){
		return location.z;
	}

	/**
	 * Get the location of this entity.
	 * @return the location
	 */
	public PVector getLocation(){
		return location.copy();
	}
}
