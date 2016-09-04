package gamelib.game.lights;

import gamelib.game.DynamicLight;
import gamelib.game.Entity;
import gamelib.game.Level;
import processing.core.PGraphics;
import processing.core.PVector;

public class SpotLight extends DynamicLight {

	private PVector normal;
	private float angle;
	private float concentration;
	
	private Entity target;
	
	public SpotLight(Level level, PVector location, Entity target, float angle, float concentration, int color) {
		this(level, location.x, location.y, location.z, 0, 0, 0, angle, concentration, color);
		this.target = target;
	}
	
	public SpotLight(Level level, PVector location, PVector normal, float angle, float concentration, int color) {
		this(level, location.x, location.y, location.z, normal.x, normal.y, normal.z, angle, concentration, color);
	}

	public SpotLight(Level level, float x, float y, float z, float nx, float ny, float nz, float angle, float concentration, int color) {
		super(level, x, y, z, color);
		this.normal = new PVector(nx, ny, nz);
		this.angle = angle;
		this.concentration = concentration;
	}

	@Override
	public void apply(PGraphics g) {
		g.spotLight(getRed(), getGreen(), getBlue(), getX(), getY(), getZ(), normal.x, normal.y, normal.z, angle, concentration);
	}

	@Override
	public void update(float delta) {
		if(this.target != null){
			this.normal.set(PVector.sub(target.getLocation(), this.getLocation()));
		}
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public void setTarget(PVector target) {
		this.target = null;
		this.normal.set(PVector.sub(target, this.getLocation()));
	}
}
