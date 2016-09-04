package gamelib.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class Level2D extends Level {

	private boolean entitiesNeedResorting;
	private Map<Entity, Float> entityLayer;
	
	public Level2D(){
		entityLayer = new HashMap<Entity, Float>();
	}

	public void addEntity(Entity ent, float layer){
		super.addEntity(ent);
		entityLayer.put(ent, layer);
		entitiesNeedResorting = true;
	}
	
	@Override
	public void update(float delta){
		if(entitiesNeedResorting){
			Collections.sort(entities, new Comparator<Entity>(){
				@Override
				public int compare(Entity e1, Entity e2){
					Float v1_obj = entityLayer.get(e1);
					Float v2_obj = entityLayer.get(e2);
					float v1 = (v1_obj == null) ? 0 : v1_obj;
					float v2 = (v2_obj == null) ? 0 : v2_obj;
					return (v1 > v2) ? 1 : (v1 < v2) ? -1 : 0;
				}
			});
			entitiesNeedResorting = false;
		}
		super.update(delta);
	}

	@Override
	public final boolean is3D() {
		return false;
	}
}
