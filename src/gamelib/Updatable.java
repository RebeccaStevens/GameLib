package gamelib;

/**
 * Something that can be updated.
 *
 * @author Rebecca Stevens
 */
public interface Updatable {

	/**
	 * Update me.
	 * 
	 * @param delta - The amount of game time that has passed since the last frame
	 */
	public void update(float delta);
}
