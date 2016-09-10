package gamelib.game.entities;

import gamelib.game.Entity;
import gamelib.game.Level;
import processing.core.PConstants;

public abstract class Actor extends Entity {

	public Actor(Level level, float x, float y, float width, float height) {
		this(level, x, y, width, height, 1);
	}

	public Actor(Level level, float x, float y, float width, float height, float mass) {
		super(level, x, y, width, height, PConstants.CENTER);
		init(mass);
	}
	
	public Actor(Level level, float x, float y, float z, float width, float height, float depth) {
		this(level, x, y, z, width, height, depth, 1);
	}
	
	public Actor(Level level, float x, float y, float z, float width, float height, float depth, float mass) {
		super(level, x, y, z, width, height, depth, PConstants.CENTER);
		init(mass);
	}
	
	private void init(float mass){
		setGravityEffected(true);
		setMass(mass);
		setCollisionGroup(1);
	}

}
