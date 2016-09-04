package gamelib.game;

import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Light {
	
	private PVector location;
	private PVector velocity;
	private PVector rotation;
	private PVector scale;
	
	private Level level;
	
	private int color;
	
	public Light(Level level, float x, float y, float z, int color){
		setLevel(level);
		
		location = new PVector(x, y, z);
		velocity = new PVector();
		rotation = new PVector();
		scale = new PVector(1, 1, 1);
		
		this.color = color;
	}
	
	public abstract void apply(PGraphics g);

	/**
	 * Set the level that this light is in.
	 * @param level
	 */
	private void setLevel(Level level){
		if(level == null){
			remove();
		}
		else{
			this.level = level;
			level.addLight(this);
		}
	}
	
	public void remove(){
		this.level.removeLight(this);
	}

	final void removeLevel() {
		level = null;
	}
	
	public int getColor(){
		return color;
	}
	
	public int getRed(){
		return (color & 0x00FF0000) >> 16;
	}
	
	public int getGreen(){
		return (color & 0x0000FF00) >> 8;
	}
	
	public int getBlue(){
		return (color & 0x000000FF);
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	/**
	 * Get the level that this light is in.
	 * @return the level
	 */
	public final Level getLevel(){
		return level;
	}

	/**
	 * Get the x location of this light.
	 * @return the x location
	 */
	public float getX(){
		return location.x;
	}

	/**
	 * Get the y location of this light.
	 * @return the y location
	 */
	public float getY(){
		return location.y;
	}

	/**
	 * Get the z location of this light.
	 * @return the z location
	 */
	public float getZ(){
		return location.z;
	}

	/**
	 * Get the location of this light.
	 * @return the location
	 */
	public PVector getLocation(){
		return location.copy();
	}

	/**
	 * Get the x velocity of this light.
	 * @return the x velocity
	 */
	public float getVelocityX(){
		return velocity.x;
	}

	/**
	 * Get the y velocity of this light.
	 * @return the y velocity
	 */
	public float getVelocityY(){
		return velocity.y;
	}

	/**
	 * Get the z velocity of this light.
	 * @return the z velocity
	 */
	public float getVelocityZ(){
		return velocity.z;
	}

	/**
	 * Get the velocity of this light.
	 * @return the velocity
	 */
	public PVector getVelocity(){
		return velocity.copy();
	}

	/**
	 * Get the tilt rotation of this light.
	 * @return the tilt rotation
	 */
	public float getRotationTilt(){
		return rotation.x;
	}

	/**
	 * Get the pan rotation of this light.
	 * @return the pan rotation
	 */
	public float getRotationPan(){
		return rotation.y;
	}

	/**
	 * Get the roll rotation of this light.
	 * @return the roll rotation
	 */
	public float getRotationRoll(){
		return rotation.z;
	}
	
	/**
	 * Get the rotation of this light.
	 * @return the rotation
	 */
	public float getRotation2D(){
		return rotation.x;
	}
	
	/**
	 * Get the rotation of this light (tilt, pan, roll).
	 * @return the rotation
	 */
	public PVector getRotation3D(){
		return rotation.copy();
	}

	/**
	 * Get the x scale of this light.
	 * @return The x scale
	 */
	public float getScaleX(){
		return scale.x;
	}

	/**
	 * Get the x scale of this light.
	 * @return The x scale
	 */
	public float getScaleY(){
		return scale.y;
	}
	
	/**
	 * Get the z scale of this light.
	 * @return The z scale
	 */
	public float getScaleZ(){
		return scale.z;
	}
	
	/**
	 * Get the scale of this light.
	 * @return The scale
	 */
	public PVector getScale(){
		return scale.copy();
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
}
