package net.zomis.lachesis.retrievers;

import java.util.Objects;
import java.util.Set;

import net.zomis.lachesis.core.Component;
import net.zomis.lachesis.core.Entity;
import net.zomis.lachesis.core.Game;

public class ComponentRetriever<T extends Component> {

	private final Class<T> clazz;

	public ComponentRetriever(Class<T> clazz) {
		this.clazz = clazz;
	}

	public boolean has(Entity entity) {
		return entity.hasComponent(clazz);
	}

	public T get(Entity entity) {
		Objects.requireNonNull(entity, "Cannot retrieve component " + clazz.getSimpleName() + " on a null entity");
		return entity.getComponent(clazz);
	}

	public T required(Entity entity) {
		String removed = entity.isRemoved() ? " Entity has been removed!" : "";
		return Objects.requireNonNull(get(entity), clazz.getName() + " not found on entity: " + entity +
				" available components is: " + entity.getSuperComponents(Component.class) + removed);
	}
	
	public static <T extends Component> ComponentRetriever<T> retreiverFor(Class<T> clazz) {
		return new ComponentRetriever<>(clazz);
	}

	public static <T extends Component> ComponentRetriever<T> singleton(Class<T> class1) {
		return new ComponentRetriever<T>(null) {
			
			@Override
			public boolean has(Entity entity) {
				return get(entity) != null;
			}
			
			@Override
			public T get(Entity entity) {
				return singleton(entity.getGame(), class1);
			}
			
		};
	}

	public static <T extends Component> T singleton(Game game, Class<T> class1) {
		Set<Entity> all = game.getEntitiesWithComponent(class1);
		if (all.size() != 1) {
			throw new IllegalStateException("Expected to find exactly one " + class1.getSimpleName() + ", found " + all.size());
		}
		return all.iterator().next().getComponent(class1);
	}

}
