package net.zomis.lachesis.retrievers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.zomis.lachesis.core.Component;
import net.zomis.lachesis.core.Entity;
import net.zomis.lachesis.core.Game;

public class Retrievers {

	public static <T extends Component> ComponentRetriever<T> component(Class<T> clazz) {
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

	public static void inject(Object object, Game game) {
		List<Field> fields = AccessController.doPrivileged((PrivilegedAction<List<Field>>)() -> {
			Class<?> clazz = object.getClass();
			List<Field> result = new ArrayList<>();
			do {
				result.addAll(Arrays.asList(clazz.getDeclaredFields()));
				clazz = clazz.getSuperclass();
			}
			while (clazz != Object.class);
			return result;
		});
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			fields.stream().filter(field -> field.getAnnotation(Retriever.class) != null).forEach(field -> injectField(object, field, game));
			fields.stream().filter(field -> field.getAnnotation(RetrieverSingleton.class) != null).forEach(field -> injectSingleton(object, field, game));
			return null;
		});
	}

	private static void injectSingleton(Object obj, Field field, Game game) {
		Class<? extends Component> clazz = field.getType().asSubclass(Component.class);
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			field.setAccessible(true);
			try {
				field.set(obj, Retrievers.singleton(game, clazz));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			return null;
		});
	}

	private static void injectField(Object obj, Field field, Game game) {
		if (field.getType() != ComponentRetriever.class) {
			throw new RuntimeException(field.getType() + " is not a ComponentRetriever");
		}

		Type genericFieldType = field.getGenericType();

		if (genericFieldType instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) genericFieldType;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			Class<?> fieldArgClass = (Class<?>) fieldArgTypes[0];
			try {
				field.setAccessible(true);
				field.set(obj, Retrievers.component(fieldArgClass.asSubclass(Component.class)));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
