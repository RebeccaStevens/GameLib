package gamelib.game;

import gamelib.Time;
import gamelib.game.entities.PushableEntity;

import java.util.HashSet;
import java.util.Set;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Entity {
	
	private PVector location;
	private PVector velocity;
	private PVector rotation;
	private PVector scale;
	
	private float mass;
	
	private PVector maxLocation, minLocation;
	private PVector maxVelocity, minVelocity;
	private PVector maxRotation, minRotation;
	private float maxHorizontalVelocity;
	
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
	
	enum CollisionMode{
		GREATER_THAN_OR_EQUAL_TO, EQUAL_TO, LESS_THAN;
	}

	/**
	 * Create a 2D Entity
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 * @param width The width
	 * @param height The height
	 */
	public Entity(Level level, float x, float y, float width, float height){
		this(level, x, y, 0, width, 1, height);
	}
	
	/**
	 * Create a 2D Entity
	 * @param level The level the entity will exist in
	 * @param location The location of this entity
	 * @param width The width
	 * @param height The height
	 */
	public Entity(Level level, PVector location, float width, float height){
		this(level, location.x, location.y, 0, width, 1, height);
	}
	
	/**
	 * Create a 3D Entity
	 * @param level The level the entity will exist in
	 * @param location The location of this entity
	 * @param width The width
	 * @param height The height
	 * @param depth The depth
	 */
	public Entity(Level level, PVector location, float width, float height, float depth){
		this(level, location.x, location.y, location.z, width, height, depth);
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
	public Entity(Level level, float x, float y, float z, float width, float height, float depth){
		setLevel(level);
		location = new PVector(x, y, z);
		velocity = new PVector();
		rotation = new PVector();
		if(level.is3D()){
			boundingBox = new BoundingBox3D(this, width, height, depth);
		}
		else{
			boundingBox = new BoundingBox2D(this, width, height);
		}
		scale = new PVector(1, 1, 1);
		
		attachedEntities = new HashSet<Entity>();
		entitiesOnMe = new HashSet<Entity>();
		collisionIgnore = new HashSet<Entity>();
		
		mass = 1;
		gravityEffected = false;
		collisionGroup = 1;
		collisionMode = CollisionMode.GREATER_THAN_OR_EQUAL_TO;
		
		ground = null;
		
		maxLocation = null;	minLocation = null;
		maxVelocity = null;	minVelocity = null;
		maxRotation = null;	minRotation = null;
	}

	/**
	 * Set the level that this entity is in.
	 * @param level
	 */
	private void setLevel(Level level){
		if(level == null){
			remove();
		}
		else{
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
	 * This method calls the method: update(float delta)
	 * @param delta The amount of game time that has passed since the last frame
	 */
	final void _update(float delta){
		if(this.level == null) return;
		if(attachedTo != null) return;
		update(delta);
		if(this.level == null) return;
		applyMotionLimits();
		
		boolean allChildrenCanMove = true;
		for(Entity ent : attachedEntities){
			if(!ent.updateAttached(delta)) allChildrenCanMove = false;
		}

		PVector currentLocation = getLocation();
		PVector newLocation = getMoveToLocation(delta);
		
		if(allChildrenCanMove){
			if(!move(newLocation, currentLocation)){
				if(!move(new PVector(newLocation.x, currentLocation.y, currentLocation.z), currentLocation)){ velocity.x = 0; }
				if(!move(new PVector(currentLocation.x, currentLocation.y, newLocation.z), currentLocation)){ velocity.z = 0; }
				if(!move(new PVector(currentLocation.x, newLocation.y, currentLocation.z), currentLocation)){ velocity.y = 0; }
			}
		}
		
		applyLocationLimits();
		groundDetection();
	}

	/**
	 * Update the entity.
	 * @param delta The amount of game time that has passed since the last frame
	 */
	public abstract void update(float delta);

	private final boolean updateAttached(float delta){
		assert(attachedTo == null);
		update(delta);
		
		for(Entity ent : attachedEntities){
			ent.updateAttached(delta);
		}
		
		velocity.mult(0);
		
		PVector newLocation = getMoveToLocation(delta);
		return level.canMove(this, newLocation) == null;
	}

	/**
	 * Try and move this entity to the given location
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
		((Entity)(pushee)).velocity.add(this.velocity);
		
		return true;
	}

	/**
	 * Move the entity.
	 * @param delta The amount of game time that has passed since the last frame
	 * @return The location that the entity will move to
	 */
	protected PVector getMoveToLocation(float delta){
		boolean onGround = isOnGround();
		
		if(gravityEffected && !onGround){
			applyAcceleration(level.getGravity(), level.getAirFriction());
		}
		if(onGround){
			applyAcceleration(0, 0, 0, ground.getGroundFriction());
		}
		
		return PVector.add(location, PVector.mult(velocity, (float) delta));
	}
	
	/**
	 * Draw the entity.
	 * This method calls the method: draw(delta)
	 * @param g The graphics object to draw to
	 */
	final void draw(PGraphics g){
		if(this.level == null) return;
		g.pushMatrix();
		if(level.is3D()){
			g.translate(location.x, location.y, location.z);
			g.rotateX(rotation.x);
			g.rotateY(rotation.y);
			g.rotateZ(rotation.z);
			g.scale(scale.x, scale.y, scale.z);
		}
		else{
			g.translate(location.x, location.y);
			g.rotate(rotation.x);
			g.scale(scale.x, scale.y);
		}
		g.pushStyle();
		g.noStroke();
		draw(g, Time.getTimeStep());
		g.popStyle();
		g.pushStyle();
		if(level.isDrawBoundingBoxes()){
			drawBoundingBox(g);
		}
		g.popStyle();
		g.popMatrix();
	}
	
	/**
	 * Draw the entity.
	 * @param delta The amount of game time that has passed since the last frame
	 */
	public abstract void draw(PGraphics g, float delta);

	/**
	 * Draw a bounding box around the entity.
	 * @param g The graphics object to draw to
	 */
	private void drawBoundingBox(PGraphics g) {
		if(level.is3D()){
			drawBoundingBox3D(g);
		}
		else{
			drawBoundingBox2D(g);
		}
	}
	
	/**
	 * Draw a 2D bounding box around the entity.
	 * @param g The graphics object to draw to
	 */
	private void drawBoundingBox2D(PGraphics g) {
		float bbx = boundingBox.getCenterX();
		float bby = boundingBox.getCenterY();
		float bbw = boundingBox.getWidth();
		float bbh = boundingBox.getHeight();
		
		g.stroke(0xFFFF0000);
		g.strokeWeight(bbw > 10 && bbh > 10 ? 3F : 1F);
		g.noFill();
		g.rectMode(PApplet.CORNER);
		g.rect(bbx, bby, bbw, bbh);
	}
	
	/**
	 * Draw a 3D bounding box around the entity.
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
	 * @param fx X force
	 * @param fy Y force
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled be the entity
	 */
	public PVector applyForce(float fx, float fy, float friction){
		return applyForce(fx, fy, 0, friction);
	}
	
	/**
	 * Apply a force on the entity.
	 * @param fx X force
	 * @param fy Y force
	 * @param fz Z force
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled be the entity
	 */
	public PVector applyForce(float fx, float fy, float fz, float friction){
		return applyForce(new PVector(fx/mass, fy/mass, fz/mass), friction);
	}
	
	/**
	 * Apply a force on the entity.
	 * @param f The force to apply
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled be the entity
	 */
	public PVector applyForce(PVector f, float friction){
		return accelerate(velocity, PVector.div(f, mass), friction);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * @param ax X acceleration
	 * @param ay Y acceleration
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(float ax, float ay, float friction){
		return applyAcceleration(ax, ay, 0, friction);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * @param ax X acceleration
	 * @param ay Y acceleration
	 * @param az Z acceleration
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(float ax, float ay, float az, float friction){
		return applyAcceleration(new PVector(ax, ay, az), friction);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * No friction will be applied to the acceleration.
	 * @param a The acceleration to apply
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(PVector a){
		return applyAcceleration(a, 0);
	}
	
	/**
	 * Apply an acceleration to the entity.
	 * @param a The acceleration to apply
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled be the entity
	 */
	public PVector applyAcceleration(PVector a, float friction){
		return accelerate(velocity, a, friction);
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
	 * @param veloc The velocity to accelerate
	 * @param accel The amount if acceleration
	 * @param friction The amount of friction to apply (should be between 0 and 1 though can be greater)
	 * @return The distance traveled
	 */
	public static PVector accelerate(PVector velocity, PVector acceleration, float friction){
		PVector delta;
		float time_step = Time.getTimeStep();

		  if (friction == 0) {
		    delta = PVector.add(PVector.mult(velocity, (float) time_step), PVector.mult(acceleration, (float) (0.5F * time_step * time_step)));
		    velocity.add(PVector.mult(acceleration, (float) time_step));
		  } 
		  else {
		    delta = PVector.add(PVector.mult(acceleration, (float) (time_step / friction)), PVector.mult(PVector.sub(velocity, PVector.div(acceleration, friction)), (float) ((1 - Math.exp(-friction * time_step)) / friction)));
		    velocity.set(PVector.add(PVector.div(acceleration, friction), PVector.mult(PVector.sub(velocity, PVector.div(acceleration, friction)), (float) Math.exp(-friction * time_step))));
		  }
		  
		  return delta;
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
	 * Put on to me
	 * @param entity
	 */
	private void putOn(Entity entity){
		assert(entity != this);
		entity.entitiesOnMe.remove(this);
		this.entitiesOnMe.add(entity);
	}
	
	/**
	 * Take off of me
	 * @param entity
	 */
	private void takeOff(Entity entity){
		this.entitiesOnMe.remove(entity);
		entity.velocity.add(this.velocity);
	}
	
	/**
	 * Get the level that this entity is in.
	 * @return the level
	 */
	public final Level getLevel(){
		return level;
	}

	/**
	 * Get the x location of this entity.
	 * @return the x location
	 */
	public float getX(){
		return location.x;
	}

	/**
	 * Get the y location of this entity.
	 * @return the y location
	 */
	public float getY(){
		return location.y;
	}

	/**
	 * Get the z location of this entity.
	 * @return the z location
	 */
	public float getZ(){
		return location.z;
	}

	/**
	 * Get the location of this entity.
	 * @return the location
	 */
	public PVector getLocation(){
		return location.copy();
	}

	/**
	 * Get the width of this entity.
	 * @return the width
	 */
	public float getWidth() {
		return boundingBox.getWidth();
	}

	/**
	 * Get the height of this entity.
	 * @return the height
	 */
	public float getHeight() {
		return boundingBox.getHeight();
	}

	/**
	 * Get the depth of this entity.
	 * @return the depth
	 */
	public float getDepth(){
		return boundingBox.getDepth();
	}

	/**
	 * Get the x velocity of this entity.
	 * @return the x velocity
	 */
	public float getVelocityX(){
		return velocity.x;
	}

	/**
	 * Get the y velocity of this entity.
	 * @return the y velocity
	 */
	public float getVelocityY(){
		return velocity.y;
	}

	/**
	 * Get the z velocity of this entity.
	 * @return the z velocity
	 */
	public float getVelocityZ(){
		return velocity.z;
	}

	/**
	 * Get the velocity of this entity.
	 * @return the velocity
	 */
	public PVector getVelocity(){
		return velocity.copy();
	}

	/**
	 * Get the tilt rotation of this entity.
	 * @return the tilt rotation
	 */
	public float getRotationTilt(){
		return rotation.x;
	}

	/**
	 * Get the pan rotation of this entity.
	 * @return the pan rotation
	 */
	public float getRotationPan(){
		return rotation.y;
	}

	/**
	 * Get the roll rotation of this entity.
	 * @return the roll rotation
	 */
	public float getRotationRoll(){
		return rotation.z;
	}
	
	/**
	 * Get the rotation of this entity.
	 * @return the rotation
	 */
	public float getRotation2D(){
		return rotation.x;
	}
	
	/**
	 * Get the rotation of this entity (tilt, pan, roll).
	 * @return the rotation
	 */
	public PVector getRotation3D(){
		return rotation.copy();
	}

	/**
	 * Get the x scale of this entity.
	 * @return The x scale
	 */
	public float getScaleX(){
		return scale.x;
	}

	/**
	 * Get the x scale of this entity.
	 * @return The x scale
	 */
	public float getScaleY(){
		return scale.y;
	}
	
	/**
	 * Get the z scale of this entity.
	 * @return The z scale
	 */
	public float getScaleZ(){
		return scale.z;
	}
	
	/**
	 * Get the scale of this entity.
	 * @return The scale
	 */
	public PVector getScale(){
		return scale.copy();
	}
	
	/**
	 * Get the mass of this entity.
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
	 * @return
	 */
	BoundingBox getBoundingBox() {
		return boundingBox;
	}
	
	/**
	 * Get the bounding box used by this entity.
	 * @return
	 */
	public BoundingBox3D getBoundingBox3D() {
		if(!(boundingBox instanceof BoundingBox3D)) throw new RuntimeException("Cannot get a 3D bounding for a 2D entity.");
		return (BoundingBox3D) boundingBox;
	}
	
	/**
	 * Get the bounding box used by this entity.
	 * @return
	 */
	public BoundingBox2D getBoundingBox2D() {
		if(!(boundingBox instanceof BoundingBox2D)) throw new RuntimeException("Cannot get a 2D bounding for a 3D entity.");
		return (BoundingBox2D) boundingBox;
	}
	
	/**
	 * Get the collision group of this entity.
	 * @return
	 */
	int getCollisionGroup() {
		return collisionGroup;
	}
	
	/**
	 * Get the collision mode that this entity uses.
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
	 * @return
	 */
	public boolean isOnGround() {
		return ground != null;
	}

	/**
	 * Set the x location of the entity.
	 * @param x
	 */
	public void setLocationX(float x){
		location.x = x;
	}
	
	/**
	 * Set the y location of the entity.
	 * @param y
	 */
	public void setLocationY(float y){
		location.y = y;
	}
	
	/**
	 * Set the z location of the entity.
	 * (For 3D games only)
	 * @param z
	 */
	public void setLocationZ(float z){
		location.z = z;
	}
	
	/**
	 * Set the location of the entity.
	 * (For 2D games only)
	 * @param x
	 * @param y
	 */
	public void setLocation(float x, float y){
		location.set(x, y, 0);
	}
	
	/**
	 * Set the location of the entity.
	 * (For 3D games only)
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setLocation(float x, float y, float z){
		location.set(x, y, z);
	}

	/**
	 * Set the location of the entity.
	 * @param loc
	 */
	public void setLocation(PVector loc){
		location.set(loc);
	}
	
	/**
	 * Set the x speed of the entity.
	 * @param vx
	 */
	public void setVelocityX(float vx){
		velocity.x = vx;
	}
	
	/**
	 * Set the y speed of the entity.
	 * @param vy
	 */
	public void setVelocityY(float vy){
		velocity.y = vy;
	}
	
	/**
	 * Set the z speed of the entity.
	 * (For 3D games only)
	 * @param vz
	 */
	public void setVelocityZ(float vz){
		velocity.z = vz;
	}
	
	/**
	 * Set the speed of the entity.
	 * (For 2D games only)
	 * @param vx
	 * @param vy
	 */
	public void setVelocity(float vx, float vy){
		velocity.set(vx, vy, 0);
	}
	
	/**
	 * Set the speed of the entity.
	 * (For 3D games only)
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void setVelocity(float vx, float vy, float vz){
		velocity.set(vx, vy, vz);
	}
	
	/**
	 * Set the speed of the entity.
	 * @param v
	 */
	public void setVelocity(PVector v){
		velocity.set(v);
	}
	
	/**
	 * Set the tilt rotation of the entity.
	 * This is the rotation in the xy plain.
	 * (For 3D games only)
	 * @param tilt
	 */
	public void setRotationTilt(float tilt){
		rotation.x = tilt;
	}

	/**
	 * Set the pan rotation of the entity.
	 * This is the rotation in the xz plain.
	 * (For 3D games only)
	 * @param pan
	 */
	public void setRotationPan(float pan){
		rotation.y = pan;
	}
	
	/**
	 * Set the roll rotation of the entity.
	 * This is the rotation in the yz plain.
	 * (For 3D games only)
	 * @param roll
	 */
	public void setRotationRoll(float roll){
		rotation.z = roll;
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 2D games only)
	 * @param rotation
	 */
	public void setRotation(float rotation){
		this.rotation.x = rotation;
	}
	
	/**
	 * Set the rotation of the entity.
	 * (For 3D games only)
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
	 * @param rotation
	 */
	public void setRotation(PVector rotation){
		this.rotation.set(rotation);
	}
	
	/**
	 * Set the scale of the entity uniformly to the given value
	 * @param s The new scale of the entity
	 */
	public void setScale(float s){
		scale.set(s, s, s);
	}
	
	/**
	 * Set the scale of the entity.
	 * @param x The x scale of the entity
	 * @param y The y scale of the entity
	 */
	public void setScale(float x, float y){
		scale.set(x, y, scale.z);
	}
	
	/**
	 * Set the scale of the entity.
	 * @param x The x scale of the entity
	 * @param y The y scale of the entity
	 * @param z The z scale of the entity
	 */
	public void setScale(float x, float y, float z){
		scale.set(x, y, z);
	}
	
	/**
	 * Set the mass of the entity.
	 * The mass must be greater than 0.
	 * @param mass The mass
	 */
	public void setMass(float mass) {
		if(mass<=0) throw new RuntimeException("Can not set mass to zero or a negitive value.");
		this.mass = mass;
	}
	
	/**
	 * Set whether or not this entity is effected by gravity.
	 * @param b
	 */
	public void setGravityEffected(boolean b){
		gravityEffected = b;
	}
	
	/**
	 * Set the collision group that this entity is apart of.
	 * The group number cannot be negative. If group value equals 0 then the entity will not collide with any thing.
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
	 * @param minX The minimum x location this entity can be
	 * @param minY The minimum y location this entity can be
	 */
	public void limitLocationMin(float minX, float minY){
		limitLocationMin(new PVector(minX, minY));
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension less than the give values.
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
	 * @param maxX The minimum x location this entity can be
	 * @param maxY The minimum y location this entity can be
	 */
	public void limitLocationMax(float maxX, float maxY){
		limitLocationMax(new PVector(maxX, maxY));
	}
	
	/**
	 * Limit the location this entity can be in.
	 * This entity will not be at location in any dimension greater than the give values.
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
	 * @param minX The minimum x velocity of this entity
	 * @param minY The minimum y velocity of this entity
	 */
	public void limitVelocityMin(float minX, float minY){
		limitVelocityMin(new PVector(minX, minY));
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go slower than the given velocity.
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
	 * @param maxX The minimum x velocity of this entity
	 * @param maxY The minimum y velocity of this entity
	 */
	public void limitVelocityMax(float maxX, float maxY){
		limitVelocityMax(new PVector(maxX, maxY));
	}
	
	/**
	 * Limit the velocity of this entity.
	 * This entity will not go faster than the given velocity.
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
	 * @param ang The minimum angle that this entity can be at
	 */
	public void limitRotationMin(float ang){
		limitRotationMin(new PVector(ang, 0, 0));
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation less than any of the give values.
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
	 * @param ang The maximum angle that this entity can be at
	 */
	public void limitRotationMax(float ang){
		limitRotationMax(new PVector(ang, 0, 0));
	}
	
	/**
	 * Limit the rotation of this entity.
	 * This entity will not have a rotation greater than any of the give values.
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
			location.x = Math.max(location.x, minLocation.x);
			location.y = Math.max(location.y, minLocation.y);
			location.z = Math.max(location.z, minLocation.z);
		}
		if(maxLocation != null){
			location.x = Math.min(location.x, maxLocation.x);
			location.y = Math.min(location.y, maxLocation.y);
			location.z = Math.min(location.z, maxLocation.z);
		}
		if(minRotation != null){
			rotation.x = Math.max(rotation.x, minRotation.x);
			rotation.y = Math.max(rotation.y, minRotation.y);
			rotation.z = Math.max(rotation.z, minRotation.z);
		}
		if(maxRotation != null){
			rotation.x = Math.min(rotation.x, maxRotation.x);
			rotation.y = Math.min(rotation.y, maxRotation.y);
			rotation.z = Math.min(rotation.z, maxRotation.z);
		}
	}
	
	/**
	 * Apply the limits the motion of this entity.
	 */
	private void applyMotionLimits() {
		if(minVelocity != null){
			velocity.x = Math.max(velocity.x, minVelocity.x);
			velocity.y = Math.max(velocity.y, minVelocity.y);
			velocity.z = Math.max(velocity.z, minVelocity.z);
		}
		if(maxVelocity != null){
			velocity.x = Math.min(velocity.x, maxVelocity.x);
			velocity.y = Math.min(velocity.y, maxVelocity.y);
			velocity.z = Math.min(velocity.z, maxVelocity.z);
		}
		if(!Float.isNaN(maxHorizontalVelocity)){
			PVector temp = velocity.copy();
			temp.y = 0;
			if(temp.mag() > maxHorizontalVelocity){
				temp.normalize();
				temp.mult(maxHorizontalVelocity);
				velocity.x = temp.x;
				velocity.z = temp.z;
			}
		}
	}
}