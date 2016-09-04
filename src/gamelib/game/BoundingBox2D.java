package gamelib.game;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import processing.core.PVector;

public class BoundingBox2D extends BoundingBox {

	private Rectangle2D box;
	
	public BoundingBox2D(Entity ent, float width, float height){
		this(ent, -width/2, -height/2, width, height);
	}

	public BoundingBox2D(Entity ent, float x, float y, float width, float height){
		super(ent);
		this.box = new Rectangle2D.Float(x, y, width, height);
	}

	@Override
	public boolean contains(PVector point) {
		return box.contains(new Point2D.Float(point.x, point.y));
	}

	@Override
	public boolean contains(BoundingBox2D other, PVector location) {
		return box.contains(other.box);
	}
	
	@Override
	public boolean contains(BoundingBox3D other, PVector location) {
		return false;
	}

	@Override
	public boolean intersects(BoundingBox2D other, PVector location) {
		return box.intersects(other.box);
	}

	@Override
	public boolean intersects(BoundingBox3D other, PVector location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getCenterX() {
		return (float) box.getX();
	}

	@Override
	public float getCenterY() {
		return (float) box.getY();
	}

	@Override
	public float getCenterZ(){
		return 0;
	}

	@Override
	public float getWidth() {
		return (float) box.getWidth();
	}

	@Override
	public float getHeight() {
		return (float) box.getHeight();
	}

	@Override
	public float getDepth() {
		return 0;
	}

	@Override
	public float getMinX() {
		return (float) (box.getX() - box.getWidth() / 2);
	}

	@Override
	public float getMinY() {
		return (float) (box.getY() - box.getHeight() / 2);
	}

	@Override
	public float getMinZ() {
		return 0;
	}

	@Override
	public float getMaxX() {
		return (float) (box.getX() + box.getWidth() / 2);
	}

	@Override
	public float getMaxY() {
		return (float) (box.getY() + box.getHeight() / 2);
	}

	@Override
	public float getMaxZ() {
		return 0;
	}
}
