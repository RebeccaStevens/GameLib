package gamelib.game.lights;

import gamelib.game.DynamicLight;
import gamelib.game.Level;
import processing.core.PGraphics;

public class PointLight extends DynamicLight {

	public PointLight(Level level, float x, float y, float z, int color) {
		super(level, x, y, z, color);
	}

	@Override
	public void apply(PGraphics g) {
		g.pointLight(getRed(), getGreen(), getBlue(), getX(), getY(), getZ());
	}

	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
	}
}
