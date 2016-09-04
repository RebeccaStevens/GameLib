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
	 */
	public void draw(PGraphics g);
}
