package gamelib.game.entities;

import gamelib.game.Entity;
import gamelib.game.Level;

public abstract class Platform extends Entity {
	
	private float groundFriction;

	public Platform(Level level, float x, float y, float width, float height) {
		super(level, x, y, width, height);
		init();
	}
	
	public Platform(Level level, float x, float y, float z, float width, float height, float depth) {
		super(level, x, y, z, width, height, depth);
		init();
	}
	
	private void init(){
		setCollisionGroup(2);
		setGroundFriction(10);
	}

	@Override
	public float getGroundFriction() {
		return groundFriction;
	}

	public void setGroundFriction(float groundFriction) {
		this.groundFriction = groundFriction;
	}
}