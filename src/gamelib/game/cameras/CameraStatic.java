package gamelib.game.cameras;

import gamelib.game.Camera;
import gamelib.game.Level;
import processing.core.PVector;

public class CameraStatic extends Camera {
	
	public CameraStatic(Level level){
		this(level, new PVector(), new PVector());
	}
	
	public CameraStatic(Level level, float x, float y, float rotation) {
		this(level, new PVector(x, y), new PVector(rotation, 0));
	}

	public CameraStatic(Level level, float x, float y, float z, float tilt, float pan, float roll) {
		this(level, new PVector(x, y, z), new PVector(tilt, pan, roll));
	}

	public CameraStatic(Level level, PVector location, PVector rotation){
		super(level);
		setLocation(location);
//		this.rotation = rotation.copy(); //TODO
	}

	@Override
	public void update(float delta) {
	}
}