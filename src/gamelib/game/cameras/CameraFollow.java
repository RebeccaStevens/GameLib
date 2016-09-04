package gamelib.game.cameras;

import gamelib.game.Camera;
import gamelib.game.Entity;
import gamelib.game.Level;

public class CameraFollow extends Camera {

	private Entity target;

	public CameraFollow(Level level){
		this(level, null);
	}
	
	public CameraFollow(Level level, Entity entity){
		super(level);
		follow(entity);
	}
	
	public void follow(Entity entity){
		target = entity;
	}

	@Override
	public void update(double delta) {
		if(target == null) return;
		location.set(target.getLocation());
	}
}