package gamelib;

import gamelib.scenes.GameScene;
import gamelib.scenes.Scene;
import processing.core.PApplet;
import processing.core.PGraphics;

public final class GameManager {
	
	private static GameManager me;
	
	private PApplet sketch;
	private Time time;
	private Scene activeScene;
	private GameScene gameScene;

	private Background background;

	private boolean autoDraw = true;

	public GameManager(PApplet sketch){
		if(me != null){
			throw new RuntimeException("Error: there can only be one LibraryManager.");
		}
		
		this.sketch = sketch;
		GameManager.me = this;
		this.time = new Time();
		this.background = new Background(0xFF2277FF);
		this.gameScene = new GameScene();
		setActiveScene(this.gameScene);
		
		sketch.registerMethod("pre", this);
	}
	
	public void pre(){
		if(this.autoDraw){
			update();
			draw();
		}
	}
	
	public void update(){
		time.update();
		if(this.activeScene != null){
			this.activeScene.update(time.getTimeStep());
		}
	}
	
	public void draw(){
		draw(getGraphics());
	}

	public void draw(PGraphics g) {
		g.pushStyle();
		g.rectMode(PApplet.CENTER);
		g.ellipseMode(PApplet.CENTER);
		g.imageMode(PApplet.CENTER);
		if (this.activeScene != null) {
			this.background.draw(g);
			this.activeScene.draw(g);
		}
		g.popStyle();
	}
	
	public PApplet getSketch(){
		return this.sketch;
	}

	public Scene getActiveScene() {
		return this.activeScene;
	}

	public void setActiveScene(Scene scene) {
		if (this.activeScene != null) {
			this.activeScene.leave();
		}
		this.activeScene = scene;
		this.activeScene.enter();
	}

	/**
	 * @return the gameScene
	 */
	public GameScene getGameScene() {
		return gameScene;
	}

	public Time getTime(){
		return this.time;
	}
	
	public void setAutoDraw(boolean b){
		this.autoDraw = b;
	}
	
	/**
	 * Get the graphics object that the level is being drawn to.
	 * @return
	 */
	public final PGraphics getGraphics() {
		return sketch.g;
	}

	public static GameManager getMe(){
		return me;
	}
}
