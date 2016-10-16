package gamelib.game;

import gamelib.Updatable;
import processing.core.PVector;

abstract class GameObject implements Updatable {

	private final PVector location;
	private final PVector velocity;
	
	private final PVector locationOffset;
	private final PVector velocityOffset;
	
	private final PVector maxLocation, minLocation;
	private final PVector maxVelocity, minVelocity;
	private float maxHorizontalVelocity;
	
	private Level level;
	
	/**
	 * Create a 2D GameObject.
	 * 
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 */
	public GameObject(Level level, float x, float y) {
		this(level, x, y, 0);
	}
	
	/**
	 * Create a 3D GameObject.
	 * 
	 * @param level The level the entity will exist in
	 * @param x The x location
	 * @param y The y location
	 * @param z The z location
	 */
	public GameObject(Level level, float x, float y, float z) {
		setLevel(level);
		this.location = new PVector(x, y, z);
		this.velocity = new PVector();
		
		this.locationOffset = new PVector();
		this.velocityOffset = new PVector();
		
		this.maxLocation = new PVector(Float.NaN, Float.NaN, Float.NaN);
		this.minLocation = new PVector(Float.NaN, Float.NaN, Float.NaN);
		this.maxVelocity = new PVector(Float.NaN, Float.NaN, Float.NaN);
		this.minVelocity = new PVector(Float.NaN, Float.NaN, Float.NaN);
		this.maxHorizontalVelocity = Float.NaN;
	}
	
