package gamelib.game;

import gamelib.GameManager;
import processing.core.PGraphics;
import processing.core.PVector;


public abstract class Camera extends GameObject {
	
	protected final PVector rotation;
	
	private final PVector maxAbsoluteLocation, minAbsoluteLocation;

	public Camera(Level level) {
		super(level, 0, 0, 0);
		rotation = new PVector(0, 0, 0);
		maxAbsoluteLocation = new PVector(Float.NaN, Float.NaN);
		minAbsoluteLocation = new PVector(Float.NaN, Float.NaN);
	}

	void apply(PGraphics g) {
		if (getLevel().is3D()){
			apply3D(g);
		} else {
			apply2D(g);
		}
	}
	
	private void apply3D(PGraphics g) {
		g.translate(
				Math.min(Math.max(-getXInPixels() + g.width / 2, minAbsoluteLocation.x), maxAbsoluteLocation.x),
				Math.min(Math.max(-getYInPixels() + g.height / 2, minAbsoluteLocation.y), maxAbsoluteLocation.y),
				-getZInPixels());
		g.rotateX(-rotation.x);
		g.rotateY(-rotation.y);
		g.rotateZ(-rotation.z);
	}

	private void apply2D(PGraphics g) {
		g.rotate(rotation.x);
		g.translate(-getXInPixels() + g.width / 2, -getYInPixels() + g.height / 2);
	}
	
	/**
	 * Limit the min x location this camera can be in (in pixels).
	 * 
	 * @param minX The minimum x location this camera can be
	 */
	public void limitAbsoluteXMin(float minX) {
		minAbsoluteLocation.x = minX;
	}
	
	/**
	 * Limit the min y location this camera can be in (in pixels).
	 * 
	 * @param minX The minimum y location this camera can be
	 */
	public void limitAbsoluteYMin(float minY) {
		minAbsoluteLocation.y = minY;
	}
	
	/**
	 * Limit the max x location this camera can be in (in pixels).
	 * 
	 * @param maxX The maximum x location this camera can be
	 */
	public void limitAbsoluteXMax(float maxX) {
		maxAbsoluteLocation.x = maxX;
	}
	
	/**
	 * Limit the max y location this camera can be in (in pixels).
	 * 
	 * @param maxY The maximum y location this camera can be
	 */
	public void limitAbsoluteYMax(float maxY) {
		maxAbsoluteLocation.y = maxY;
	}
	
	/**
	 * Remove the absolute limits applied to the location of this camera.
	 */
	public void removeLimitAbsoluteLocation(){
		minAbsoluteLocation.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		maxAbsoluteLocation.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	}
	
	/**
	 * Remove the absolute limits applied to the min x location of this camera.
	 */
	public void removeLimitAbsoluteXMin(){
		minAbsoluteLocation.x = Float.NaN;
	}
	
	/**
	 * Remove the absolute limits applied to the min y location of this camera.
	 */
	public void removeLimitAbsoluteYMin(){
		minAbsoluteLocation.y = Float.NaN;
	}
	
	/**
	 * Remove the absolute limits applied to the max x location of this camera.
	 */
	public void removeLimitAbsoluteXMax(){
		maxAbsoluteLocation.x = Float.NaN;
	}
	
	/**
	 * Remove the absolute limits applied to the max y location of this camera.
	 */
	public void removeLimitAbsoluteYMax(){
		maxAbsoluteLocation.y = Float.NaN;
	}
	
	/**
	 * Remove the absolute limits applied to the x location of this camera.
	 */
	public void removeLimitAbsoluteX(){
		removeLimitAbsoluteXMin();
		removeLimitAbsoluteXMax();
	}
	
	/**
	 * Remove the absolute limits applied to the y location of this camera.
	 */
	public void removeLimitAbsoluteY(){
		removeLimitAbsoluteYMin();
		removeLimitAbsoluteYMax();
	}
	
	@Override
	public float getXInPixels() {
		float x = super.getXInPixels();
		if (!Float.isNaN(minAbsoluteLocation.x)) {
			x = Math.max(x, minAbsoluteLocation.x);
		}
		if (!Float.isNaN(maxAbsoluteLocation.x)) {
			x = Math.min(x, maxAbsoluteLocation.x);
		}
		return x;
	}

	@Override
	public float getYInPixels() {
		float y = super.getYInPixels();
		if (!Float.isNaN(minAbsoluteLocation.y)) {
			y = Math.max(y, minAbsoluteLocation.y);
		}
		if (!Float.isNaN(maxAbsoluteLocation.y)) {
			y = Math.min(y, maxAbsoluteLocation.y);
		}
		return y;
	}
}
