package gamelib.game;

import processing.core.PVector;

public class BoundingBox3D extends BoundingBox {
	
	private float width, height, depth;
	private float halfWidth, halfHeight, halfDepth;
	private PVector location, minLocation, maxLocation;

	public BoundingBox3D(Entity ent, float width, float height, float depth){
		this(ent, 0, 0, 0, width, height, depth);
	}

	public BoundingBox3D(Entity ent, float x, float y, float z, float width, float height, float depth){
		super(ent);
		this.halfWidth  = (this.width  = width)  / 2;
		this.halfHeight = (this.height = height) / 2;
		this.halfDepth  = (this.depth  = depth)  / 2;
		this.location = new PVector(x, y, z);
		this.minLocation = new PVector(x-halfWidth, y-halfHeight, z-halfDepth);
		this.maxLocation = new PVector(x+halfWidth, y+halfHeight, z+halfDepth);
	}

	@Override
	public boolean contains(PVector point) {
		PVector offset = entity.getLocation();
		return		point.x >= minLocation.x+offset.x && point.x <= maxLocation.x+offset.x
				&&	point.y >= minLocation.y+offset.y && point.y <= maxLocation.y+offset.y
				&&	point.z >= minLocation.z+offset.z && point.z <= maxLocation.z+offset.z;
	}

	@Override
	public boolean contains(BoundingBox2D other, PVector location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(BoundingBox3D other, PVector location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(BoundingBox2D other, PVector location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(BoundingBox3D other, PVector location) {
		PVector te = PVector.add(location, this.location);
		PVector oe = PVector.add(other.entity.getLocation(), other.location);
		
		return	((te.x - halfWidth  < oe.x + other.width /2 && te.x + halfWidth  > oe.x - other.width /2) || (te.x + halfWidth  > oe.x - other.width /2 && te.x + halfWidth  < oe.x + other.width /2 ))
			&&	((te.y - halfHeight < oe.y + other.height/2 && te.y + halfHeight > oe.y - other.height/2) || (te.y + halfHeight > oe.y - other.height/2 && te.y + halfHeight < oe.y + other.height/2 ))
			&&	((te.z - halfDepth  < oe.z + other.depth /2 && te.z + halfDepth  > oe.z - other.depth /2) || (te.z + halfDepth  > oe.z - other.depth /2 && te.z + halfDepth  < oe.z + other.depth /2 ));
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public float getDepth() {
		return depth;
	}

	@Override
	public float getCenterX() {
		return location.x;
	}

	@Override
	public float getCenterY() {
		return location.y;
	}

	@Override
	public float getCenterZ() {
		return location.z;
	}

	@Override
	public float getMinX() {
		return minLocation.x;
	}

	@Override
	public float getMinY() {
		return minLocation.y;
	}

	@Override
	public float getMinZ() {
		return minLocation.z;
	}

	@Override
	public float getMaxX() {
		return maxLocation.x;
	}

	@Override
	public float getMaxY() {
		return maxLocation.y;
	}

	@Override
	public float getMaxZ() {
		return maxLocation.z;
	}
}
