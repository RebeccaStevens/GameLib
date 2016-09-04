package gamelib.game;

import gamelib.GameManager;
import gamelib.game.cameras.CameraStatic;

import java.util.ArrayList;
import java.util.List;

import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Level {

	protected List<Entity> entities;
	private List<Entity> entitiesToRemove;
	protected List<DynamicLight> dLights;	// dynamic Lights
	protected List<Light> lights;			// all Lights
	
	private Camera camera;
	
	private boolean drawBoundingBoxes;
	
	private PVector gravity;
	private float airFriction;
	
	/**
	 * Create a level with a static camera
	 */
	public Level(){
		this(null);
	}
	
	/**
	 * Create a level with the given camera
	 * @param camera
	 */
	public Level(Camera camera){
		entities = new ArrayList<Entity>();
		entitiesToRemove = new ArrayList<Entity>();
		dLights = new ArrayList<DynamicLight>();
		lights = new ArrayList<Light>();
		if(camera == null){
			this.camera = new CameraStatic(this);
		}
		else{
			this.camera = camera;
		}
		gravity = new PVector();
		airFriction = 0;
	}
	
	/**
	 * Update the level
	 */
	public void update(float delta){
		camera.update(delta);
		for(Entity e : entities){
			e._update(delta);
		}
		for(DynamicLight l : dLights){
			l.update(delta);
		}
		entities.removeAll(entitiesToRemove);
		entitiesToRemove.clear();
	}

	/**
	 * Display the level.
	 * @param g The graphics to draw to
	 */
	public void display(PGraphics g) {
		g.pushMatrix();
		camera.apply(g);
		for(Light l : lights){
			l.apply(g);
		}
		for(Entity e : entities){
			e.draw(g);
		}
		g.popMatrix();
	}

	/**
	 * Draw the level.
	 * @param g The graphics to draw to
	 */
	public abstract void draw(PGraphics g);

	/**
	 * Add an entity to the level.
	 * (To be called from the Entity class)
	 * @param entity
	 */
	void addEntity(Entity entity){
		entities.add(entity);
	}
	
	void addLight(Light light){
		lights.add(light);
		if(light instanceof DynamicLight) dLights.add((DynamicLight) light);
	}
	
	/**
	 * Get the amount of air friction in the level.
	 * @return
	 */
	public float getAirFriction() {
		return airFriction;
	}

	/**
	 * Get the gravity on the level.
	 * @return
	 */
	PVector getGravity() {
		return gravity;
	}

	/**
	 * Detect if this entity is on the ground. If so, the ground object is returned.
	 * Note: This method does collision detection.
	 * @param entity The entity to test
	 * @return the ground object or null if not on the ground
	 * TODO increase efficiency
	 */
	Entity getGround(Entity entity) {
		if(entity.getCollisionGroup() == 0) return null;
		for(Entity ent : entities){
			if(ent == entity) continue;
			if(!needToCheckCollision(entity, ent)) continue;
			BoundingBox thisbb = entity.getBoundingBox();
			BoundingBox otherbb = ent.getBoundingBox();
			float groundDist = 1;//TODO extract
			if(otherbb.contains(new PVector(entity.getX() + thisbb.getCenterX(), entity.getY() + thisbb.getMaxY() + groundDist, entity.getZ() + thisbb.getCenterZ()))
			|| otherbb.contains(new PVector(entity.getX() + thisbb.getMinX(),    entity.getY() + thisbb.getMaxY() + groundDist, entity.getZ() + thisbb.getMinZ()))
			|| otherbb.contains(new PVector(entity.getX() + thisbb.getMinX(),    entity.getY() + thisbb.getMaxY() + groundDist, entity.getZ() + thisbb.getMaxZ()))
			|| otherbb.contains(new PVector(entity.getX() + thisbb.getMaxX(),    entity.getY() + thisbb.getMaxY() + groundDist, entity.getZ() + thisbb.getMinZ()))
			|| otherbb.contains(new PVector(entity.getX() + thisbb.getMaxX(),    entity.getY() + thisbb.getMaxY() + groundDist, entity.getZ() + thisbb.getMaxZ()))){
				return ent;
			}
		}
		return null;
	}

	/**
	 * Note: This method does collision detection.
	 * @param entity The entity to move
	 * @param newLocation The new location that this entity wants to move to
	 * @return the entity that this entity will collide with if it move to the new location
	 * TODO increase efficiency
	 */
	Entity canMove(Entity entity, PVector newLocation) {
		if(entity.getCollisionGroup() == 0) return null;
		for(Entity ent : entities){
			if(ent == entity) continue;
			if(!needToCheckCollision(entity, ent)) continue;
			if(entity.getBoundingBox().intersects(ent.getBoundingBox(), newLocation)){
				return ent;
			}
		}
		return null;
	}
	
	final boolean collidesWithSomething(BoundingBox boundingBox) {
		Entity entity = boundingBox.getEntity();
		
		if(entity.getCollisionGroup() == 0) return true;
		for(Entity ent : entities){
			if(ent == entity) continue;
			if(!needToCheckCollision(entity, ent)) continue;
			if(boundingBox.intersects(ent.getBoundingBox())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether or not the level is 3D
	 * @return
	 */
	public abstract boolean is3D();

	/**
	 * Get whether or not the bounding boxes of the entities are being drawn.
	 * @return
	 */
	public final boolean isDrawBoundingBoxes(){
		return drawBoundingBoxes;
	}

	/**
	 * Set the amount of air friction in the level.
	 * @param airFriction
	 */
	public void setAirFriction(float airFriction) {
		this.airFriction = airFriction;
	}

	/**
	 * Set the active camera used in the level.
	 * @param camera
	 */
	public void setCamera(Camera camera){
		this.camera.setLevel(null);
		this.camera = camera;
		this.camera.setLevel(this);
	}

	/**
	 * Set the downward acceleration of gravity.
	 * Horizontal acceleration will be set to 0.
	 * @param gravityDown
	 */
	public void setGravity(float gravityDown){
		setGravity(new PVector(0, gravityDown, 0));
	}
	
	/**
	 * Set the acceleration of gravity.
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setGravity(float x, float y, float z){
		setGravity(new PVector(x, y, z));
	}
	
	/**
	 * Set the acceleration of gravity.
	 * @param gravity
	 */
	public void setGravity(PVector gravity){
		this.gravity.set(gravity);
	}
	
	/**
	 * Set whether or not to draw the bounding boxes of the entities in the level.
	 * @param b
	 */
	public final void setDrawBoundingBoxes(boolean b){
		drawBoundingBoxes = b;
	}

	/**
	 * Remove an entity from the level.
	 * (To be called from the Entity class)
	 * @param entity
	 */
	public void removeEntity(Entity entity){
		entitiesToRemove.add(entity);
		entity.removeLevel();
	}
	
	public void removeLight(Light light){
		light.removeLevel();
		lights.remove(light);
		dLights.remove(light);
	}

	/**
	 * Make this the active level
	 */
	public final void makeActive(){
		GameManager.getMe().setActiveLevel(this);
	}
	
	/**
	 * Returns whether or not collision detection needs to be done between the two entities
	 * @param entity1 The entity to check if it can collide with the other one
	 * @param entity2 The other entity to check against
	 * @return
	 */
	private boolean needToCheckCollision(Entity entity1, Entity entity2) {
		if(entity2.getCollisionGroup() == 0) return false;
		
		if(entity1.getCollisionIgnoreEntities().contains(entity2)) return false;
		
		switch(entity1.getCollisionMode()){
		case EQUAL_TO:
			return entity2.getCollisionGroup() == entity1.getCollisionGroup();
		case GREATER_THAN_OR_EQUAL_TO:
			return entity2.getCollisionGroup() >= entity1.getCollisionGroup();
		case LESS_THAN:
			return entity1.getCollisionGroup() <  entity1.getCollisionGroup();
		default:
			return false;
		}
	}
}
