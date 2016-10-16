package gamelib.game;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An abstract level for 2D games.
 * 
 * @author Rebecca Stevens
 */
public abstract class Level2D extends Level {

	private final Map<Entity, Float> entityLayer;
	private final SortedSet<Entity> sortedEntities;
	
	public Level2D(){
		entityLayer = new HashMap<Entity, Float>();
		sortedEntities = new TreeSet<Entity>(new EntityLayerComparator());
	}
	
	@Override
	void addEntity(Entity entity) {
		super.addEntity(entity);
		sortedEntities.add(entity);
	}

	/**
	 * Set what layer the entity should be drawn on.
	 * By default, entities are on layer 0.
	 * 
	 * @param entity
	 * @param layer
	 */
	public void setEntityLayer(Entity entity, float layer) {
		entityLayer.put(entity, layer);
		resortEntity(entity);
	}

	@Override
	public final void update(float delta) {
		super.update(delta);
	}

	@Override
	protected Collection<Entity> getEntitiesToDraw() {
		return sortedEntities;
	}

	@Override
	protected void removeGameObjects(Collection<GameObject> toRemove) {
		super.removeGameObjects(toRemove);
		for (GameObject o : toRemove) {
			if (o instanceof Entity) {
				sortedEntities.remove(o);
			}
		}
	}

	/**
	 * Resort an entity in sortedEntities.
	 * This should be called when an entity's layer is changed.
	 * 
	 * @param entity
	 */
	private void resortEntity(Entity entity) {
		if (sortedEntities.remove(entity)) {
			sortedEntities.add(entity);
		}
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
			
			if (v1 > v2) {
				return 1;
			} else if (v1 < v2) {
				return -1;
			} else if (e1.equals(e2)) {
				return 0;
			} else {
				return 1;
			}
		}
	}
}