	/**
	 * Update the entity.
	 * This method calls {@link #update(float)}.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 */
	void _update(float delta) {
		if (level == null) {
			return;
		}
		
		update(delta);
		
		if (level == null) {
			return;
		}
		
		applyMotionLimits();
		setLocation(getMoveToLocation(delta));
		applyLocationLimits();
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
	
	/**
	 * Apply the limits the location of this entity.
	 */
	protected void applyLocationLimits() {
		if (!Float.isNaN(minLocation.x)) location.x = Math.max(getX(), minLocation.x);
		if (!Float.isNaN(minLocation.y)) location.y = Math.max(getY(), minLocation.y);
		if (!Float.isNaN(minLocation.z)) location.z = Math.max(getZ(), minLocation.z);
	
		if (!Float.isNaN(maxLocation.x)) location.x = Math.min(getX(), maxLocation.x);
		if (!Float.isNaN(maxLocation.y)) location.y = Math.min(getY(), maxLocation.y);
		if (!Float.isNaN(maxLocation.z)) location.z = Math.min(getZ(), maxLocation.z);
	}

	/**
	 * Apply the limits the motion of this entity.
	 */
	protected void applyMotionLimits() {
		if (!Float.isNaN(minVelocity.x)) velocity.x = Math.max(getVelocityX(), minVelocity.x);
		if (!Float.isNaN(minVelocity.y)) velocity.y = Math.max(getVelocityY(), minVelocity.y);
		if (!Float.isNaN(minVelocity.z)) velocity.z = Math.max(getVelocityZ(), minVelocity.z);
	
		if (!Float.isNaN(maxVelocity.x)) velocity.x = Math.min(getVelocityX(), maxVelocity.x);
		if (!Float.isNaN(maxVelocity.y)) velocity.y = Math.min(getVelocityZ(), maxVelocity.y);
		if (!Float.isNaN(maxVelocity.z)) velocity.z = Math.min(getVelocityY(), maxVelocity.z);
	
		if(!Float.isNaN(maxHorizontalVelocity)){
			PVector temp = getVelocity();
			temp.y =  0;
			if(temp.mag() > maxHorizontalVelocity){
				temp.normalize();
				temp.mult(maxHorizontalVelocity);
				velocity.x = temp.x;
				velocity.z = temp.z;
			}
		}
	}

	/**
	 * Get the location the entity wants to move to.
	 * 
	 * @param delta The amount of game time that has passed since the last frame
	 * @return The location that the entity wants move to
	 */
	protected PVector getMoveToLocation(float delta){
		return PVector.add(location, new PVector(
				(this.velocity.x + this.velocityOffset.x) * delta,
				(this.velocity.y + this.velocityOffset.y) * delta,
				(this.velocity.z + this.velocityOffset.z) * delta));
	}

	/**
	 * Get the level that this entity is apart of.
	 * 
	 * @return the level
	 */
	public final Level getLevel() {
		return level;
	}

	/**
	 * Get the x location of this entity (in grid units).
	 * 
	 * @return the x location
	 */
	public float getX() {
		return location.x + locationOffset.x;
	}

	/**
	 * Get the x location of this entity (in pixels).
	 * 
	 * @return the x location
	 */
	public float getXInPixels() {
		return level.convertGridUnitsXToPixels(getX());
	}

	/**
	 * Get the y location of this entity (in grid units).
	 * 
	 * @return the y location
	 */
	public float getY() {
		return location.y + locationOffset.y;
	}

	/**
	 * Get the y location of this entity (in pixels).
	 * 
	 * @return the y location
	 */
	public float getYInPixels() {
		return level.convertGridUnitsYToPixels(getY());
	}

	/**
	 * Get the z location of this entity (in grid units).
	 * 
	 * @return the z location
	 */
	public float getZ() {
		return location.z + locationOffset.z;
	}

	/**
	 * Get the z location of this entity (in pixels).
	 * 
	 * @return the z location
	 */
	public float getZInPixels() {
		return level.convertGridUnitsZToPixels(getZ());
	}

	/**
	 * Get the location of this entity (in grid units).
	 * 
	 * @return the location
	 */
	public PVector getLocation() {
		return PVector.add(location, locationOffset);
	}

	/**
	 * Get the location of this entity (in pixels).
	 * 
	 * @return the location
	 */
	public PVector getLocationInPixels() {
		return new PVector(getXInPixels(), getYInPixels(), getZInPixels());
	}
	
	/**
	 * Get the x location offset of this entity (in grid units).
	 * 
	 * @return the x location
	 */
	public float getXOffset() {
		return locationOffset.x;
	}

	/**
	 * Get the y location offset of this entity (in grid units).
	 * 
	 * @return the y location
	 */
	public float getYOffset() {
		return locationOffset.y;
	}

	/**
	 * Get the z location offset of this entity (in grid units).
	 * 
	 * @return the z location
	 */
	public float getZOffset() {
		return locationOffset.z;
	}

	/**
	 * Get the location offset of this entity (in grid units).
	 * 
	 * @return the location offset
	 */
	public PVector getLocationOffset() {
		return locationOffset.copy();
	}
	
	/**
	 * Get the x velocity of this entity.
	 * 
	 * @return the x velocity
	 */
	public float getVelocityX() {
		return velocity.x + velocityOffset.x;
	}

	/**
	 * Get the y velocity of this entity.
	 * 
	 * @return the y velocity
	 */
	public float getVelocityY() {
		return velocity.y + velocityOffset.y;
	}

	/**
	 * Get the z velocity of this entity.
	 * 
	 * @return the z velocity
	 */
	public float getVelocityZ(){
		return velocity.z + velocityOffset.z;
	}

	/**
	 * Get the velocity of this entity.
	 * 
	 * @return the velocity
	 */
	public PVector getVelocity(){
		return PVector.add(velocity, velocityOffset);
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
	 * Add to the x location of the entity.
	 * 
	 * @param x
	 */
	public void addX(float x){
		location.x += x;
	}
	
	/**
	 * Set the y location of the entity.
	 * 
	 * @param y
	 */
	public void addY(float y){
		location.y += y;
	}
	
	/**
	 * Add to the z location of the entity.
	 * (For 3D games only)
	 * 
	 * @param z
	 */
	public void addZ(float z){
		location.z += z;
	}
	
	/**
	 * Add to the location of the entity.
	 * (For 2D games only)
	 * 
	 * @param x
	 * @param y
	 */
	public void addLocation(float x, float y){
		location.add(x, y);
	}
	
	/**
	 * Add to the location of the entity.
	 * (For 3D games only)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void addLocation(float x, float y, float z){
		location.add(x, y, z);
	}

	/**
	 * Add to the location of the entity.
	 * 
	 * @param loc
	 */
	public void addLocation(PVector loc){
		location.add(loc);
	}

	/**
	 * Add to the x location offset of the entity.
	 * 
	 * @param x
	 */
	public void addXOffset(float x){
		locationOffset.x += x;
	}
	
	/**
	 * Add to the y location offset of the entity.
	 * 
	 * @param y
	 */
	public void addYOffset(float y){
		locationOffset.y += y;
	}
	
	/**
	 * Add to the z location offset of the entity.
	 * 
	 * (For 3D games only)
	 * @param z
	 */
	public void addZOffset(float z){
		locationOffset.z += z;
	}
	
	/**
	 * Add to the location offset of the entity.
	 * (For 2D games only)
	 * 
	 * @param x
	 * @param y
	 */
	public void addLocationOffset(float x, float y){
		locationOffset.add(x, y);
	}
	
	/**
	 * Add to the location offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void addLocationOffset(float x, float y, float z){
		locationOffset.add(x, y, z);
	}

	/**
	 * Add to the location offset of the entity.
	 * 
	 * @param loc
	 */
	public void addLocationOffset(PVector loc){
		locationOffset.add(loc);
	}
	
	/**
	 * Add to the x speed of the entity.
	 * 
	 * @param vx
	 */
	public void addVelocityX(float vx){
		velocity.x += vx;
	}
	
	/**
	 * Add to the y speed of the entity.
	 * 
	 * @param vy
	 */
	public void addVelocityY(float vy){
		velocity.y += vy;
	}
	
	/**
	 * Add to the z speed of the entity.
	 * (For 3D games only)
	 * 
	 * @param vz
	 */
	public void addVelocityZ(float vz){
		velocity.z += vz;
	}
	
	/**
	 * Add to the speed of the entity.
	 * (For 2D games only)
	 * 
	 * @param vx
	 * @param vy
	 */
	public void addVelocity(float vx, float vy){
		velocity.add(vx, vy);
	}
	
	/**
	 * Add to the speed of the entity.
	 * (For 3D games only)
	 * 
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void addVelocity(float vx, float vy, float vz){
		velocity.add(vx, vy, vz);
	}
	
	/**
	 * Add to the speed of the entity.
	 * 
	 * @param v
	 */
	public void addVelocity(PVector v){
		velocity.add(v);
	}
	
	/**
	 * Add to the x velocity offset of the entity.
	 * 
	 * @param vx
	 */
	public void addVelocityOffsetX(float vx){
		velocityOffset.x += vx;
	}
	
	/**
	 * Add to the y velocity offset of the entity.
	 * 
	 * @param vy
	 */
	public void addVelocityOffsetY(float vy){
		velocityOffset.y += vy;
	}
	
	/**
	 * Add to the z velocity offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param vz
	 */
	public void addVelocityOffsetZ(float vz){
		velocityOffset.z += vz;
	}
	
	/**
	 * Add to the velocity offset of the entity.
	 * (For 2D games only)
	 * 
	 * @param vx
	 * @param vy
	 */
	public void addVelocityOffset(float vx, float vy){
		velocityOffset.add(vx, vy, 0);
	}
	
	/**
	 * Add to the velocity offset of the entity.
	 * (For 3D games only)
	 * 
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void addVelocityOffset(float vx, float vy, float vz){
		velocityOffset.add(vx, vy, vz);
	}
	
	/**
	 * Add to the velocity offset of the entity.
	 * 
	 * @param v
	 */
	public void addVelocityOffset(PVector v){
		velocityOffset.add(v);
	}
	
	/**
	 * Set the level that this entity is in.
	 * 
	 * @param level
	 */
	void setLevel(Level level) {
		if (level == null) {
			remove();
		} else {
			this.level = level;
			level.addGameObject(this);
		}
	}

	/**
	 * Set the x location of the entity.
	 * 
	 * @param x
	 */
	public void setX(float x){
		location.x = x;
	}
	
	/**
	 * Set the y location of the entity.
	 * 
	 * @param y
	 */
	public void setY(float y){
		location.y = y;
	}
	
	/**
	 * Set the z location of the entity.
	 * (For 3D games only)
	 * 
	 * @param z
	 */
	public void setZ(float z){
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
		location.set(x, y, location.z);
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
	public void setXOffset(float x){
		locationOffset.x = x;
	}
	
	/**
	 * Set the y location offset of the entity.
	 * 
	 * @param y
	 */
	public void setYOffset(float y){
		locationOffset.y = y;
	}
	
	/**
	 * Set the z location offset of the entity.
	 * 
	 * (For 3D games only)
	 * @param z
	 */
	public void setZOffset(float z){
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
		locationOffset.set(x, y, locationOffset.z);
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
		velocity.set(vx, vy, velocity.z);
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
		velocityOffset.set(vx, vy, velocityOffset.z);
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
	 * Remove this entity from the level it is in.
	 */
	public void remove() {
		this.level.removeGameObject(this);
	}

	/**
	 * Set this entity's level to null.
	 * To be called from level.
	 */
	final void removeLevel() {
		level = null;
	}

	/**
	 * Limit the min x location this entity can be in.
	 * 
	 * @param minX The minimum x location this entity can be
	 */
	public void limitXMin(float minX) {
		minLocation.x = minX;
	}
	
	/**
	 * Limit the min y location this entity can be in.
	 * 
	 * @param minX The minimum y location this entity can be
	 */
	public void limitYMin(float minY) {
		minLocation.y = minY;
	}
	
	/**
	 * Limit the min z location this entity can be in.
	 * 
	 * @param minX The minimum z location this entity can be
	 */
	public void limitZMin(float minZ) {
		minLocation.z = minZ;
	}

	/**
	 * Limit the max x location this entity can be in.
	 * 
	 * @param maxX The maximum x location this entity can be
	 */
	public void limitXMax(float maxX) {
		maxLocation.x = maxX;
	}
	
	/**
	 * Limit the max y location this entity can be in.
	 * 
	 * @param maxY The maximum y location this entity can be
	 */
	public void limitYMax(float maxY) {
		maxLocation.y = maxY;
	}
	
	/**
	 * Limit the max z location this entity can be in.
	 * 
	 * @param maxZ The maximum z location this entity can be
	 */
	public void limitZMax(float maxZ) {
		maxLocation.z = maxZ;
	}
	
	/**
	 * Limit the min x velocity of this entity.
	 * 
	 * @param minX The minimum x velocity this entity can be
	 */
	public void limitVelocityXMin(float minX) {
		minVelocity.x = minX;
	}
	
	/**
	 * Limit the min y velocity of this entity.
	 * 
	 * @param minY The minimum y velocity this entity can be
	 */
	public void limitVelocityYMin(float minY) {
		minVelocity.y = minY;
	}
	
	/**
	 * Limit the min z velocity of this entity.
	 * 
	 * @param minZ The minimum z velocity this entity can be
	 */
	public void limitVelocityZMin(float minZ) {
		minVelocity.z = minZ;
	}
	
	/**
	 * Limit the max x velocity of this entity.
	 * 
	 * @param maxX The maximum x velocity this entity can be
	 */
	public void limitVelocityXMax(float maxX) {
		maxVelocity.x = maxX;
	}
	
	/**
	 * Limit the max y velocity of this entity.
	 * 
	 * @param maxY The maximum y velocity this entity can be
	 */
	public void limitVelocityYMax(float maxY) {
		maxVelocity.y = maxY;
	}
	
	/**
	 * Limit the max z velocity of this entity.
	 * 
	 * @param maxZ The maximum z velocity this entity can be
	 */
	public void limitVelocityZMax(float maxZ) {
		maxVelocity.z = maxZ;
	}
	
	public void limitVelocityHorizontal(float maxHorizontalVelocity){
		this.maxHorizontalVelocity = maxHorizontalVelocity;
	}
	
	/**
	 * Remove the limits applied to the location of this entity.
	 */
	public void removeLimitLocation(){
		minLocation.set(Float.NaN, Float.NaN, Float.NaN);
		maxLocation.set(Float.NaN, Float.NaN, Float.NaN);
	}
	
	/**
	 * Remove the limits applied to the x location of this entity.
	 */
	public void removeLimitX(){
		removeLimitXMin();
		removeLimitXMax();
	}

	/**
	 * Remove the limits applied to the y location of this entity.
	 */
	public void removeLimitY(){
		removeLimitYMin();
		removeLimitYMax();
	}

	/**
	 * Remove the limits applied to the z location of this entity.
	 */
	public void removeLimitZ(){
		removeLimitZMin();
		removeLimitZMax();
	}

	/**
	 * Remove the limits applied to the min x location of this entity.
	 */
	public void removeLimitXMin(){
		minLocation.x = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the min y location of this entity.
	 */
	public void removeLimitYMin(){
		minLocation.y = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the min z location of this entity.
	 */
	public void removeLimitZMin(){
		minLocation.z = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max x location of this entity.
	 */
	public void removeLimitXMax(){
		maxLocation.x = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max y location of this entity.
	 */
	public void removeLimitYMax(){
		maxLocation.y = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to the max z location of this entity.
	 */
	public void removeLimitZMax(){
		maxLocation.z = Float.NaN;
	}
	
	/**
	 * Remove the limits applied to this entity's velocity.
	 */
	public void removeLimitVelocity(){
		minVelocity.set(Float.NaN, Float.NaN, Float.NaN);
		maxVelocity.set(Float.NaN, Float.NaN, Float.NaN);
		maxHorizontalVelocity = Float.NaN;
	}

	/**
	 * Remove the limits applied to this entity's x velocity.
	 */
	public void removeLimitVelocityX(){
		removeLimitVelocityXMin();
		removeLimitVelocityXMax();
	}

	/**
	 * Remove the limits applied to this entity's y velocity.
	 */
	public void removeLimitVelocityY(){
		removeLimitVelocityYMin();
		removeLimitVelocityYMax();
	}

	/**
	 * Remove the limits applied to this entity's z velocity.
	 */
	public void removeLimitVelocityZ(){
		removeLimitVelocityZMin();
		removeLimitVelocityZMax();
	}

	/**
	 * Remove the limits applied to this entity's min x velocity.
	 */
	public void removeLimitVelocityXMin(){
		minVelocity.x =  Float.NaN;
	}

	/**
	 * Remove the limits applied to this entity's min y velocity.
	 */
	public void removeLimitVelocityYMin(){
		minVelocity.y =  Float.NaN;
	}

	/**
	 * Remove the limits applied to this entity's min z velocity.
	 */
	public void removeLimitVelocityZMin(){
		minVelocity.z =  Float.NaN;
	}

	/**
	 * Remove the limits applied to this entity's max x velocity.
	 */
	public void removeLimitVelocityXMax(){
		maxVelocity.x =  Float.NaN;
	}

	/**
	 * Remove the limits applied to this entity's max y velocity.
	 */
	public void removeLimitVelocityYMax(){
		maxVelocity.y =  Float.NaN;
	}

	/**
	 * Remove the limits applied to this entity's max z velocity.
	 */
	public void removeLimitVelocityZMax(){
		maxVelocity.z =  Float.NaN;
	}
	
	/**
	 * Remove the limits applied to this entity's horizontal velocity.
	 */
	public void removeLimitVelocityHorizontal(){
		this.maxHorizontalVelocity = Float.NaN;
	}
}
