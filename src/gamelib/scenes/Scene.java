package gamelib.scenes;

import gamelib.Drawable;
import gamelib.GameManager;
import gamelib.Updatable;

/**
 * Scenes are used to display things on the screen.
 *
 * @author Rebecca Stevens
 */
public abstract class Scene implements Updatable, Drawable {
	
	/**
	 * Enter the scene.
	 */
	public abstract void enter();
	
	/**
	 * Leave the scene.
	 */
	public abstract void leave();

	/**
	 * Make this the active level
	 */
	public final void makeActive(){
		GameManager.getMe().setActiveScene(this);
	}

}
