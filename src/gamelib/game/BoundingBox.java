package gamelib.game;

import processing.core.PVector;

abstract class BoundingBox {

	protected final Entity entity;

	/**
	 * A BoundingBox must be attached to an entity.
	 * However, that entity does not need have this BoundingBox as its bounding box.
	 * @param entity The entity this will be attached to
	 */
	BoundingBox(Entity entity){
		if(entity == null) throw new IllegalArgumentException("A BoundingBox must be attached to an entity.");
		this.entity = entity;
	}
	
	public abstract boolean contains(PVector point);
	
	/**
	 * Test if the given BoundingBox is contained by this BoundingBox.
	 * 
	 * @param other
	 * @return
	 */
	boolean contains(BoundingBox other) {
		return contains(other, entity.getLocation());
	}
	
	/**
	 * Test if the given BoundingBox is contained by this BoundingBox when it is at the given location.
	 * 
	 * @param other
	 * @param location
	 * @return
	 */
	boolean contains(BoundingBox other, PVector location) {
		if(other instanceof BoundingBox2D){
			return contains((BoundingBox2D)other, location);
		}
		else if(other instanceof BoundingBox3D){
			return contains((BoundingBox3D)other, location);
		}
		return false;
	}
	
	/**
	 * Test if the given BoundingBox is contained by this BoundingBox when it is at the given location.
	 * 
	 * @param other
	 * @param location
	 * @return
	 */
	public abstract boolean contains(BoundingBox2D other, PVector location);

	/**
	 * Test if the given BoundingBox is contained by this BoundingBox when it is at the given location.
	 * 
	 * @param other
	 * @param location
	 * @return
	 */
	public abstract boolean contains(BoundingBox3D other, PVector location);
	
	/**
	 * Test if the given BoundingBox intersects this BoundingBox.
	 * 
	 * @param other
	 * @return
	 */
	boolean intersects(BoundingBox other) {
		return intersects(other, entity.getLocation());
	}
	
	/**
	 * Test if the given BoundingBox intersects this BoundingBox when it is at the given location.
	 * 
	 * @param other
	 * @param location
	 * @return
	 */
	boolean intersects(BoundingBox other, PVector location) {
		if(other instanceof BoundingBox2D){
			return intersects((BoundingBox2D)other, location);
		}
		else if(other instanceof BoundingBox3D){
			return intersects((BoundingBox3D)other, location);
		}
		return false;
	}

	/**
	 * Test if the given BoundingBox intersects this BoundingBox when it is at the given location.
	 * 
	 * @param other
	 * @param location
	 * @return
	 */
	public abstract boolean intersects(BoundingBox2D other, PVector location);

	/**
	 * Test if the given BoundingBox intersects this BoundingBox when it is at the given location.
	 * 
	 * @param other
	 * @param location
	 * @return
	 */
	public abstract boolean intersects(BoundingBox3D other, PVector location);
	
	public final void intersectsAny(){
		
	}

	public abstract float getWidth();

	public abstract float getHeight();

	public abstract float getDepth();

	public abstract float getCenterX();
	
	public abstract float getCenterY();
	
	public abstract float getCenterZ();

	public PVector getLocation() {
		return new PVector(getCenterX(), getCenterY(), getCenterZ());
	}

	public abstract float getMinX();
	
	public abstract float getMinY();
	
	public abstract float getMinZ();

	public abstract float getMaxX();
	
	public abstract float getMaxY();
	
	public abstract float getMaxZ();
	
	public abstract void setWidth(float width);

	public abstract void setHeight(float height);

	public abstract void setDepth(float depth);

	public abstract void setCenterX(float x);
	
	public abstract void setCenterY(float y);
	
	public abstract void setCenterZ(float z);
	
	public abstract void setSize(PVector size);
	
	public abstract void setLocation(PVector location);
	
	public abstract void setDimensions(PVector location, PVector size);

	public abstract void addX(float x);
	
	public abstract void addY(float y);
	
	public abstract void addZ(float z);
	
	public abstract void addLocation(PVector pVector);

	final Entity getEntity() {
		return entity;
	}
	
	public final boolean collidesWithSomething() {
		return entity.getLevel().collidesWithSomething(this);
	}
}
