package gamelib.game.entities;

import gamelib.game.Entity;
import gamelib.game.Level;
import processing.core.PConstants;
import processing.core.PVector;

public abstract class PushableEntity extends Entity {
	
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
		setCollisionGroup(1);
	}
	
	public float getResistance(){
		return this.resistance;
	}
}
