package gamelib.game;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import gamelib.Drawable;
import gamelib.GameManager;
import gamelib.Updatable;
import gamelib.game.entities.PushableEntity;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Entity implements Updatable, Drawable {
	
	private PVector location;
	private PVector velocity;
	private PVector rotation;
	private PVector scale;
	
	private PVector locationOffset;
	private PVector velocityOffset;
	private PVector rotationOffset;
	private PVector scaleOffset;
	
	private float mass;
	
	private PVector maxLocation, minLocation;
	private PVector maxVelocity, minVelocity;
	private PVector maxRotation, minRotation;
	private float maxHorizontalVelocity = Float.NaN;
	
	private BoundingBox boundingBox;
	
	private Level level;
	private boolean gravityEffected;
	
	private int collisionGroup;
	private CollisionMode collisionMode;
	private Set<Entity> collisionIgnore;
	
	private Entity ground;

	private Entity attachedTo;
	private Set<Entity> attachedEntities;
	private Set<Entity> entitiesOnMe;
	
	private int drawMode;
	
	enum CollisionMode {
		GREATER_THAN_OR_EQUAL_TO, EQUAL_TO, LESS_THAN;
	}

	/**
	 * Create a 2D Entity.
	 * 
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 * @param width The width
	 * @param height The height
	 * @param drawMode The draw mode of this entity
	 */
	public Entity(Level level, float x, float y, float width, float height, int drawMode) {
		this(level, x, y, 0, width, height, 0, drawMode);
	}
	
	/**
	 * Create a 2D Entity.
	 * 
	 * @param level The level the entity will exist in
	 * @param location The location of this entity
	 * @param width The width
	 * @param height The height
	 * @param drawMode The draw mode of this entity
	 */
	public Entity(Level level, PVector location, float width, float height, int drawMode) {
		this(level, location.x, location.y, 0, width, 1, height, drawMode);
	}
	
	/**
	 * Create a 3D Entity.
	 * 
	 * @param level The level the entity will exist in
	 * @param location The location of this entity
	 * @param width The width
	 * @param height The height
	 * @param depth The depth
	 * @param drawMode The draw mode of this entity
	 */
	public Entity(Level level, PVector location, float width, float height, float depth, int drawMode) {
		this(level, location.x, location.y, location.z, width, height, depth, drawMode);
	}
	
	/**
	 * Create a 3D Entity.
	 * 
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 * @param z The z location
	 * @param width The width
	 * @param height The height
	 * @param depth The depth
	 * @param drawMode The draw mode of this entity
	 */
	public Entity(Level level, float x, float y, float z, float width, float height, float depth, int drawMode) {
		setLevel(level);
		this.location = new PVector(x, y, z);
		this.velocity = new PVector();
		this.rotation = new PVector();
		
		if (level.is3D()) {
			this.boundingBox = new BoundingBox3D(this, width, height, depth);
		} else {
			this.boundingBox = new BoundingBox2D(this, width, height);
		}
		
		this.scale = new PVector(1, 1, 1);
		
		this.locationOffset = new PVector();
		this.velocityOffset = new PVector();
		this.rotationOffset = new PVector();
		this.scaleOffset = new PVector();

		this.drawMode = drawMode;
		if (drawMode == PConstants.CENTER) {
			
		} else if (drawMode == PConstants.CORNER) {
			this.location.add(width / 2, height / 2, depth / 2);
		} else {
			throw new InvalidParameterException("Only CENTER and CORNER draw modes are supported.");
		}
		
		this.attachedEntities = new HashSet<Entity>();
		this.entitiesOnMe = new HashSet<Entity>();
		this.collisionIgnore = new HashSet<Entity>();
		
		this.mass = 1;
		this.gravityEffected = false;
		this.collisionGroup = 1;
		this.collisionMode = CollisionMode.GREATER_THAN_OR_EQUAL_TO;
		
		this.ground = null;
		
		this.maxLocation = null;
		this.minLocation = null;
		this.maxVelocity = null;
		this.minVelocity = null;
		this.maxRotation = null;
		this.minRotation = null;
	}

	/**
	 * Set the level that this entity is in.
	 * 
	 * @param level
	 */
	private void setLevel(Level level) {
		if(level == null) {
			remove();
		} else {
			this.level = level;
			level.addEntity(this);
		}
	}
	
	/**
	 * Remove this entity from the level it is in.
	 */
	public void remove(){
		this.level.removeEntity(this);
	}

	/**
	 * Set this entity's level to null.
	 * To be called from level.
	 */
	final void removeLevel() {
		level = null;
	}

	/**
	 * Update the entity.
	 * This method calls {@link #update(float)}.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 */
	final void _update(float delta){
		if(this.level == null) return;
		if(attachedTo != null) return;
		update(delta);
		if(this.level == null) return;
		applyMotionLimits();
		this.boundingBox.setLocation(PVector.add(this.location, this.locationOffset));
		
		boolean allChildrenCanMove = true;
		for(Entity ent : attachedEntities){
			if(!ent.updateAttached(delta)) allChildrenCanMove = false;
		}
		
		if(allChildrenCanMove){
			PVector currentLocation = PVector.add(this.location, this.locationOffset);
			PVector newLocation = getMoveToLocation(delta);
			
			if(!move(newLocation, currentLocation)){
				if(!move(new PVector(newLocation.x, currentLocation.y, currentLocation.z), currentLocation)){ velocity.x = 0; velocityOffset.x = 0; }
				if(!move(new PVector(currentLocation.x, currentLocation.y, newLocation.z), currentLocation)){ velocity.z = 0; velocityOffset.y = 0; }
				if(!move(new PVector(currentLocation.x, newLocation.y, currentLocation.z), currentLocation)){ velocity.y = 0; velocityOffset.z = 0; }
			}
		}
		
		applyLocationLimits();
		groundDetection();
	}

	private final boolean updateAttached(float delta){
		assert(attachedTo == null);
		update(delta);
		
		for(Entity ent : attachedEntities){
			ent.updateAttached(delta);
		}
		
		velocity.mult(0);
		velocityOffset.mult(0);
		
		PVector newLocation = getMoveToLocation(delta);
		return level.canMove(this, newLocation) == null;
	}

	/**
	 * Try and move this entity to the given location.
	 * 
	 * @param newLocation
	 * @return whether or not the move was successful
	 */
	private boolean move(PVector newLocation, PVector currentLocation) {
		Entity willCollideWith = level.canMove(this, newLocation);
		
		PVector dLocation = PVector.sub(newLocation, currentLocation);
		
		if(willCollideWith != null && willCollideWith instanceof PushableEntity){
			return moveAndPush((PushableEntity) willCollideWith, newLocation, dLocation);
		}
		else if(willCollideWith != null){
			return false;
		}
		
		moveNow(newLocation, dLocation);
		return true;
	}

	private void moveNow(PVector newLocation, PVector dLocation) {
		location.set(newLocation);
		for(Entity ent : attachedEntities){
			PVector nl = ent.getLocation();
			nl.add(dLocation);
			if(level.canMove(ent, nl ) == null){
				ent.location.set(nl);
			}
		}
		for(Entity ent : entitiesOnMe){
			PVector nl = ent.getLocation();
			nl.add(dLocation);
			if(level.canMove(ent, nl ) == null){
				ent.location.set(nl);
			}
		}
	}
	
	private boolean moveAndPush(PushableEntity pushee, PVector newLocation, PVector dLocation) {
		float resistance = pushee.getResistance();
		PVector pusheeDLocation = PVector.mult(dLocation, 1-resistance);
		PVector pusheeNewLocation = PVector.add(pushee.getLocation(), pusheeDLocation);
		
		if(level.canMove(pushee, pusheeNewLocation) != null) {
			return false;
		}

		dLocation.sub(pusheeDLocation);
		newLocation.sub(pusheeDLocation);
		
		((Entity)(pushee)).moveNow(pusheeNewLocation, pusheeDLocation);
		this.moveNow(newLocation, dLocation);
		
		this.velocity.mult(1-resistance);
		this.velocityOffset.mult(1-resistance);
		((Entity)(pushee)).velocity.add(
				this.velocity.x + this.velocityOffset.x,
				this.velocity.y + this.velocityOffset.y,
				this.velocity.z + this.velocityOffset.z);
		
		return true;
	}

	/**
	 * Move the entity.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 * @return The location that the entity will move to
	 */
	protected PVector getMoveToLocation(float delta){
		boolean onGround = isOnGround();
		
		if(gravityEffected && !onGround){
			applyAcceleration(level.getGravity(), level.getAirFriction(), delta);
		}
		if(onGround){
			applyAcceleration(0, 0, 0, ground.getGroundFriction(), delta);
		}
		
		return PVector.add(location, new PVector(
				this.velocity.x + this.velocityOffset.x * delta,
				this.velocity.y + this.velocityOffset.y * delta,
				this.velocity.z + this.velocityOffset.z * delta));
	}
	
	/**
	 * Draw the entity.
	 * This method calls {@link #draw(PGraphics)}.
	 * 
	 * @param g The graphics object to draw to
	 */
	final void _draw(PGraphics g){
		if(this.level == null) return;
		g.pushMatrix();
		if (this.level.is3D()) {
			g.translate(
					level.convertGridUnitsXToPixels(this.location.x + this.locationOffset.x),
					level.convertGridUnitsYToPixels(this.location.y + this.locationOffset.y),
					level.convertGridUnitsZToPixels(this.location.z + this.locationOffset.z));
			g.rotateX(this.rotation.x + this.rotationOffset.x);
			g.rotateY(this.rotation.y + this.rotationOffset.y);
			g.rotateZ(this.rotation.z + this.rotationOffset.z);
			g.scale(this.scale.x + this.scaleOffset.x, this.scale.y + this.scaleOffset.y, this.scale.z + this.scaleOffset.z);
		} else {
			g.translate(
					level.convertGridUnitsXToPixels(this.boundingBox.getCenterX() + this.locationOffset.x),
					level.convertGridUnitsYToPixels(this.boundingBox.getCenterY() + this.locationOffset.y));
			g.rotate(this.rotation.x + this.rotationOffset.x);
			g.scale(this.scale.x + this.scaleOffset.x, this.scale.y + this.scaleOffset.y);
		}
		g.pushStyle();
		g.rectMode(PConstants.CENTER);
		draw(g);
		g.popStyle();
		g.popMatrix();
		
		if (this.level.isDrawBoundingBoxes()) {
			drawBoundingBox(g);
		}
	}

	/**
	 * Draw a bounding box around the entity.
	 * 
	 * @param g The graphics object to draw to
	 */
	private void drawBoundingBox(PGraphics g) {
		if (this.level.is3D()) {
			drawBoundingBox3D(g);
		} else {
			drawBoundingBox2D(g);
		}
	}
	
	/**
	 * Draw a 2D bounding box around the entity.
	 * 
	 * @param g The graphics object to draw to
	 */
	private void drawBoundingBox2D(PGraphics g) {
		float bbx = level.convertGridUnitsXToPixels(this.boundingBox.getCenterX());
		float bby = level.convertGridUnitsYToPixels(this.boundingBox.getCenterY());
		float bbw = level.convertGridUnitsWidthToPixels(this.boundingBox.getWidth());
		float bbh = level.convertGridUnitsHeightToPixels(this.boundingBox.getHeight());
		
		g.pushStyle();
		g.rectMode(PConstants.CENTER);
		g.noFill();
		
		g.stroke(0xFFFFFFFF);
		g.strokeWeight(bbw > 10 && bbh > 10 ? 4F : 2F);
		g.rect(bbx, bby, bbw, bbh);
		
		g.stroke(0xFFFF0000);
		g.strokeWeight(bbw > 10 && bbh > 10 ? 2F : 1F);
		g.rect(bbx, bby, bbw, bbh);
		g.popStyle();
	}
	
	/**
	 * Draw a 3D bounding box around the entity.
	 * 
	 * @param g The graphics object to draw to
	 */
	private void drawBoundingBox3D(PGraphics g) {
		g.stroke(0xFFFF0000);
		g.strokeWeight(1F);
		g.noFill();
		g.box(boundingBox.getWidth(), boundingBox.getHeight(), boundingBox.getDepth());
	}
	
	private void groundDetection(){
		Entity lastGround = ground;
		ground = level.getGround(this);
		if(lastGround != ground){
			if(lastGround != null) lastGround.takeOff(this);
			if(ground != null) ground.putOn(this);
		}
	}
	
	public float getGroundFriction(){
		return 10;
	}
	
	/**
	 * Apply a force on the entity.
	 * 
	 * @param fx X force
	 * @param fy Y force
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to apply the force for
	 * @return The distance traveled be the entity
	 */
	public PVector applyForce(float fx, float fy, float friction, float time){
		return applyForce(fx, fy, 0, friction, time);
	}
	
	/**
	 * Apply a force on the entity.
	 * 
	 * @param fx X force
	 * @param fy Y force
	 * @param fz Z force
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to apply the force for
	 * @return The distance traveled be the entity
	 */
	public PVector applyForce(float fx, float fy, float fz, float friction, float time){
		return applyForce(new PVector(fx/mass, fy/mass, fz/mass), friction, time);
	}
	
	/**
	 * Apply a force on the entity.
	 * 
	 * @param f The force to apply
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to apply the force for
	 * @return The distance traveled be the entity
	 */
	public PVector applyForce(PVector f, float friction, float time){
		return accelerate(velocity, PVector.div(f, mass), friction, time);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * 
	 * @param ax X acceleration
	 * @param ay Y acceleration
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to accelerate for
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(float ax, float ay, float friction, float time){
		return applyAcceleration(ax, ay, 0, friction, time);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * 
	 * @param ax X acceleration
	 * @param ay Y acceleration
	 * @param az Z acceleration
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to accelerate for
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(float ax, float ay, float az, float friction, float time){
		return applyAcceleration(new PVector(ax, ay, az), friction, time);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * 
	 * No friction will be applied to the acceleration.
	 * @param a The acceleration to apply
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(PVector a, float time){
		return applyAcceleration(a, 0, time);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * 
	 * @param a The acceleration to apply
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to accelerate for
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(PVector a, float friction, float time){
		return accelerate(velocity, a, friction, time);
	}
	
	public void addLocation(float x, float y) {
		addLocation(x, y, 0);
	}
	
	public void addLocation(float x, float y, float z) {
		addLocation(new PVector(x, y, z));
	}
	
	public void addLocation(PVector dLocation) {
		this.location.add(dLocation);
	}
	
	public void addVelocity(float x, float y) {
		addVelocity(x, y, 0);
	}
	
	public void addVelocity(float x, float y, float z) {
		addVelocity(new PVector(x, y, z));
	}
	
	public void addVelocity(PVector dVelocity) {
		this.velocity.add(dVelocity);
	}
	
	/**
	 * Accelerate the given velocity by the given acceleration, applying the give amount of friction.
	 * 
	 * @param veloc The velocity to accelerate
	 * @param accel The amount if acceleration
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @param time The amount of time to accelerate for
	 * @return The distance traveled
	 */
	public static PVector accelerate(PVector velocity, PVector acceleration, float friction, float time) {
		PVector distance;

		if (friction == 0) {
			distance = PVector.add(PVector.mult(velocity, time), PVector.mult(acceleration, 0.5F * time * time));
			velocity.add(PVector.mult(acceleration, time));
		} else {
			distance = PVector.add(PVector.mult(acceleration, time / friction), PVector.mult(PVector.sub(velocity, PVector.div(acceleration, friction)), (float) ((1 - Math.exp(-friction * time)) / friction)));
			velocity.set(PVector.add(PVector.div(acceleration, friction), PVector.mult(PVector.sub(velocity, PVector.div(acceleration, friction)), (float) Math.exp(-friction * time))));
		}
		
		return distance;
	}
	
	public void attach(Entity entity){
		if(entity == this) throw new RuntimeException("Cannot attach an entity to itself.");
		entity.deattach(this);
		this.attachedEntities.add(entity);
		this.ignoreInCollisions(entity);
		entity.ignoreInCollisions(this);
		entity.attachedTo = this;
	}
	
	public void deattach(Entity entity){
		this.attachedEntities.remove(entity);
		this.unignoreInCollisions(entity);
		entity.unignoreInCollisions(this);
		entity.attachedTo = null;
	}
	
	/**
	 * Put the given entity on me.
	 * 
	 * @param entity
	 */
	private void putOn(Entity entity){
		assert(entity != this);
		entity.entitiesOnMe.remove(this);
		this.entitiesOnMe.add(entity);
	}
	
	/**
	 * Take the given entity off of me.
	 * 
	 * @param entity
	 */
	private void takeOff(Entity entity){
		this.entitiesOnMe.remove(entity);
		entity.velocity.add(this.velocity);
		entity.velocity.add(this.velocityOffset);
	}
	
	/**
	 * Get the level that this entity is apart of.
	 * 
	 * @return the level
	 */
	public final Level getLevel(){
		return level;
	}

	/**
	 * Get the x location of this entity (in grid units).
	 * 
	 * @return the x location
	 */
	public float getX(){
		return location.x;
	}

	/**
	 * Get the x location of this entity (in pixels).
	 * 
	 * @return the x location
	 */
	public float getXInPixels(){
		return level.convertGridUnitsXToPixels(location.x);
	}

	/**
	 * Get the y location of this entity (in grid units).
	 * 
	 * @return the y location
	 */
	public float getY(){
		return location.y;
	}

	/**
	 * Get the y location of this entity (in pixels).
	 * 
	 * @return the y location
	 */
	public float getYInPixels(){
		return level.convertGridUnitsYToPixels(location.y);
	}

	/**
	 * Get the z location of this entity (in grid units).
	 * 
	 * @return the z location
	 */
	public float getZ(){
		return location.z;
	}

	/**
	 * Get the z location of this entity (in pixels).
	 * 
	 * @return the z location
	 */
	public float getZInPixels(){
		return level.convertGridUnitsZToPixels(location.z);
	}

	/**
	 * Get the location of this entity (in grid units).
	 * 
	 * @return the location
	 */
	public PVector getLocation(){
		return location.copy();
	}

	/**
	 * Get the location of this entity (in pixels).
	 * 
	 * @return the location
	 */
	public PVector getLocationInPixels(){
		return new PVector(getXInPixels(), getYInPixels(), getZInPixels());
	}
	
	/**
	 * Get the x location offset of this entity (in grid units).
	 * 
	 * @return the x location
	 */
	public float getXOffset(){
		return locationOffset.x;
	}

	/**
	 * Get the x location offset of this entity (in pixels).
	 * 
	 * @return the x location
	 */
	public float getXOffsetInPixels(){
		return level.convertGridUnitsXToPixels(locationOffset.x);
	}

	/**
	 * Get the y location offset of this entity (in grid units).
	 * 
	 * @return the y location
	 */
	public float getYOffset(){
		return locationOffset.y;
	}

	/**
	 * Get the y location offset of this entity (in pixels).
	 * 
	 * @return the y location
	 */
	public float getYOffsetInPixels(){
		return level.convertGridUnitsYToPixels(locationOffset.y);
	}

	/**
	 * Get the z location offset of this entity (in grid units).
	 * 
	 * @return the z location
	 */
	public float getZOffset(){
		return locationOffset.z;
	}

	/**
	 * Get the z location offset of this entity (in pixels).
	 * 
	 * @return the z location
	 */
	public float getZOffsetInPixels(){
		return level.convertGridUnitsZToPixels(locationOffset.z);
	}

	/**
	 * Get the location offset of this entity (in grid units).
	 * 
	 * @return the location offset
	 */
	public PVector getLocationOffset(){
		return locationOffset.copy();
	}
	
	/**
	 * Get the location offset of this entity (in pixels).
	 * 
	 * @return the location offset
	 */
	public PVector getLocationOffsetInPixels(){
		return new PVector(getXOffsetInPixels(), getYOffsetInPixels(), getZOffsetInPixels());
	}

	/**
	 * Get the width of this entity (in grid units).
	 * 
	 * @return the width
	 */
	public float getWidth() {
		return boundingBox.getWidth();
	}

	/**
	 * Get the width of this entity (in pixels).
	 * 
	 * @return the width
	 */
	public float getWidthInPixels() {
		return level.convertGridUnitsWidthToPixels(boundingBox.getWidth());
	}

	/**
	 * Get the height of this entity (in grid units).
	 * 
	 * @return the height
	 */
	public float getHeight() {
		return boundingBox.getHeight();
	}

	/**
	 * Get the height of this entity (in pixels).
	 * 
	 * @return the height
	 */
	public float getHeightInPixels() {
		return level.convertGridUnitsHeightToPixels(boundingBox.getHeight());
	}

	/**
	 * Get the depth of this entity (in grid units).
	 * 
	 * @return the depth
	 */
	public float getDepth(){
		return boundingBox.getDepth();
	}

	/**
	 * Get the depth of this entity (in pixels).
	 * 
	 * @return the depth
	 */
	public float getDepthInPixels(){
		return level.convertGridUnitsDepthToPixels(boundingBox.getDepth());
	}

	/**
	 * Get the size of this entity (in grid units).
	 * 
	 * @return the location
	 */
	public PVector getSize(){
		return new PVector(getWidth(), getHeight(), getDepth());
	}
	
	/**
	 * Get the size of this entity (in pixels).
	 * 
	 * @return the location
	 */
	public PVector getSizeInPixels(){
		return new PVector(getWidthInPixels(), getHeightInPixels(), getDepthInPixels());
	}

	/**
	 * Get the x velocity of this entity.
	 * 
	 * @return the x velocity
	 */
	public float getVelocityX(){
		return velocity.x;
	}

	/**
	 * Get the y velocity of this entity.
	 * 
	 * @return the y velocity
	 */
	public float getVelocityY(){
		return velocity.y;
	}

	/**
	 * Get the z velocity of this entity.
	 * 
	 * @return the z velocity
	 */
	public float getVelocityZ(){
		return velocity.z;
	}

	/**
	 * Get the velocity of this entity.
	 * 
	 * @return the velocity
	 */
	public PVector getVelocity(){
		return velocity.copy();
	}

	/**
	 * Get the x velocity offset of this entity.
	 * 
	 * @return the x velocity
	 */
	public float getVelocityXOffset(){
		return velocityOffset.x;
	}

	/**
	 * Get the y velocity offset of this entity.
	 * 
	 * @return the y velocity
	 */
	public float getVelocityYOffset(){
		return velocityOffset.y;
	}

	/**
	 * Get the z velocity offset of this entity.
	 * 
	 * @return the z velocity
	 */
	public float getVelocityZOffset(){
		return velocityOffset.z;
	}

	/**
	 * Get the velocity offset of this entity.
	 * 
	 * @return the velocity offset
	 */
	public PVector getVelocityOffset(){
		return velocityOffset.copy();
	}

	/**
	 * Get the tilt rotation of this entity.
	 * 
	 * @return the tilt rotation
	 */
	public float getRotationTilt(){
		return rotation.x;
	}

	/**
	 * Get the pan rotation of this entity.
	 * 
	 * @return the pan rotation
	 */
	public float getRotationPan(){
		return rotation.y;
	}

	/**
	 * Get the roll rotation of this entity.
	 * 
	 * @return the roll rotation
	 */
	public float getRotationRoll(){
		return rotation.z;
	}
	
	/**
	 * Get the rotation of this entity.
	 * 
	 * @return the rotation
	 */
	public float getRotation2D(){
		return rotation.x;
	}
	
	/**
	 * Get the rotation of this entity (tilt, pan, roll).
	 * 
	 * @return the rotation
	 */
	public PVector getRotation3D(){
		return rotation.copy();
	}
	
	/**
	 * Get the tilt rotation offset of this entity.
	 * 
	 * @return the tilt rotation
	 */
	public float getRotationTiltOffset(){
		return rotationOffset.x;
	}

	/**
	 * Get the pan rotation offset of this entity.
	 * 
	 * @return the pan rotation
	 */
	public float getRotationPanOffset(){
		return rotationOffset.y;
	}

	/**
	 * Get the roll rotation offset of this entity.
	 * 
	 * @return the roll rotation
	 */
	public float getRotationRollOffset(){
		return rotationOffset.z;
	}
	
	/**
	 * Get the rotation offset of this entity.
	 * 
	 * @return the rotation
	 */
	public float getRotationOffset2D(){
		return rotationOffset.x;
	}
	
	/**
	 * Get the rotation offset of this entity (tilt, pan, roll).
	 * 
	 * @return the rotation
	 */
	public PVector getRotationOffset3D(){
		return rotationOffset.copy();
	}

	/**
	 * Get the x scale of this entity.
	 * 
	 * @return The x scale
	 */
	public float getScaleX(){
		return scale.x;
	}

	/**
	 * Get the x scale of this entity.
	 * 
	 * @return The x scale
	 */
	public float getScaleY(){
		return scale.y;
	}
	
	/**
	 * Get the z scale of this entity.
	 * 
	 * @return The z scale
	 */
	public float getScaleZ(){
		return scale.z;
	}
	
	/**
	 * Get the scale of this entity.
	 * 
	 * @return The scale
	 */
	public PVector getScale(){
		return scale.copy();
	}

	/**
	 * Get the x scale offset of this entity.
	 * 
	 * @return The x scale
	 */
	public float getScaleXOffset(){
		return scaleOffset.x;
	}

	/**
	 * Get the x scale offset of this entity.
	 * 
	 * @return The x scale
	 */
	public float getScaleYOffset(){
		return scaleOffset.y;
	}
	
	/**
	 * Get the z scale offset of this entity.
	 * 
	 * @return The z scale
	 */
	public float getScaleZOffset(){
		return scaleOffset.z;
	}
	
	/**
	 * Get the scale offset of this entity.
	 * 
	 * @return The scale
	 */
	public PVector getScaleOffset(){
		return scaleOffset.copy();
	}
	
	/**
	 * Get the mass of this entity.
	 * 
	 * @return The mass
	 */
	public float getMass() {
		return mass;
	}
	
	public Entity getGroundEntity() {
		return ground;
	}
	
	/**
	 * Get the bounding box used by this entity.
	 * 
	 * @return
	 */
	BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	/**
	 * Get the bounding box used by this entity.
	 * 
	 * @return
	 */
	public BoundingBox3D getBoundingBox3D() {
		if(!(boundingBox instanceof BoundingBox3D)) throw new RuntimeException("Cannot get a 3D bounding for a 2D entity.");
		return (BoundingBox3D) boundingBox;
	}
	
	/**
	 * Get the bounding box used by this entity.
	 * 
	 * @return
	 */
	public BoundingBox2D getBoundingBox2D() {
		if(!(boundingBox instanceof BoundingBox2D)) throw new RuntimeException("Cannot get a 2D bounding for a 3D entity.");
		return (BoundingBox2D) boundingBox;
	}
	
	/**
	 * Get the collision group of this entity.
	 * 
	 * @return
	 */
	int getCollisionGroup() {
		return collisionGroup;
	}
	
	/**
	 * Get the collision mode that this entity uses.
	 * 
	 * @return
	 */
	CollisionMode getCollisionMode() {
		return collisionMode;
	}
	
	Set<Entity> getCollisionIgnoreEntities(){
		return collisionIgnore;
	}

	/**
	 * Get whether or not this entity is effected by gravity.
	 */
	public boolean isGravityEffected(){
		return gravityEffected;
	}
	
	/**
	 * Returns whether or not this entity is on the ground.
	 * 
	 * @return
	 */
	public boolean isOnGround() {
		return ground != null;
	}

	/**
	 * Set the x location of the entity.
	 * 
	 * @param x
	 */
	public void setLocationX(float x){
		location.x = x;
	}
	
	/**
	 * Set the y location of the entity.
	 * 
	 * @param y
	 */
	public void setLocationY(float y){
		location.y = y;
	}
	
	/**
	 * Set the z location of the entity.
	 * (For 3D games only)
	 * 
	 * @param z
	 */
	public void setLocationZ(float z){
		location.z = z;
	}
	
	/**
	 * Set the location of the entity.
	 * (For 2D games only)
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(float x, float y){
		location.set(x, y, 0);
	}
	
	/**
	 * Set the location of the entity.
	 * (For 3D games only)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLocation(float x, float y, float z){
		location.set(x, y, z);
	}

	/**
	 * Set the location of the entity.
	 * 
	 * @param loc
	 */
	public void setLocation(PVector loc){
		location.set(loc);
	}

	/**
	 * Set the x location offset of the entity.
	 * 
	 * @param x
	 */
	public void setLocationOffsetX(float x){
		locationOffset.x = x;
	}
	
	/**
	 * Set the y location offset of the entity.
	 * 
	 * @param y
	 */
	public void setLocationOffsetY(float y){
		locationOffset.y = y;
	}
	
	/**
	 * Set the z location offset of the entity.
	 * 
	 * (For 3D games only)
	 * @param z
	 */
	public void setLocationOffsetZ(float z){
		locationOffset.z = z;
	}
	
	/**
	 * Set the location offset of the entity.
	 * (For 2D games only)
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocationOffset(float x, float y){
		locationOffset.set(x, y, 0);
	}
	
	/**
	 * Set the location offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLocationOffset(float x, float y, float z){
		locationOffset.set(x, y, z);
	}

	/**
	 * Set the location offset of the entity.
	 * 
	 * @param loc
	 */
	public void setLocationOffset(PVector loc){
		locationOffset.set(loc);
	}
	
	/**
	 * Set the x speed of the entity.
	 * 
	 * @param vx
	 */
	public void setVelocityX(float vx){
		velocity.x = vx;
	}
	
	/**
	 * Set the y speed of the entity.
	 * 
	 * @param vy
	 */
	public void setVelocityY(float vy){
		velocity.y = vy;
	}
	
	/**
	 * Set the z speed of the entity.
	 * (For 3D games only)
	 * 
	 * @param vz
	 */
	public void setVelocityZ(float vz){
		velocity.z = vz;
	}
	
	/**
	 * Set the speed of the entity.
	 * (For 2D games only)
	 * 
	 * @param vx
	 * @param vy
	 */
	public void setVelocity(float vx, float vy){
		velocity.set(vx, vy, 0);
	}
	
	/**
	 * Set the speed of the entity.
	 * (For 3D games only)
	 * 
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void setVelocity(float vx, float vy, float vz){
		velocity.set(vx, vy, vz);
	}
	
	/**
	 * Set the speed of the entity.
	 * 
	 * @param v
	 */
	public void setVelocity(PVector v){
		velocity.set(v);
	}
	
	/**
	 * Set the x velocity offset of the entity.
	 * 
	 * @param vx
	 */
	public void setVelocityOffsetX(float vx){
		velocityOffset.x = vx;
	}
	
	/**
	 * Set the y velocity offset of the entity.
	 * 
	 * @param vy
	 */
	public void setVelocityOffsetY(float vy){
		velocityOffset.y = vy;
	}
	
	/**
	 * Set the z velocity offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param vz
	 */
	public void setVelocityOffsetZ(float vz){
		velocityOffset.z = vz;
	}
	
	/**
	 * Set the velocity offset of the entity.
	 * (For 2D games only)
	 * 
	 * @param vx
	 * @param vy
	 */
	public void setVelocityOffset(float vx, float vy){
		velocityOffset.set(vx, vy, 0);
	}
	
	/**
	 * Set the velocity offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void setVelocityOffset(float vx, float vy, float vz){
		velocityOffset.set(vx, vy, vz);
	}
	
	/**
	 * Set the velocity offset of the entity.
	 * 
	 * @param v
	 */
	public void setVelocityOffset(PVector v){
		velocityOffset.set(v);
	}
	
	/**
	 * Set the tilt rotation of the entity.
	 * This is the rotation in the xy plain.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 */
	public void setRotationTilt(float tilt){
		rotation.x = tilt;
	}

	/**
	 * Set the pan rotation of the entity.
	 * This is the rotation in the xz plain.
	 * (For 3D games only)
	 * 
	 * @param pan
	 */
	public void setRotationPan(float pan){
		rotation.y = pan;
	}
	
	/**
	 * Set the roll rotation of the entity.
	 * This is the rotation in the yz plain.
	 * (For 3D games only)
	 * 
	 * @param roll
	 */
	public void setRotationRoll(float roll){
		rotation.z = roll;
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 2D games only)
	 * 
	 * @param rotation
	 */
	public void setRotation(float rotation){
		this.rotation.x = rotation;
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 * @param pan
	 * @param roll
	 */
	public void setRotation(float tilt, float pan, float roll){
		rotation.set(tilt, pan, roll);
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 3D games only)
	 * 
	 * @param rotation
	 */
	public void setRotation(PVector rotation){
		this.rotation.set(rotation);
	}
	
	/**
	 * Set the tilt rotation offset of the entity.
	 * This is the rotation in the xy plain.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 */
	public void setRotationOffsetTilt(float tilt){
		rotationOffset.x = tilt;
	}

	/**
	 * Set the pan rotation offset of the entity.
	 * This is the rotation in the xz plain.
	 * (For 3D games only)
	 * 
	 * @param pan
	 */
	public void setRotationOffsetPan(float pan){
		rotationOffset.y = pan;
	}
	
	/**
	 * Set the roll rotation offset of the entity.
	 * This is the rotation in the yz plain.
	 * (For 3D games only)
	 * 
	 * @param roll
	 */
	public void setRotationOffsetRoll(float roll){
		rotationOffset.z = roll;
	}
	
	/**
	 * Set the rotation offset of the entity.
	 * (For 2D games only)
	 * 
	 * @param rotation
	 */
	public void setRotationOffset(float rotation){
		this.rotationOffset.x = rotation;
	}
	
	/**
	 * Set the rotation offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 * @param pan
	 * @param roll
	 */
	public void setRotationOffset(float tilt, float pan, float roll){
		rotationOffset.set(tilt, pan, roll);
	}
	
	/**
	 * Set the rotation offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param rotation
	 */
	public void setRotationOffset(PVector rotation){
		this.rotationOffset.set(rotation);
	}
	
	/**
	 * Set the scale of the entity uniformly to the given value
	 * 
	 * @param s The new scale of the entity
	 */
	public void setScale(float s){
		scale.set(s, s, s);
	}
	
	/**
	 * Set the scale of the entity.
	 * 
	 * @param x The x scale of the entity
	 * @param y The y scale of the entity
	 */
	public void setScale(float x, float y){
		scale.set(x, y, scale.z);
	}
	
	/**
	 * Set the scale of the entity.
	 * 
	 * @param x The x scale of the entity
	 * @param y The y scale of the entity
	 * @param z The z scale of the entity
	 */
	public void setScale(float x, float y, float z){
		scale.set(x, y, z);
	}
	
	/**
	 * Set the scale offset of the entity uniformly to the given value
	 * 
	 * @param s The new scale offset of the entity
	 */
	public void setScaleOffset(float s){
		scaleOffset.set(s, s, s);
	}
	
	/**
	 * Set the scale offset of the entity.
	 * 
	 * @param x The x scale offset of the entity
	 * @param y The y scale offset of the entity
	 */
	public void setScaleOffset(float x, float y){
		scaleOffset.set(x, y, scaleOffset.z);
	}
	
	/**
	 * Set the scale offset of the entity.
	 * 
	 * @param x The x scale offset of the entity
	 * @param y The y scale offset of the entity
	 * @param z The z scale offset of the entity
	 */
	public void setScaleOffset(float x, float y, float z){
		scaleOffset.set(x, y, z);
	}
	
	/**
	 * Set the mass of the entity.
	 * The mass must be greater than 0.
	 * 
	 * @param mass The mass
	 */
	public void setMass(float mass) {
		if(mass<=0) throw new RuntimeException("Can not set mass to zero or a negitive value.");
		this.mass = mass;
	}
	
	/**
	 * Set whether or not this entity is effected by gravity.
	 * 
	 * @param b
	 */
	public void setGravityEffected(boolean b){
		gravityEffected = b;
	}
	
	/**
	 * Set the collision group that this entity is apart of.
	 * The group number cannot be negative. If group value equals 0 then the entity will not collide with any thing.
	 * 
	 * @param group The group to put this entity in
	 */
	public void setCollisionGroup(int group){
		if(group < 0) throw new RuntimeException("Cannot set an entity's collision group to a negative namuber.");
		collisionGroup = group;
	}
	
	/**
	 * Set the collision mode that this entity uses.
	 * Modes:
	 * <ul>
	 * <li><strong>null</strong>: do not collide with anything</li>
	 * <li><strong>GREATER_THAN_OR_EQUAL_TO</strong>: will only collide with entities in the same or a higher collision group</li>
	 * <li><strong>EQUAL_TO</strong>: will only collide with entities in the same collision group</li>
	 * <li><strong>LESS_THAN</strong>: will only collide with entities in a lower collision group</li>
	 * </ul>
	 * 
	 * @param mode The collision mode to use
	 */
	public void setCollisionMode(CollisionMode mode){
		if(mode == null){
			setCollisionGroup(0);
		}
		else{
			collisionMode = mode;
		}
	}
	
	/**
	 * See {@link #setCollisionGroup(int)} and {@link #setCollisionMode(CollisionMode)} for details.
	 * 
	 * @param group The group to put this entity in
	 * @param mode The collision mode to use
	 * @see setCollisionGroup(int)
	 * @see setCollisionMode(CollisionMode)
	 */
	public void setCollisionMode(int group, CollisionMode mode){
		setCollisionGroup(group);
		setCollisionMode(mode);
	}
	
	public void setBoundingBox3D(BoundingBox3D boundingBox) {
		if(boundingBox == null) throw new IllegalArgumentException("Cannot set an entity's bounding box to null.");
		if(!level.is3D()) throw new UnsupportedOperationException("Cannot use a 3D bounding box in a 2D level.");
		this.boundingBox = boundingBox;
	}
	
	public void setBoundingBox2D(BoundingBox2D boundingBox) {
		if(boundingBox == null) throw new IllegalArgumentException("Cannot set an entity's bounding box to null.");
		if(level.is3D()) throw new UnsupportedOperationException("Cannot use a 2D bounding box in a 3D level.");
		this.boundingBox = boundingBox;
	}
	
	public void ignoreInCollisions(Entity entity){
		collisionIgnore.add(entity);
	}
	
	public void unignoreInCollisions(Entity entity){
		collisionIgnore.remove(entity);
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension less than the give values.
	 * 
	 * @param minX The minimum x location this entity can be
	 * @param minY The minimum y location this entity can be
	 */
	public void limitLocationMin(float minX, float minY){
		limitLocationMin(new PVector(minX, minY));
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension less than the give values.
	 * 
	 * @param minX The minimum x location this entity can be
	 * @param minY The minimum y location this entity can be
	 * @param minZ The minimum z location this entity can be
	 */
	public void limitLocationMin(float minX, float minY, float minZ){
		limitLocationMin(new PVector(minX, minY, minZ));
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension less than the give values.
	 * 
	 * @param min The minimum location this entity can be
	 */
	public void limitLocationMin(PVector min){
		if(min == null){
			minLocation = null;
			return;
		}
		if(minLocation == null){
			minLocation = min.copy();
		}
		else{
			minLocation.set(min);
		}
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension greater than the give values.
	 * 
	 * @param maxX The minimum x location this entity can be
	 * @param maxY The minimum y location this entity can be
	 */
	public void limitLocationMax(float maxX, float maxY){
		limitLocationMax(new PVector(maxX, maxY));
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension greater than the give values.
	 * 
	 * @param maxX The minimum x location this entity can be
	 * @param maxY The minimum y location this entity can be
	 * @param maxZ The minimum z location this entity can be
	 */
	public void limitLocationMax(float maxX, float maxY, float maxZ){
		limitLocationMax(new PVector(maxX, maxY, maxZ));
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension greater than the give values.
	 * 
	 * @param max The minimum location this entity can be
	 */
	public void limitLocationMax(PVector max){
		if(max == null){
			maxLocation = null;
			return;
		}
		if(maxLocation == null){
			maxLocation = max.copy();
		}
		else{
			maxLocation.set(max);
		}
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go slower than the given velocity.
	 * 
	 * @param minX The minimum x velocity of this entity
	 * @param minY The minimum y velocity of this entity
	 */
	public void limitVelocityMin(float minX, float minY){
		limitVelocityMin(new PVector(minX, minY));
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go slower than the given velocity.
	 * 
	 * @param minX The minimum x velocity of this entity
	 * @param minY The minimum y velocity of this entity
	 * @param minZ The minimum z velocity of this entity
	 */
	public void limitVelocityMin(float minX, float minY, float minZ){
		limitVelocityMin(new PVector(minX, minY, minZ));
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go slower than the given velocity.
	 * 
	 * @param min The minimum velocity of this entity
	 */
	public void limitVelocityMin(PVector min){
		if(min == null){
			minVelocity = null;
			return;
		}
		if(minVelocity == null){
			minVelocity = min.copy();
		}
		else{
			minVelocity.set(min);
		}
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go faster than the given velocity.
	 * 
	 * @param maxX The minimum x velocity of this entity
	 * @param maxY The minimum y velocity of this entity
	 */
	public void limitVelocityMax(float maxX, float maxY){
		limitVelocityMax(new PVector(maxX, maxY));
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go faster than the given velocity.
	 * 
	 * @param maxX The maximum x velocity of this entity
	 * @param maxY The maximum y velocity of this entity
	 * @param maxZ The maximum z velocity of this entity
	 */
	public void limitVelocityMax(float maxX, float maxY, float maxZ){
		limitVelocityMax(new PVector(maxX, maxY, maxZ));
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go faster than the given velocity.
	 * 
	 * @param max The maximum velocity of this entity
	 */
	public void limitVelocityMax(PVector max){
		if(max == null){
			maxVelocity = null;
			return;
		}
		if(maxVelocity == null){
			maxVelocity = max.copy();
		}
		else{
			maxVelocity.set(max);
		}
	}
	
	public void limitVelocityHorizontal(float maxHorizontalVelocity){
		this.maxHorizontalVelocity = maxHorizontalVelocity;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation less than the give value.
	 * 
	 * @param ang The minimum angle that this entity can be at
	 */
	public void limitRotationMin(float ang){
		limitRotationMin(new PVector(ang, 0, 0));
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation less than any of the give values.
	 * 
	 * @param minTilt The minimum tilt angle that this entity can be at
	 * @param minPan The minimum pan angle that this entity can be at
	 * @param minRoll The minimum roll angle that this entity can be at
	 */
	public void limitRotationMin(float minTilt, float minPan, float minRoll){
		limitRotationMin(new PVector(minTilt, minPan, minRoll));
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation less than any of the give values.
	 * 
	 * @param min The minimum angles that this entity can be at
	 */
	public void limitRotationMin(PVector min){
		if(min == null){
			minRotation = null;
			return;
		}
		if(minRotation == null){
			minRotation = min.copy();
		}
		else{
			minRotation.set(min);
		}
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation greater than the give value.
	 * 
	 * @param ang The maximum angle that this entity can be at
	 */
	public void limitRotationMax(float ang){
		limitRotationMax(new PVector(ang, 0, 0));
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation greater than any of the give values.
	 * 
	 * @param maxTilt The maximum tilt angle that this entity can be at
	 * @param maxPan The maximum pan angle that this entity can be at
	 * @param maxRoll The maximum roll angle that this entity can be at
	 */
	public void limitRotationMax(float maxTilt, float maxPan, float maxRoll){
		limitRotationMax(new PVector(maxTilt, maxPan, maxRoll));
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation greater than any of the give values.
	 * 
	 * @param max The maximum angles that this entity can be at
	 */
	public void limitRotationMax(PVector max){
		if(max == null){
			maxRotation = null;
			return;
		}
		if(maxRotation == null){
			maxRotation = max.copy();
		}
		else{
			maxRotation.set(max);
		}
	}
	
	/**
	 * Remove the minimum limits applied to the location of this entity.
	 */
	public void removeLimitLocationMin(){
		limitLocationMin(null);
	}

	/**
	 * Remove the maximum limits applied to the location of this entity.
	 */
	public void removeLimitLocationMax(){
		limitLocationMax(null);
	}

	/**
	 * Remove the limits applied to the location of this entity.
	 */
	public void removeLimitLocation(){
		removeLimitLocationMin();
		removeLimitLocationMax();
	}

	/**
	 * Remove the minimum limits applied to this entity's velocity.
	 */
	public void removeLimitVelocityMin(){
		limitVelocityMin(null);
	}

	/**
	 * Remove the maximum limits applied to this entity's velocity.
	 */
	public void removeLimitVelocityMax(){
		limitVelocityMax(null);
	}

	/**
	 * Remove the limits applied to this entity's velocity.
	 */
	public void removeLimitVelocity(){
		removeLimitVelocityMin();
		removeLimitVelocityMax();
		removeLimitVelocityHorizontal();
	}

	public void removeLimitVelocityHorizontal(){
		this.maxHorizontalVelocity = Float.NaN;
	}

	/**
	 * Remove the minimum limits applied to the rotation of this entity.
	 */
	public void removeLimitRotationMin(){
		limitRotationMin(null);
	}

	/**
	 * Remove the maximum limits applied to the rotation of this entity.
	 */
	public void removeLimitRotationMax(){
		limitRotationMax(null);
	}
	
	/**
	 * Remove the limits applied to the rotation of this entity.
	 */
	public void removeLimitRotation(){
		removeLimitRotationMin();
		removeLimitRotationMax();
	}
	
	/**
	 * Apply the limits the location of this entity.
	 */
	private void applyLocationLimits(){
		if(minLocation != null){
			location.x = Math.max(location.x - locationOffset.x, minLocation.x);
			location.y = Math.max(location.y - locationOffset.y, minLocation.y);
			location.z = Math.max(location.z - locationOffset.z, minLocation.z);
		}
		if(maxLocation != null){
			location.x = Math.min(location.x - locationOffset.x, maxLocation.x);
			location.y = Math.min(location.y - locationOffset.y, maxLocation.y);
			location.z = Math.min(location.z - locationOffset.z, maxLocation.z);
		}
		if(minRotation != null){
			rotation.x = Math.max(rotation.x - rotationOffset.x, minRotation.x);
			rotation.y = Math.max(rotation.y - rotationOffset.y, minRotation.y);
			rotation.z = Math.max(rotation.z - rotationOffset.z, minRotation.z);
		}
		if(maxRotation != null){
			rotation.x = Math.min(rotation.x - rotationOffset.x, maxRotation.x);
			rotation.y = Math.min(rotation.y - rotationOffset.y, maxRotation.y);
			rotation.z = Math.min(rotation.z - rotationOffset.z, maxRotation.z);
		}
	}
	
	/**
	 * Apply the limits the motion of this entity.
	 */
	private void applyMotionLimits() {
		if(minVelocity != null){
			velocity.x = Math.max(velocity.x - velocityOffset.x, minVelocity.x);
			velocity.y = Math.max(velocity.y - velocityOffset.y, minVelocity.y);
			velocity.z = Math.max(velocity.z - velocityOffset.z, minVelocity.z);
		}
		if(maxVelocity != null){
			velocity.x = Math.min(velocity.x - velocityOffset.x, maxVelocity.x);
			velocity.y = Math.min(velocity.y - velocityOffset.y, maxVelocity.y);
			velocity.z = Math.min(velocity.z - velocityOffset.z, maxVelocity.z);
		}
		if(!Float.isNaN(maxHorizontalVelocity)){
			PVector temp = velocity.copy();
			temp.x += this.velocityOffset.x;
			temp.y =  0;
			temp.z += this.velocityOffset.z;
			if(temp.mag() > maxHorizontalVelocity){
				temp.normalize();
				temp.mult(maxHorizontalVelocity);
				velocity.x = temp.x;
				velocity.z = temp.z;
			}
		}
	}
}