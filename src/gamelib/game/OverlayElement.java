package gamelib.game;

import gamelib.Drawable;
import processing.core.PVector;

/**
 * An overlay element to be shown over the level.
 *
 * @author Rebecca Stevens
 */
public abstract class OverlayElement extends GameObject implements Drawable {
	
	private final PVector locationOnScreen;

	public OverlayElement(Level level, float x, float y) {
		super(level, 0, 0);
		locationOnScreen = new PVector(x, y);
	}

	@Override
	public void update(float delta) {
		updateLocationOnScreen();
	}

	/**
	 * Update the location of this element on the screen.
	 */
	public void updateLocationOnScreen() {
		Level level = getLevel();
		Camera camera = level.getCamera();
		setLocation(
				level.convertPixelsXToGridUnits(locationOnScreen.x) + camera.getXLimited(),
				level.convertPixelsYToGridUnits(locationOnScreen.y) + camera.getYLimited());
	}
	
	@Override
	public float getXInPixels() {
		return locationOnScreen.x;
	}

	@Override
	public float getYInPixels() {
		return locationOnScreen.y;
	}

	@Override
	public float getZInPixels() {
		return locationOnScreen.z;
	}

	/**
	 * Set the x location of this element on screen (in pixels).
	 * 
	 * @param x
	 */
	public void setXLocationOnScreen(float x) {
		locationOnScreen.x = x;
	}

	/**
	 * Set the y location of this element on screen (in pixels).
	 * 
	 * @param y
	 */
	public void setYLocationOnScreen(float y) {
		locationOnScreen.y = y;
	}

	/**
	 * Set the location of this element on screen (in pixels).
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocationOnScreen(float x, float y) {
		locationOnScreen.set(x, y);
	}

	/**
	 * Set the location of this element on screen (in pixels).
	 * 
	 * @param location
	 */
	public void setLocationOnScreen(PVector location) {
		locationOnScreen.set(location);
	}
}
