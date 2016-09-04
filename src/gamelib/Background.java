package gamelib;

import processing.core.PGraphics;

public class Background {

	private int color;

	public Background(int color) {
		this.color = color;
	}

	public void draw(PGraphics g) {
		g.background(color);
	}

}
