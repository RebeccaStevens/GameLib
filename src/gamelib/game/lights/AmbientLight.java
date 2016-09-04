package gamelib.game.lights;

import gamelib.game.Level;
import gamelib.game.Light;
import processing.core.PGraphics;

public class AmbientLight extends Light {

	public AmbientLight(Level level, int color) {
		super(level, 0, 0, 0, color);
	}

	@Override
	public void apply(PGraphics g) {
		g.ambientLight(getRed(), getGreen(), getBlue(), getX(), getY(), getZ());
	}

}
