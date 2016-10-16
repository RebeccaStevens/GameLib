package gamelib.game;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gamelib.Drawable;
import gamelib.GameManager;
import gamelib.Updatable;
import gamelib.game.cameras.CameraStatic;
import gamelib.scenes.GameScene;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * An abstract level for either 2D or 3D games.
 *
 * @author Rebecca Stevens
 */
public abstract class Level implements Updatable, Drawable {

	private final List<GameObject> gameObjects;
	private final List<Entity> entities;
	private final Collection<Entity> unmodifiableEntities;
	private final List<GameObject> gameObjectsToRemove;
	private final List<DynamicLight> dLights;	// dynamic Lights
	private final List<Light> lights;			// all Lights
	
	private final Map<Integer, List<Entity>> collisionGroups;
	
	private Camera camera;
	
	private boolean drawBoundingBoxes;
	private boolean drawGrid;
	
	private final int gridWidth;
	private int gridHeight;
	
	private PVector gravity;
	private float airFriction;
	
	private float zoom = 1;
	
	/**
	 * Create a level with a static camera and a grid with of 16.
	 */
	public Level(){
		this(null);
	}
	
	/**
	 * Create a level with the given camera and a grid with of 16.
	 * 
	 * @param camera
	 */
	public Level(Camera camera){
		this(camera, 16);
	}
	
