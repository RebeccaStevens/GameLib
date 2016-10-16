package gamelib.game.entities;

import gamelib.game.Entity;
import gamelib.game.Level;
import processing.core.PConstants;
import processing.core.PVector;

public abstract class PushableEntity extends Entity {
	
	private static int defaultCollisionGroup = 2;
	private float resistance;

	/**
	 * Create a 2D Entity
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 * @param width The width
	 * @param height The height
	 */
	public PushableEntity(Level level, float x, float y, float width, float height, float resistance){
		super(level, x, y, width, height, PConstants.CENTER);
		init(resistance);
	}
	
	/**
	 * Create a 2D Entity
	 * @param level The level the entity will exist in
	 * @param location The location of this entity
	 * @param width The width
	 * @param height The height
	 */
	public PushableEntity(Level level, PVector location, float width, float height, float resistance){
		super(level, location, width, height, PConstants.CENTER);
		init(resistance);
	}
	
	/**
	 * Create a 3D Entity
	 * @param level The level the entity will exist in
	 * @param location The location of this entity
	 * @param width The width
	 * @param height The height
	 * @param depth The depth
	 */
	public PushableEntity(Level level, PVector location, float width, float height, float depth, float resistance){
		super(level, location, width, height, depth, PConstants.CENTER);
		init(resistance);
	}
	
	/**
	 * Create a 3D Entity
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 * @param z The z location
	 * @param width The width
	 * @param height The height
	 * @param depth The depth
	 */
	public PushableEntity(Level level, float x, float y, float z, float width, float height, float depth, float resistance){
		super(level, x, y, z, width, height, depth, PConstants.CENTER);
		init(resistance);
	}
	
	private final void init(float resistance){
		this.resistance = resistance;
		setCollisionGroup(defaultCollisionGroup);
	}
	
	public float getResistance(){
		return this.resistance;
	}

	/**
	 * Get the collision group pushable entities will be put into by default.
	 * 
	 * @return
	 */
	public static final int getDefaultCollisionGroup() {
		return defaultCollisionGroup;
	}

	/**
	 * Set the collision group pushable entities will be put into by default.
	 * 
	 * @param group
	 */
	public static final void setDefaultCollisionGroup(int group) {
		defaultCollisionGroup = group;
	}
}
