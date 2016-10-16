package gamelib.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract level for 2D games.
 * 
 * @author Rebecca Stevens
 */
public abstract class Level2D extends Level {

	private boolean entitiesNeedResorting;
	private final Map<Entity, Float> entityLayer;
	private final EntityLayerComparator entityLayerComparator;
	
	public Level2D(){
		entityLayer = new HashMap<Entity, Float>();
		entityLayerComparator = new EntityLayerComparator();
	}

	/**
	 * Set what layer the entity should be drawn on.
	 * By default, entities are on layer 0.
	 * 
	 * @param entity
	 * @param layer
	 */
	public void setEntityLayer(Entity entity, float layer){
		entityLayer.put(entity, layer);
		entitiesNeedResorting = true;
	}
	
	@Override
	public final void update(float delta){
		if (entitiesNeedResorting) {
			Collections.sort(entities, entityLayerComparator);
			entitiesNeedResorting = false;
		}
		super.update(delta);
	}

	@Override
	public final boolean is3D() {
		return false;
	}
	
	/**
	 * A Comparator for sorting entities by what layer they are on.
	 * 
	 * @author Rebecca Stevens
	 */
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
