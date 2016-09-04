package gamelib.game;

public abstract class DynamicLight extends Light{
	
	public DynamicLight(Level level, float x, float y, float z, int color){
		super(level, x, y, z, color);
	}
	
	public abstract void update(float delta);
}
