package gamelib;

import processing.core.PGraphics;

/**
 * Something that can be draw.
 *
 * @author Rebecca Stevens
 */
public interface Drawable {

	/**
	 * Draw me.
	 * 
	 * @param g - The graphics object to draw to
	 * @param delta - The amount of game time that has passed since the last frame
	 */
	public void draw(PGraphics g, float delta);
}
