package gamelib.game;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gamelib.Drawable;
import gamelib.game.entities.PushableEntity;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Entity extends GameObject implements Drawable {
	
	private final PVector rotation;
	private final PVector scale;
	
	private final PVector rotationOffset;
	
	private float mass;
	
	private final PVector maxRotation, minRotation;
	
	private BoundingBox boundingBox;
	
	private boolean gravityEffected;
	
	private int collisionGroup;
	private CollisionMode collisionMode;
	private Set<Entity> collisionIgnore;
	
	private Entity ground;

	private Entity attachedTo;
	private final Set<Entity> attachedEntities;
	private final Set<Entity> entitiesOnMe;
	
	enum CollisionMode {
		LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN;
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
		super(level, x, y, z);
		this.rotation = new PVector();
		
		if (level.is3D()) {
			this.boundingBox = new BoundingBox3D(this, width, height, depth);
		} else {
			this.boundingBox = new BoundingBox2D(this, width, height);
		}
		
		this.scale = new PVector(1, 1, 1);
		
		this.rotationOffset = new PVector();

		if (drawMode == PConstants.CENTER) {
			
		} else if (drawMode == PConstants.CORNER) {
			addLocation(width / 2, height / 2, depth / 2);
		} else {
			throw new InvalidParameterException("Only CENTER and CORNER draw modes are supported.");
		}
		
		this.attachedEntities = new HashSet<Entity>();
		this.entitiesOnMe = new HashSet<Entity>();
		this.collisionIgnore = new HashSet<Entity>();
		
		this.mass = 1;
		this.gravityEffected = false;
		this.collisionMode = CollisionMode.LESS_THAN_OR_EQUAL_TO;
		
		this.ground = null;
		
		this.maxRotation = new PVector(Float.NaN, Float.NaN, Float.NaN);
		this.minRotation = new PVector(Float.NaN, Float.NaN, Float.NaN);
	}

	/**
	 * Update the entity.
	 * This method calls {@link #update(float)}.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 */
	final void _update(float delta) {
		if (getLevel() == null) {
			return;
		}
		if (attachedTo != null) {
			return;
		}
		
		update(delta);
		
		if (getLevel() == null) {
			return;
		}
		
		applyMotionLimits();
		this.boundingBox.setLocation(getLocation());
		
		boolean allChildrenCanMove = true;
		for(Entity ent : attachedEntities){
			if(!ent.updateAttached(delta)) allChildrenCanMove = false;
		}
		
		if(allChildrenCanMove){
			PVector currentLocation = getLocation();
			PVector newLocation = getMoveToLocation(delta);
			
			if (!moveTry(newLocation, currentLocation)) {
				if (!moveTry(new PVector(newLocation.x, currentLocation.y, currentLocation.z), currentLocation)) {
					setVelocityX(0);
					setVelocityOffsetX(0);
				} else {
					currentLocation.x = newLocation.x;
				}
				if (!moveTry(new PVector(currentLocation.x, newLocation.y, currentLocation.z), currentLocation)) {
					setVelocityY(0);
					setVelocityOffsetY(0);
				} else {
					currentLocation.y = newLocation.y;
				}
				if (!moveTry(new PVector(currentLocation.x, currentLocation.y, newLocation.z), currentLocation)) {
					setVelocityZ(0);
					setVelocityOffsetZ(0);
				} else {
					currentLocation.z = newLocation.z;
				}
			}
		}
		
		applyLocationLimits();
		applyRotationLimits();
		groundDetection();
	}

	private final boolean updateAttached(float delta){
		assert(attachedTo == null);
		update(delta);
		
		for(Entity ent : attachedEntities){
			ent.updateAttached(delta);
		}
		
		setVelocity(0, 0, 0);
		setVelocityOffset(0, 0, 0);
		
		PVector newLocation = getMoveToLocation(delta);
		return getLevel().willCollideWithWhenMoved(this, newLocation) == null;
	}

	/**
	 * Try and move this entity to the given location.
	 * 
	 * @param newLocation
	 * @return whether or not the move was successful
	 */
	private boolean moveTry(PVector newLocation, PVector currentLocation) {
		Entity willCollideWith = getLevel().willCollideWithWhenMoved(this, newLocation);
		
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
		Level level = getLevel();
		setLocation(newLocation);
		for(Entity ent : attachedEntities){
			PVector nl = ent.getLocation();
			nl.add(dLocation);
			if(level.willCollideWithWhenMoved(ent, nl ) == null){
				ent.setLocation(nl);
			}
		}
		for(Entity ent : entitiesOnMe){
			PVector nl = ent.getLocation();
			nl.add(dLocation);
			if(level.willCollideWithWhenMoved(ent, nl ) == null){
				ent.setLocation(nl);
			}
		}
	}
	
	private boolean moveAndPush(PushableEntity pushee, PVector newLocation, PVector dLocation) {
		float resistance = pushee.getResistance();
		PVector pusheeDLocation = PVector.mult(dLocation, 1-resistance);
		PVector pusheeNewLocation = PVector.add(pushee.getLocation(), pusheeDLocation);
		
		if(getLevel().willCollideWithWhenMoved(pushee, pusheeNewLocation) != null) {
			return false;
		}

		dLocation.sub(pusheeDLocation);
		newLocation.sub(pusheeDLocation);
		
		((Entity)(pushee)).moveNow(pusheeNewLocation, pusheeDLocation);
		this.moveNow(newLocation, dLocation);
		
		setVelocity(getVelocity().mult(1 - resistance));
		setVelocityOffset(getVelocityOffset().mult(1 - resistance));	// TODO is this needed
		pushee.addVelocity(getVelocity());
		
		return true;
	}

	/**
	 * Move the entity.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 * @return The location that the entity will move to
	 */
	protected PVector getMoveToLocation(float delta){
		Level level = getLevel();
		boolean onGround = isOnGround();
		
		if(gravityEffected && !onGround){
			applyAcceleration(level.getGravity(), level.getAirFriction(), delta);
		}
		if(onGround){
			applyAcceleration(0, 0, 0, ground.getGroundFriction(), delta);
		}
		
		return super.getMoveToLocation(delta);
	}
	
	/**
	 * Draw the entity.
	 * This method calls {@link #draw(PGraphics)}.
	 * 
	 * @param g The graphics object to draw to
	 */
	final void _draw(PGraphics g) {
		Level level = getLevel();
		if (level == null) return;
		g.pushMatrix();
		if (level.is3D()) {
			g.translate(
					level.convertGridUnitsXToPixels(getX()),
					level.convertGridUnitsYToPixels(getY()),
					level.convertGridUnitsZToPixels(getZ()));
			g.rotateX(getRotation3DTilt());
			g.rotateY(getRotation3DPan());
			g.rotateZ(getRotation3DRoll());
			g.scale(getScaleX(), getScaleY(), getScaleZ());
		} else {
			g.translate(
					level.convertGridUnitsXToPixels(this.boundingBox.getCenterX() + getXOffset()),
					level.convertGridUnitsYToPixels(this.boundingBox.getCenterY() + getYOffset()));
			g.rotate(getRotation2D());
			g.scale(getScaleX(), getScaleY());
		}
		g.pushStyle();
		g.rectMode(PConstants.CENTER);
		draw(g);
		g.popStyle();
		g.popMatrix();
		
		if (level.isDrawingBoundingBoxes()) {
			drawBoundingBox(g);
		}
	}

	/**
	 * Draw a bounding box around the entity.
	 * 
	 * @param g The graphics object to draw to
	 */
	private void drawBoundingBox(PGraphics g) {
		if (getLevel().is3D()) {
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
		Level level = getLevel();
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
		ground = getLevel().getGround(this);
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
		return applyForce(new PVector(fx, fy, fz), friction, time);
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
		return applyAcceleration(PVector.div(f, mass), friction, time);
	}
	
	/**
	 * Apply the limits the rotation of this entity.
	 */
	protected void applyRotationLimits() {
		if (!Float.isNaN(minRotation.x)) rotation.x = Math.max(rotation.x - rotationOffset.x, minRotation.x);
		if (!Float.isNaN(minRotation.y)) rotation.y = Math.max(rotation.y - rotationOffset.y, minRotation.y);
		if (!Float.isNaN(minRotation.z)) rotation.z = Math.max(rotation.z - rotationOffset.z, minRotation.z);
		
		if (!Float.isNaN(maxRotation.x)) rotation.x = Math.min(rotation.x - rotationOffset.x, maxRotation.x);
		if (!Float.isNaN(maxRotation.y)) rotation.y = Math.min(rotation.y - rotationOffset.y, maxRotation.y);
		if (!Float.isNaN(maxRotation.z)) rotation.z = Math.min(rotation.z - rotationOffset.z, maxRotation.z);
	}

	/**
	 * Attach an entity to me.
	 * 
	 * @param entity
	 */
	public void attach(Entity entity){
		if(entity == this) throw new RuntimeException("Cannot attach an entity to itself.");
		entity.deattach(this);
		this.attachedEntities.add(entity);
		this.ignoreInCollisions(entity);
		entity.ignoreInCollisions(this);
		entity.attachedTo = this;
	}
	
	/**
	 * Deattach an entity to me.
	 * 
	 * @param entity
	 */
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
		entity.addVelocity(getVelocity());
	}
	
	/**
	 * Ignore the given entity during collision detection.
	 * 
	 * @param entity
	 */
	public void ignoreInCollisions(Entity entity){
		collisionIgnore.add(entity);
	}

	/**
	 * Stop ignoring the given entity during collision detection.
	 * 
	 * @param entity
	 */
	public void unignoreInCollisions(Entity entity){
		collisionIgnore.remove(entity);
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
		return getLevel().convertGridUnitsWidthToPixels(boundingBox.getWidth());
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
		return getLevel().convertGridUnitsHeightToPixels(boundingBox.getHeight());
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
		return getLevel().convertGridUnitsDepthToPixels(boundingBox.getDepth());
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
	 * Get the rotation of this entity.
	 * 
	 * @return the rotation
	 */
	public float getRotation2D(){
		return rotation.x + rotationOffset.x;
	}

	/**
	 * Get the rotation offset of this entity.
	 * 
	 * @return the rotation
	 */
	public float getRotation2DOffset(){
		return rotationOffset.x;
	}

	/**
	 * Get the rotation of this entity (tilt, pan, roll).
	 * 
	 * @return the rotation
	 */
	public PVector getRotation3D(){
		return PVector.add(rotation, rotationOffset);
	}

	/**
	 * Get the tilt rotation of this entity.
	 * 
	 * @return the tilt rotation
	 */
	public float getRotation3DTilt(){
		return rotation.x + rotationOffset.x;
	}

	/**
	 * Get the pan rotation of this entity.
	 * 
	 * @return the pan rotation
	 */
	public float getRotation3DPan(){
		return rotation.y + rotationOffset.y;
	}

	/**
	 * Get the roll rotation of this entity.
	 * 
	 * @return the roll rotation
	 */
	public float getRotation3DRoll(){
		return rotation.z + rotationOffset.z;
	}
	
	/**
	 * Get the rotation offset of this entity (tilt, pan, roll).
	 * 
	 * @return the rotation
	 */
	public PVector getRotation3DOffset(){
		return rotationOffset.copy();
	}

	/**
	 * Get the tilt rotation offset of this entity.
	 * 
	 * @return the tilt rotation
	 */
	public float getRotation3DTiltOffset(){
		return rotationOffset.x;
	}

	/**
	 * Get the pan rotation offset of this entity.
	 * 
	 * @return the pan rotation
	 */
	public float getRotation3DPanOffset(){
		return rotationOffset.y;
	}

	/**
	 * Get the roll rotation offset of this entity.
	 * 
	 * @return the roll rotation
	 */
	public float getRotation3DRollOffset(){
		return rotationOffset.z;
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
	public int getCollisionGroup() {
		return collisionGroup;
	}
	
	/**
	 * Get the collision mode that this entity uses.
	 * 
	 * @return
	 */
	public CollisionMode getCollisionMode() {
		return collisionMode;
	}
	
	/**
	 * Get a set of the entities this entity will explicitly ignore during collision detection.
	 * 
	 * @return
	 */
	public Set<Entity> getCollisionIgnoreEntities(){
		return Collections.unmodifiableSet(collisionIgnore);
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
		int oldGroup = collisionGroup;
		collisionGroup = group;
		getLevel().updateEntityCollisionGroup(this, oldGroup);
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
		if(!getLevel().is3D()) throw new UnsupportedOperationException("Cannot use a 3D bounding box in a 2D level.");
		this.boundingBox = boundingBox;
	}

	public void setBoundingBox2D(BoundingBox2D boundingBox) {
		if(boundingBox == null) throw new IllegalArgumentException("Cannot set an entity's bounding box to null.");
		if(getLevel().is3D()) throw new UnsupportedOperationException("Cannot use a 2D bounding box in a 3D level.");
		this.boundingBox = boundingBox;
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 2D games only)
	 * 
	 * @param rotation
	 */
	public void setRotation2D(float rotation){
		this.rotation.x = rotation;
	}

	/**
	 * Set the rotation offset of the entity.
	 * (For 2D games only)
	 * 
	 * @param rotation
	 */
	public void setRotation2DOffset(float rotation){
		this.rotationOffset.x = rotation;
	}

	/**
	 * Set the tilt rotation of the entity.
	 * This is the rotation in the xy plain.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 */
	public void setRotation3DTilt(float tilt){
		rotation.x = tilt;
	}

	/**
	 * Set the pan rotation of the entity.
	 * This is the rotation in the xz plain.
	 * (For 3D games only)
	 * 
	 * @param pan
	 */
	public void setRotation3DPan(float pan){
		rotation.y = pan;
	}
	
	/**
	 * Set the roll rotation of the entity.
	 * This is the rotation in the yz plain.
	 * (For 3D games only)
	 * 
	 * @param roll
	 */
	public void setRotation3DRoll(float roll){
		rotation.z = roll;
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 * @param pan
	 * @param roll
	 */
	public void setRotation3D(float tilt, float pan, float roll){
		rotation.set(tilt, pan, roll);
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 3D games only)
	 * 
	 * @param rotation
	 */
	public void setRotation3D(PVector rotation){
		this.rotation.set(rotation);
	}
	
	/**
	 * Set the tilt rotation offset of the entity.
	 * This is the rotation in the xy plain.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 */
	public void setRotation3DOffsetTilt(float tilt){
		rotationOffset.x = tilt;
	}

	/**
	 * Set the pan rotation offset of the entity.
	 * This is the rotation in the xz plain.
	 * (For 3D games only)
	 * 
	 * @param pan
	 */
	public void setRotation3DOffsetPan(float pan){
		rotationOffset.y = pan;
	}
	
	/**
	 * Set the roll rotation offset of the entity.
	 * This is the rotation in the yz plain.
	 * (For 3D games only)
	 * 
	 * @param roll
	 */
	public void setRotation3DOffsetRoll(float roll){
		rotationOffset.z = roll;
	}
	
	/**
	 * Set the rotation offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param tilt
	 * @param pan
	 * @param roll
	 */
	public void setRotation3DOffset(float tilt, float pan, float roll){
		rotationOffset.set(tilt, pan, roll);
	}
	
	/**
	 * Set the rotation offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param rotation
	 */
	public void setRotation3DOffset(PVector rotation){
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
	 * Limit the rotation of this entity.
	 * 
	 * @param minAng The minimum angle that this entity can be at
	 */
	public void limitRotation2DMin(float minAng) {
		this.minRotation.x = minAng;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param maxAng The maximum angle that this entity can be at
	 */
	public void limitRotation2DMax(float maxAng){
		this.maxRotation.x = maxAng;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param minTilt The minimum tilt that this entity can be at
	 */
	public void limitRotation3DTiltMin(float minTilt) {
		minRotation.x = minTilt;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param minPan The minimum pan that this entity can be at
	 */
	public void limitRotation3DPanMin(float minPan) {
		minRotation.y = minPan;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param minRoll The minimum roll that this entity can be at
	 */
	public void limitRotation3DRollMin(float minRoll) {
		minRotation.z = minRoll;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param maxTilt The maximum tilt that this entity can be at
	 */
	public void limitRotation3DTiltMax(float maxTilt) {
		maxRotation.x = maxTilt;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param maxPan The maximum pan that this entity can be at
	 */
	public void limitRotation3DPanMax(float maxPan) {
		maxRotation.y = maxPan;
	}
	
	/**
	 * Limit the rotation of this entity.
	 * 
	 * @param maxRoll The maximum roll that this entity can be at
	 */
	public void limitRotation3DRollMax(float maxRoll) {
		maxRotation.z = maxRoll;
	}
	
	/**
	 * Remove the limits applied to the min rotation of this entity.
	 */
	public void removeLimitRotation2DMin(){
		minRotation.x = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max rotation of this entity.
	 */
	public void removeLimitRotation2DMax(){
		maxRotation.x = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the rotation of this entity.
	 */
	public void removeLimitRotation2D(){
		removeLimitRotation2DMin();
		removeLimitRotation2DMax();
	}

	/**
	 * Remove the limits applied to the rotation of this entity.
	 */
	public void removeLimitRotation3D(){
		minRotation.set(Float.NaN, Float.NaN, Float.NaN);
		maxRotation.set(Float.NaN, Float.NaN, Float.NaN);
	}
	
	/**
	 * Remove the limits applied to the min tilt of this entity.
	 */
	public void removeLimitRotation3DTiltMin() {
		minRotation.x = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the min pan of this entity.
	 */
	public void removeLimitRotation3DPanMin() {
		minRotation.y = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the min roll of this entity.
	 */
	public void removeLimitRotation3DRollMin() {
		minRotation.z = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max tilt of this entity.
	 */
	public void removeLimitRotation3DTiltMax() {
		maxRotation.x = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max pan of this entity.
	 */
	public void removeLimitRotation3DPanMax() {
		maxRotation.y = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max roll of this entity.
	 */
	public void removeLimitRotation3DRollMax() {
		maxRotation.z = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the tilt of this entity.
	 */
	public void removeLimitRotation3DTilt() {
		removeLimitRotation3DTiltMin();
		removeLimitRotation3DTiltMax();
	}
	
	/**
	 * Remove the limits applied to the pan of this entity.
	 */
	public void removeLimitRotation3DPan() {
		removeLimitRotation3DPanMin();
		removeLimitRotation3DPanMax();
	}
	
	/**
	 * Remove the limits applied to the roll of this entity.
	 */
	public void removeLimitRotation3DRoll() {
		removeLimitRotation3DRollMin();
		removeLimitRotation3DRollMax();
	}
}