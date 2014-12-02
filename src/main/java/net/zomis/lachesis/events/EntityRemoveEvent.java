package net.zomis.lachesis.events;

import net.zomis.lachesis.core.Entity;

public class EntityRemoveEvent implements IEvent {

	private final Entity entity;

	public EntityRemoveEvent(Entity entity) {
		this.entity = entity;
	}
	
	public Entity getEntity() {
		return entity;
	}

}
