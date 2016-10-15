package gamelib.game.entities.platforms;

import gamelib.game.Level;
import gamelib.game.entities.Platform;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PStyle;

public class BasicPlatform extends Platform {

	private PStyle style;

	public BasicPlatform(Level level, float x, float y, float width, float height) {
		this(level, x, y, width, height, createDefaultStyle());
	}
	
	public BasicPlatform(Level level, float x, float y, float width, float height, PStyle style) {
		super(level, x, y, width, height);
		this.style = style;
	}
	
	public BasicPlatform(Level level, float x, float y, float z, float width, float height, float depth) {
		this(level, x, y, z, width, height, depth, createDefaultStyle());
	}
	
	public BasicPlatform(Level level, float x, float y, float z, float width, float height, float depth, PStyle style) {
		super(level, x, y, z, width, height, depth);
		this.style = style;
	}

	private static PStyle createDefaultStyle() {
		PStyle style = new PStyle();
		style.fillColor = 0xFFFFFFFF;
		style.fill = true;
		return style;
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
