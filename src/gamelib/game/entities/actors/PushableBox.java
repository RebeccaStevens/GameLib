package gamelib.game.entities.actors;

import gamelib.game.Level;
import gamelib.game.entities.PushableEntity;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PStyle;

public class PushableBox extends PushableEntity {

	private PStyle style;

	public PushableBox(Level level, float x, float y, float width, float height, float resistance) {
		this(level, x, y, width, height, resistance, createDefaultStyle());
		init();
	}
	
	public PushableBox(Level level, float x, float y, float width, float height, float resistance, PStyle style) {
		super(level, x, y, width, height, resistance);
		this.style = style;
	}
	
	public PushableBox(Level level, float x, float y, float z, float width, float height, float depth, float resistance) {
		this(level, x, y, z, width, height, depth, resistance, createDefaultStyle());
	}
	
	public PushableBox(Level level, float x, float y, float z, float width, float height, float depth, float resistance, PStyle style) {
		super(level, x, y, z, width, height, depth, resistance);
		this.style = style;
		init();
	}

	private static PStyle createDefaultStyle() {
		PStyle style = new PStyle();
		style.fillColor = 0xFFFFFFFF;
		style.fill = true;
		return style;
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
		g.style(style);
		g.rectMode = PConstants.CENTER;
		g.rect(0, 0, getWidthInPixels(), getHeightInPixels());
	}
	
	private void draw3D(PGraphics g) {
		g.style(style);
		g.rectMode = PConstants.CENTER;
		g.box(getWidthInPixels(), getHeightInPixels(), getDepthInPixels());
	}
}