	/**
	 * Create a level.
	 * 
	 * @param camera - The camera to use
	 * @param gridWidth - Set the scale of the grid
	 */
	public Level(Camera camera, int gridWidth){
		this.gameObjects = new ArrayList<GameObject>();
		this.entities = new ArrayList<Entity>();
		this.unmodifiableEntities = Collections.unmodifiableCollection(entities);
		this.gameObjectsToRemove = new ArrayList<GameObject>();
		this.dLights = new ArrayList<DynamicLight>();
		this.lights = new ArrayList<Light>();
		
		this.collisionGroups = new HashMap<Integer, List<Entity>>();
		
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
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 */
	public void update(float delta) {
		preUpdate(delta);
		camera._update(delta);
		for(GameObject e : gameObjects){
			e._update(delta);
		}
		for(DynamicLight l : dLights){
			l.update(delta);
		}
		if (gameObjectsToRemove.size() > 0) {
			removeGameObjects(gameObjectsToRemove);
			gameObjectsToRemove.clear();
		}
		postUpdate(delta);
	}

	/**
	 * Called each frame before the level updates.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 */
	public abstract void preUpdate(float delta);
	
	/**
	 * Called each frame after the level updates.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 */
	public abstract void postUpdate(float delta);

	/**
	 * Draw the level.
	 * 
	 * @param g The graphics to draw to
	 */
	public final void draw(PGraphics g) {
		g.pushMatrix();
		this.camera.apply(g);
		for (Light l : this.lights) {
			l.apply(g);
		}
		for (Entity e : getEntitiesToDraw()) {
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
		
		float xOffset = (this.camera.getXInPixels() * this.gridWidth / this.zoom / g.width) % 1;
		float yOffset = (this.camera.getYInPixels() * this.gridHeight / this.zoom / g.height) % 1;
		
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
	 * (To be called from the {@link Entity} class)
	 * 
	 * @param entity
	 */
	void addEntity(Entity entity) {
		entities.add(entity);
		addEntityToCollisionGroup(entity);
		addGameObject(entity);
	}
	
	/**
	 * Add a game object to the level.
	 * 
	 * @param object
	 */
	void addGameObject(GameObject object) {
		gameObjects.add(object);
	}
	
	void addLight(Light light){
		lights.add(light);
		if(light instanceof DynamicLight) dLights.add((DynamicLight) light);
	}
	
	/**
	 * Add the given entity to the collision group map.
	 * 
	 * @param entity
	 */
	private void addEntityToCollisionGroup(Entity entity) {
		int group = entity.getCollisionGroup();
		List<Entity> list = collisionGroups.get(group);
		
		if (list == null) {
			list = new ArrayList<Entity>();
			collisionGroups.put(group, list);
		}
		list.add(entity);
	}
	
	/**
	 * Update the given entity's location in the collision group map.
	 * 
	 * @param entity The entity to update
	 * @param oldGroup The entity's previous collision group
	 */
	void updateEntityCollisionGroup(Entity entity, int oldGroup) {
		collisionGroups.get(oldGroup).remove(entity);
		addEntityToCollisionGroup(entity);
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
		int group = entity.getCollisionGroup();
		if (group == 0) {
			return null;
		}
		
		switch (entity.getCollisionMode()) {
		case LESS_THAN_OR_EQUAL_TO:
			for (Integer key : collisionGroups.keySet()) {
				if (key != 0 && key <= group) {
					for (Entity ent : collisionGroups.get(key)) {
						if (isGroundEntity(entity, ent)) {
							return ent;
						}
					}
				}
			}
			return null;
			
		case GREATER_THAN:
			for (Integer key : collisionGroups.keySet()) {
				if (key != 0 && key > group) {
					for (Entity ent : collisionGroups.get(key)) {
						if (isGroundEntity(entity, ent)) {
							return ent;
						}
					}
				}
			}
			return null;
			
		case EQUAL_TO:
			for (Entity ent : collisionGroups.get(group)) {
				if (isGroundEntity(entity, ent)) {
					return ent;
				}
			}
			return null;
			
		default:
			return null;
		}
	}
	
	/**
	 * Test if the posibleGround is the ground entity for entityLookingForGround;
	 * 
	 * @param entityLookingForGround
	 * @param posibleGround
	 * @return
	 */
	private boolean isGroundEntity(Entity entityLookingForGround, Entity posibleGround) {
		if (posibleGround == entityLookingForGround) {
			return false;
		}
		if (!needToCheckForCollision(entityLookingForGround, posibleGround)) {
			return false;
		}
		
		BoundingBox thisbb = entityLookingForGround.getBoundingBox();
		BoundingBox otherbb = posibleGround.getBoundingBox();
		
		float groundDist = 1F / 12F;	//TODO extract
		
		if (is3D()) {
			if (otherbb.contains(new PVector(thisbb.getCenterX(), thisbb.getMaxY() + groundDist, thisbb.getCenterZ()))
			||  otherbb.contains(new PVector(thisbb.getMinX(),    thisbb.getMaxY() + groundDist, thisbb.getMinZ()))
			||  otherbb.contains(new PVector(thisbb.getMinX(),    thisbb.getMaxY() + groundDist, thisbb.getMaxZ()))
			||  otherbb.contains(new PVector(thisbb.getMaxX(),    thisbb.getMaxY() + groundDist, thisbb.getMinZ()))
			||  otherbb.contains(new PVector(thisbb.getMaxX(),    thisbb.getMaxY() + groundDist, thisbb.getMaxZ()))) {
				return true;
			}
		} else {
			if (otherbb.contains(new PVector(thisbb.getCenterX(), thisbb.getMinY() - groundDist))
			||  otherbb.contains(new PVector(thisbb.getMinX(),    thisbb.getMinY() - groundDist))
			||  otherbb.contains(new PVector(thisbb.getMaxX(),    thisbb.getMinY() - groundDist))) {
				return true;
			}				
		}
		return false;
	}

	/**
	 * Test if the given entity will collide with something if moved to the new location.
	 * Note: This method does collision detection.
	 * 
	 * @param entity The entity to move
	 * @param newLocation The new location that this entity wants to move to
	 * @return the entity that this entity will collide with if it move to the new location
	 * TODO increase efficiency
	 */
	Entity willCollideWithWhenMoved(Entity entity, PVector newLocation) {
		int group = entity.getCollisionGroup();
		if (group == 0) {
			return null;
		}
		
		switch (entity.getCollisionMode()) {
		case LESS_THAN_OR_EQUAL_TO:
			for (Integer key : collisionGroups.keySet()) {
				if (key != 0 && key <= group) {
					for (Entity ent : collisionGroups.get(key)) {
						if (willCollide(entity, ent, newLocation)) {
							return ent;
						}
					}
				}
			}
			return null;
			
		case GREATER_THAN:
			for (Integer key : collisionGroups.keySet()) {
				if (key != 0 && key > group) {
					for (Entity ent : collisionGroups.get(key)) {
						if (willCollide(entity, ent, newLocation)) {
							return ent;
						}
					}
				}
			}
			return null;
			
		case EQUAL_TO:
			for (Entity ent : collisionGroups.get(group)) {
				if (willCollide(entity, ent, newLocation)) {
					return ent;
				}
			}
			return null;
			
		default:
			return null;
		}
	}

	/**
	 * Test if the movingEntity will collide with the stationaryEntity if it moves to desiredLocationOfMovingEntity.
	 * 
	 * @param movingEntity
	 * @param stationaryEntity
	 * @param desiredLocationOfMovingEntity
	 * @return
	 */
	private boolean willCollide(Entity movingEntity, Entity stationaryEntity, PVector desiredLocationOfMovingEntity) {
		if (stationaryEntity == movingEntity) {
			return false;
		}
		if (!needToCheckForCollision(movingEntity, stationaryEntity)) {
			return false;
		}
		if (movingEntity.getBoundingBox().intersects(stationaryEntity.getBoundingBox(), desiredLocationOfMovingEntity)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Test if the given bounding box collides with something.
	 * 
	 * @param boundingBox
	 * @return
	 */
	final boolean collidesWithSomething(BoundingBox boundingBox) {
		Entity entity = boundingBox.getEntity();
		int group = entity.getCollisionGroup();
		
		if (group == 0) {
			return false;
		}
		
		switch (entity.getCollisionMode()) {
		case LESS_THAN_OR_EQUAL_TO:
			for (Integer key : collisionGroups.keySet()) {
				if (key != 0 && key <= group) {
					for (Entity ent : collisionGroups.get(key)) {
						return isColliding(entity, ent);
					}
				}
			}
			return false;
			
		case GREATER_THAN:
			for (Integer key : collisionGroups.keySet()) {
				if (key != 0 && key > group) {
					for (Entity ent : collisionGroups.get(key)) {
						return isColliding(entity, ent);
					}
				}
			}
			return false;
			
		case EQUAL_TO:
			for (Entity ent : collisionGroups.get(group)) {
				return isColliding(entity, ent);
			}
			return false;
			
		default:
			return false;
		}
	}

	/**
	 * Make this the active level
	 */
	public final void makeActive(){
		GameManager.getMe().getGameScene().setActiveLevel(this);
	}

	/**
	 * Test if entity1 is colliding with entity2.
	 * Note: this is not the same as test if entity2 is colliding with entity1.
	 * 
	 * @param entity1
	 * @param entity2
	 * @return
	 */
	public boolean isColliding(Entity entity1, Entity entity2) {
		if(entity1 == entity2) {
			return false;
		}
		if(!needToCheckForCollision(entity1, entity2)) {
			return false;
		}
		if(entity1.getBoundingBox().intersects(entity2.getBoundingBox())){
			return true;
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
	public final boolean isDrawingBoundingBoxes(){
		return drawBoundingBoxes;
	}

	/**
	 * Get whether or not the grid should be drawn.
	 * 
	 * @return
	 */
	public final boolean isDrawingGrid(){
		return this.drawGrid;
	}

	/**
	 * Get the amount of air friction in the level.
	 * 
	 * @return
	 */
	public float getAirFriction() {
		return airFriction;
	}

	/**
	 * Get the gravity on the level.
	 * 
	 * @return
	 */
	public PVector getGravity() {
		return gravity;
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
	 * Get a collection of all the entities in the level.
	 * .
	 * @return
	 */
	public Collection<Entity> getEntities() {
		return unmodifiableEntities;
	}

	/**
	 * Get the collection of entities to draw.
	 * .
	 * @return
	 */
	protected Collection<Entity> getEntitiesToDraw() {
		return unmodifiableEntities;
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
		this.camera.remove();
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
	 * 
	 * @param entity
	 */
	void removeEntity(Entity entity){
		removeGameObject(entity);
	}

	/**
	 * Remove a game object from the level.
	 * (To be called from the Entity class)
	 * 
	 * @param object
	 */
	void removeGameObject(GameObject object){
		gameObjectsToRemove.add(object);
		object.removeLevel();
	}

	/**
	 * Remove the all given game objects from the level.
	 * 
	 * @param toRemove
	 */
	protected void removeGameObjects(Collection<GameObject> toRemove) {
		gameObjects.removeAll(gameObjectsToRemove);
		entities.removeAll(toRemove);
	}

	public void removeLight(Light light){
		light.removeLevel();
		lights.remove(light);
		dLights.remove(light);
	}

	/**
	 * Returns whether or not collision detection needs to be done between the two entities.
	 * If false is returned, the two entities are not colliding.
	 * If true is returned, the two entities <i>might</i> be colliding.
	 * 
	 * @param entity1 The entity to check if it can collide with the other one
	 * @param entity2 The other entity to check against
	 * @return
	 */
	private boolean needToCheckForCollision(Entity entity1, Entity entity2) {
		int ent1Group = entity1.getCollisionGroup();
		int ent2Group = entity2.getCollisionGroup();
		
		if (ent1Group == 0 || ent2Group == 0) {
			return false;
		}
		
		if (entity1.getCollisionIgnoreEntities().contains(entity2)) {
			return false;
		}
		
		switch (entity1.getCollisionMode()) {
		case EQUAL_TO:
			return ent2Group == ent1Group;
		case LESS_THAN_OR_EQUAL_TO:
			return ent2Group <= ent1Group;
		case GREATER_THAN:
			return ent2Group >  ent1Group;
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
	 * Convert game units (for x location) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsXToPixels(float gameUnits) {
		return this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameWidth() / this.gridWidth;
	}
	
	/**
	 * Convert game units (for y location) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsYToPixels(float gameUnits) {
		return GameManager.getMe().getGameScene().getGameHeight() - (this.zoom * gameUnits * GameManager.getMe().getGameScene().getGameHeight() / this.gridHeight);
	}
	
	/**
	 * Convert game units (for z location) to pixels.
	 * 
	 * @param gameUnits
	 * @return
	 */
	float convertGridUnitsZToPixels(float gameUnits) {
		return convertGridUnitsXToPixels(gameUnits);
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
