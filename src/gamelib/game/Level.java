package gamelib.game;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import gamelib.GameManager;
import gamelib.game.cameras.CameraStatic;
import gamelib.scenes.GameScene;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Level {

	protected List<Entity> entities;
	private List<Entity> entitiesToRemove;
	protected List<DynamicLight> dLights;	// dynamic Lights
	protected List<Light> lights;			// all Lights
	
	private Camera camera;
	
	private boolean drawBoundingBoxes;
	private boolean drawGrid;
	
	private final int gridWidth;
	private int gridHeight;
	
	private PVector gravity;
	private float airFriction;
	
	private float zoom = 1;
	
	/**
	 * Create a level with a static camera
	 */
	public Level(){
		this(null);
	}
	
	/**
	 * Create a level with the given camera.
	 * 
	 * @param camera
	 */
	public Level(Camera camera){
		this(null, 16);
	}
	
	/**
	 * Create a level.
	 * 
	 * @param camera - The camera to use
	 * @param gridWidth - Set the scale of the grid
	 */
	public Level(Camera camera, int gridWidth){
		this.entities = new ArrayList<Entity>();
		this.entitiesToRemove = new ArrayList<Entity>();
		this.dLights = new ArrayList<DynamicLight>();
		this.lights = new ArrayList<Light>();
		
		if (camera == null) {
			this.camera = new CameraStatic(this);
		} else {
			this.camera = camera;
		}
		this.gravity = new PVector();
		this.airFriction = 1;
		this.gridWidth = 16;
		updateGrid();
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
	 * Draw the level.
	 * 
	 * @param g The graphics to draw to
	 */
	public final void draw(PGraphics g) {
		g.pushMatrix();
		this.camera.apply(g);
		for(Light l : this.lights){
			l.apply(g);
		}
		for(Entity e : this.entities){
			e._draw(g);
		}
		g.popMatrix();
		
		if (this.drawGrid) {
			drawGrid(g);
		}
	}
	
	/**
	 * Draw the grid.
	 * 
	 * @param g
	 */
	private void drawGrid(PGraphics g) {
		g.pushStyle();
		
		g.stroke(0x33000000); // black transparent lines	
		g.strokeWeight(1);
		
		float xOffset = (this.camera.getX() * this.gridWidth / g.width) % 1;
		float yOffset = (this.camera.getY() * this.gridHeight / g.height) % 1;
		
		// draw the vertical lines
		for (int i = 0; i < gridWidth / this.zoom + 1; i++) {
			g.line((i - xOffset) * this.zoom * g.width / this.gridWidth, 0, (i - xOffset) * this.zoom * g.width / this.gridWidth, g.height);
		}
		
		// draw the horizontal lines
		for (int i = 0; i < gridHeight / this.zoom + 1; i++) {
			g.line(0, g.height - ((i + yOffset) * this.zoom * g.height / this.gridHeight), g.width, g.height - ((i + yOffset) * this.zoom * g.height / this.gridHeight));
		}
		
		g.popStyle();
	}

	/**
	 * Draw the level's background.
	 * @param g The graphics to draw to
	 */
	public abstract void drawBackground(PGraphics g);

	/**
	 * Draw the level overlay.
	 * @param g The graphics to draw to
	 */
	public abstract void drawOverlay(PGraphics g);

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
	 * Detect if the given entity is on the ground. If so, the ground object is returned.
	 * Note: This method does collision detection.
	 * 
	 * @param entity - The entity to test
	 * @return the ground object or null if not on the ground
	 * TODO increase efficiency
	 */
	Entity getGround(Entity entity) {
		if (entity.getCollisionGroup() == 0) {
			return null;
		}
		for (Entity ent : entities) {
			if(ent == entity) continue;
			if(!needToCheckCollision(entity, ent)) continue;
			BoundingBox thisbb = entity.getBoundingBox();
			BoundingBox otherbb = ent.getBoundingBox();
			float groundDist = 1;//TODO extract
			if (is3D()) {
				if (otherbb.contains(new PVector(thisbb.getCenterX(), thisbb.getMaxY() + groundDist, thisbb.getCenterZ()))
				||  otherbb.contains(new PVector(thisbb.getMinX(),    thisbb.getMaxY() + groundDist, thisbb.getMinZ()))
				||  otherbb.contains(new PVector(thisbb.getMinX(),    thisbb.getMaxY() + groundDist, thisbb.getMaxZ()))
				||  otherbb.contains(new PVector(thisbb.getMaxX(),    thisbb.getMaxY() + groundDist, thisbb.getMinZ()))
				||  otherbb.contains(new PVector(thisbb.getMaxX(),    thisbb.getMaxY() + groundDist, thisbb.getMaxZ()))) {
					return ent;
				}
			} else {
				if (otherbb.contains(new PVector(thisbb.getCenterX(), thisbb.getMaxY() + groundDist))
				||  otherbb.contains(new PVector(thisbb.getMinX(),    thisbb.getMaxY() + groundDist))
				||  otherbb.contains(new PVector(thisbb.getMaxX(),    thisbb.getMaxY() + groundDist))) {
					return ent;
				}				
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
	 * Get whether or not the grid should be drawn.
	 * 
	 * @return
	 */
	public final boolean isDrawGrid(){
		return this.drawGrid;
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
	 * Set whether or not to draw the level grid.
	 * 
	 * @param b
	 */
	public final void setDrawGrid(boolean b){
		this.drawGrid = b;
	}

	/**
	 * Get the zoom level.
	 * 
	 * @return
	 */
	public float getZoom() {
		return zoom;
	}

	/**
	 * Set the zoom level (must be greater than zero).
	 * >1 => Zoom In
	 * <1 => Zoom Out
	 * 
	 * @param zoom - The zoom level
	 */
	public void setZoom(float zoom) {
		if (zoom <= 0) {
			throw new InvalidParameterException("The Zoom Level must be greater than zero.");
		}
		this.zoom = zoom;
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
		GameManager.getMe().getGameScene().setActiveLevel(this);
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

	/**
	 * Update the size of the grid.
	 */
	private void updateGrid() {
		GameScene gs = GameManager.getMe().getGameScene();
		
		this.gridHeight = (int) (this.gridWidth * gs.getGameHeight() / (double) gs.getGameWidth());
	}
	
	/**
	 * Convert game units (for x position) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsXToPixels(float gameUnits) {
		return this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameWidth() / this.gridWidth;
	}
	
	/**
	 * Convert game units (for y position) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsYToPixels(float gameUnits) {
		return GameManager.getMe().getGameScene().getGameHeight() - (this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameHeight() / this.gridHeight);
	}
	
	/**
	 * Convert game units (for z position) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsZToPixels(float gameUnits) {
		return convertGridUnitsXToPixels(gameUnits);
	}
	
	/**
	 * Convert game units (for x movement) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsVelocityXToPixels(float gameUnits) {
		return this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameWidth() / this.gridWidth;
	}
	
	/**
	 * Convert game units (for y movement) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsVelocityYToPixels(float gameUnits) {
		return -(this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameHeight() / this.gridHeight);
	}
	
	/**
	 * Convert game units (for z movement) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsVelocityZToPixels(float gameUnits) {
		return convertGridUnitsVelocityXToPixels(gameUnits);
	}

	/**
	 * Convert game units (for width) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsWidthToPixels(float gameUnits) {
		return this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameWidth() / this.gridWidth;
	}

	/**
	 * Convert game units (for height) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsHeightToPixels(float gameUnits) {
		return this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameHeight() / this.gridHeight;
	}

	/**
	 * Convert game units (for depth) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsDepthToPixels(float gameUnits) {
		return convertGridUnitsWidthToPixels(gameUnits);
	}
}
