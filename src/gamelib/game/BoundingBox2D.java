package gamelib.game;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import processing.core.PVector;

public class BoundingBox2D extends BoundingBox {

	private Rectangle2D.Float box;
	
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
		return getBoxForTest(location).contains(other.box);
	}
	
	@Override
	public boolean contains(BoundingBox3D other, PVector location) {
		return false;
	}

	@Override
	public boolean intersects(BoundingBox2D other, PVector location) {
		return  getBoxForTest(location).intersects(other.box);
	}

	@Override
	public boolean intersects(BoundingBox3D other, PVector location) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get a new box at the given location.
	 * 
	 * @param location
	 * @return
	 */
	public Rectangle2D.Float getBoxForTest(PVector location) {
		float width = getWidth();
		float height = getHeight();
		Rectangle2D.Float colBox = new Rectangle2D.Float(location.x - width / 2 , location.y - height / 2, width, height);
		return colBox;
	}

	@Override
	public float getCenterX() {
		return (float) this.box.getX() + getWidth() / 2;
	}

	@Override
	public float getCenterY() {
		return (float) this.box.getY() + getHeight() / 2;
	}

	@Override
	public float getCenterZ(){
		return 0;
	}

	@Override
	public float getWidth() {
		return (float) this.box.getWidth();
	}

	@Override
	public float getHeight() {
		return (float) this.box.getHeight();
	}

	@Override
	public float getDepth() {
		return 0;
	}

	@Override
	public float getMinX() {
		return (float) (this.box.getX());
	}

	@Override
	public float getMinY() {
		return (float) (this.box.getY());
	}

	@Override
	public float getMinZ() {
		return 0;
	}

	@Override
	public float getMaxX() {
		return (float) (this.box.getX() + this.box.getWidth());
	}

	@Override
	public float getMaxY() {
		return (float) (this.box.getY() + this.box.getHeight());
	}

	@Override
	public float getMaxZ() {
		return 0;
	}

	@Override
	public void setWidth(float width) {
		float dx = (this.box.width - width) / 2;
		this.box = new Rectangle2D.Float(this.box.x + dx, this.box.y, width, this.box.height);
	}

	@Override
	public void setHeight(float height) {
		float dy = (this.box.height - height) / 2;
		this.box = new Rectangle2D.Float(this.box.x, this.box.y + dy, this.box.width, height);
	}

	@Override
	public void setDepth(float depth) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCenterX(float x) {
		this.box = new Rectangle2D.Float(x - this.box.width / 2, this.box.y, this.box.width, this.box.height);
	}

	@Override
	public void setCenterY(float y) {
		this.box = new Rectangle2D.Float(this.box.x, y - this.box.height / 2, this.box.width, this.box.height);
	}

	@Override
	public void setCenterZ(float z) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSize(PVector size) {
		float dx = (this.box.width - size.x) / 2;
		float dy = (this.box.height - size.y) / 2;
		this.box = new Rectangle2D.Float(this.box.x + dx, this.box.y + dy, size.x, size.y);
	}

	@Override
	public void setLocation(PVector location) {
		this.box = new Rectangle2D.Float(location.x - this.box.width / 2, location.y - this.box.height / 2, this.box.width, this.box.height);
	}

	@Override
	public void addX(float x) {
		this.box = new Rectangle2D.Float(this.box.x + x, this.box.y, this.box.width, this.box.height);
	}

	@Override
	public void addY(float y) {
		this.box = new Rectangle2D.Float(this.box.x, this.box.y + y, this.box.width, this.box.height);
	}

	@Override
	public void addZ(float z) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addLocation(PVector pVector) {
		this.box = new Rectangle2D.Float(this.box.x + pVector.y, this.box.y + pVector.y, this.box.width, this.box.height);
	}

	@Override
	public void setDimensions(PVector location, PVector size) {
		this.box = new Rectangle2D.Float(location.x - size.x / 2, location.y - size.y / 2, size.x, size.y);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + " " + getBox();
	}
	
	public String getBox() {
		return "(" + this.box.getX() + ", "  + this.box.getY() + ", "  + this.box.getWidth() + ", "  + this.box.getHeight() + ")";
	}
	
	public String getBoxInPixels() {
		Level level = entity.getLevel();
		return "(" + 
				level.convertGridUnitsXToPixels((float) this.box.getX()) + ", "  + 
				level.convertGridUnitsYToPixels((float) this.box.getY()) + ", "  + 
				level.convertGridUnitsWidthToPixels((float) this.box.getWidth()) + ", "  + 
				level.convertGridUnitsHeightToPixels((float) this.box.getHeight()) + ")";
	}
}
