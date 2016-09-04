package gamelib.game.entities;

import gamelib.game.Entity;
import gamelib.game.Level;

public abstract class Actor extends Entity {

	public Actor(Level level, float x, float y, float width, float height, float mass) {
		super(level, x, y, width, height);
		init(mass);
	}
	
	public Actor(Level level, float x, float y, float z, float width, float height, float depth, float mass) {
		super(level, x, y, z, width, height, depth);
		init(mass);
	}
	
	private void init(float mass){
		setGravityEffected(true);
		setMass(mass);
		setCollisionGroup(1);
	}

}
