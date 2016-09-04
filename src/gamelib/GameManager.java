package gamelib;

import gamelib.game.Level;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.KeyEvent;

public class GameManager {
	
	private static GameManager me;
	
	private PApplet sketch;
	private Time time;
	private Level activeLevel;

	private Background background;

	private boolean autoDraw = true;

	public GameManager(PApplet sketch){
		if(me != null){
			throw new RuntimeException("Error: there can only be one LibraryManager.");
		}
		
		this.sketch = sketch;
		me = this;
		time = new Time(sketch);
		background = new Background(0xFF2277FF);
		
		sketch.registerMethod("pre", this);
		sketch.registerMethod("keyEvent", this);
	}
	
	public void pre(){
		if(autoDraw){
			update();
			draw();
		}
	}
	
	public Level getActiveLevel() {
		return activeLevel;
	}
	
	public void setActiveLevel(Level level){
		activeLevel = level;
	}
	
	public void update(){
		Time.update(sketch);
		if(activeLevel != null){
			activeLevel.update(Time.getTimeStep());
		}
	}
	
	public void draw(){
		draw(getGraphics());
	}

	private void draw(PGraphics g) {
		g.pushStyle();
		g.rectMode(PApplet.CENTER);
		g.ellipseMode(PApplet.CENTER);
		g.imageMode(PApplet.CENTER);
		if(activeLevel != null){
			background.draw(g);
			activeLevel.display(g);
		}
		g.popStyle();
		if(activeLevel != null){
			g.pushStyle();
			activeLevel.draw(g);
			g.popStyle();
		}
	}

	public void keyEvent(KeyEvent e){
		switch(e.getAction()){
			case KeyEvent.PRESS:	keyPressed(e);	break;
			case KeyEvent.RELEASE:	keyReleased(e);	break;
		}
	}

	private void keyPressed(KeyEvent e){
		for(Key key : Key.keys){
			key.update(e.getKeyCode(), true);
		}
	}

	private void keyReleased(KeyEvent e){
		for(Key key : Key.keys){
			key.update(e.getKeyCode(), false);
		}
	}
	
	public PApplet getSketch(){
		return sketch;
	}

	public Time getTime(){
		return time;
	}
	
	public void setAutoDraw(boolean b){
		this.autoDraw  = b;
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
