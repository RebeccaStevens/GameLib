package gamelib;

import gamelib.scenes.GameScene;
import gamelib.scenes.Scene;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public final class GameManager {
	
	private static GameManager me;
	
	private PApplet sketch;
	private Time time;
	private Scene activeScene;
	private GameScene gameScene;

	private boolean autoDraw = true;
	
	private boolean drawFPS = false;

	public GameManager(PApplet sketch){
		if(me != null){
			throw new RuntimeException("Error: there can only be one LibraryManager.");
		}
		
		this.sketch = sketch;
		GameManager.me = this;
		this.time = new Time();
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
			this.activeScene.draw(g);
		}
		if (drawFPS) {
			drawFPS(g);
		}
		g.popStyle();
	}
	
	/**
	 * Draw the frame per second count in the top right corner.
	 * 
	 * @param g
	 */
	private void drawFPS(PGraphics g) {
		if (sketch.frameRate < 30) {
			g.fill(255, 0, 0);
		} else if (sketch.frameRate < 45) {
			g.fill(255, 255, 0);
		} else {
			g.fill(0, 255, 0);
		}
		
		g.textAlign(PConstants.RIGHT, PConstants.TOP);
		g.textSize(24);
		g.text(sketch.frameRate, sketch.width - 10, 10);
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
	 * Set to true to draw the Frames per Second count in the top right corner;
	 * 
	 * @param drawFPS
	 */
	public void setDrawFPS(boolean drawFPS) {
		this.drawFPS = drawFPS;
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
