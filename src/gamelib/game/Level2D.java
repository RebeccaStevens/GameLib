package gamelib.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class Level2D extends Level {

	private boolean entitiesNeedResorting;
	private final Map<Entity, Float> entityLayer;
	private final EntityLayerComparator entityLayerComparator;
	
	public Level2D(){
		entityLayer = new HashMap<Entity, Float>();
		entityLayerComparator = new EntityLayerComparator();
	}

	public void addGameObject(Entity ent, float layer){
		super.addGameObject(ent);
		entityLayer.put(ent, layer);
		entitiesNeedResorting = true;
	}
	
	@Override
	public final void update(float delta){
		if(entitiesNeedResorting){
			Collections.sort(entities, entityLayerComparator);
			entitiesNeedResorting = false;
		}
		super.update(delta);
	}

	@Override
	public final boolean is3D() {
		return false;
	}
	
	private class EntityLayerComparator implements Comparator<Entity> {
		@Override
		public int compare(Entity e1, Entity e2){
			Float v1_obj = entityLayer.get(e1);
			Float v2_obj = entityLayer.get(e2);
			float v1 = (v1_obj == null) ? 0 : v1_obj;
			float v2 = (v2_obj == null) ? 0 : v2_obj;
			return (v1 > v2) ? 1 : (v1 < v2) ? -1 : 0;
		}
	}
}
