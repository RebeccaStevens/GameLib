package gamelib.game.entities.actors;

import gamelib.Style;
import gamelib.game.Level;
import gamelib.game.entities.PushableEntity;
import processing.core.PGraphics;

public class PushableBox extends PushableEntity {

	private Style style;

	public PushableBox(Level level, float x, float y, float width, float height, float resistance) {
		this(level, x, y, width, height, resistance, new Style());
		init();
	}
	
	public PushableBox(Level level, float x, float y, float width, float height, float resistance, Style style) {
		super(level, x, y, width, height, resistance);
		this.style = style;
	}
	
	public PushableBox(Level level, float x, float y, float z, float width, float height, float depth, float resistance) {
		this(level, x, y, z, width, height, depth, resistance, new Style());
	}
	
	public PushableBox(Level level, float x, float y, float z, float width, float height, float depth, float resistance, Style style) {
		super(level, x, y, z, width, height, depth, resistance);
		this.style = style;
		init();
	}
	
	private final void init(){
		setGravityEffected(true);
	}

	@Override
	public void update(float delta) {
		
	}

	@Override
	public void draw(PGraphics g) {
		if(getLevel().is3D()){
			draw3D(g);
		}
		else{
			draw2D(g);
		}
	}
	
	private void draw2D(PGraphics g) {
		style.apply(g);
		g.rect(0, 0, getWidthInPixels(), getHeightInPixels());
	}
	
	private void draw3D(PGraphics g) {
		style.apply(g);
		g.box(getWidthInPixels(), getHeightInPixels(), getDepthInPixels());
	}
}
