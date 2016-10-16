package gamelib.game.entities;

import gamelib.game.Entity;
import gamelib.game.Level;
import processing.core.PConstants;

public abstract class Platform extends Entity {
	
	private static int defaultCollisionGroup = 1;
	private float groundFriction;

	public Platform(Level level, float x, float y, float width, float height) {
		super(level, x, y, width, height, PConstants.CORNER);
		init();
	}
	
	public Platform(Level level, float x, float y, float z, float width, float height, float depth) {
		super(level, x, y, z, width, height, depth, PConstants.CORNER);
		init();
	}
	
	private void init(){
		setCollisionGroup(defaultCollisionGroup);
		setGroundFriction(10);
	}

	@Override
	public float getGroundFriction() {
		return groundFriction;
	}

	public void setGroundFriction(float groundFriction) {
		this.groundFriction = groundFriction;
	}

	/**
	 * Get the collision group platforms will be put into by default.
	 * 
	 * @return
	 */
	public static final int getDefaultCollisionGroup() {
		return defaultCollisionGroup;
	}

	/**
	 * Set the collision group platforms will be put into by default.
	 * 
	 * @param group
	 */
	public static final void setDefaultCollisionGroup(int group) {
		defaultCollisionGroup = group;
	}
}