package net.zomis.lachesis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.zomis.lachesis.events.CancellableEvent;
import net.zomis.lachesis.events.EventExecutor;
import net.zomis.lachesis.events.GameOverEvent;
import net.zomis.lachesis.events.IEvent;
import net.zomis.lachesis.events.StartGameEvent;
import net.zomis.lachesis.retrievers.ComponentRetriever;
import net.zomis.lachesis.retrievers.Retrievers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Starting point for the entire ECS
 * 
 * @author Simon Forsberg
 */
public final class Game {
	private static final Logger logger = LogManager.getLogger(Game.class);

	private final AtomicInteger ids = new AtomicInteger();
	
	/**
	 * All the entities of a single game
	 */
	private final Map<Integer, Entity> entities = new HashMap<>();
	private final EventExecutor events = new EventExecutor();
	
	/**
	 * All the systems that comprise the game
	 */
	private final List<GameSystem> systems = new ArrayList<>();
	private final Random random = new Random();
	/**
	 * An enum for the current state of the game
	 */
	private GameState gameState = GameState.NOT_STARTED;
	
	public Game() {
	}
	
	/**
	 * Creates an entity, assigns an Id, adds it to the entities of the game object
	 * @return The created entity
	 */
	public Entity newEntity() {
		Entity entity = new Entity(this, ids.incrementAndGet());
		this.entities.put(entity.getId(), entity);
		return entity;
	}
	
	/**
	 * Executes an event while performing something in the middle of executing the event.
	 * It will first do an event for listeners that have registered before, then
	 * it will do runInBetween, then it will fire off the listeners that have registered
	 * for after.
	 * 
	 * @param <T> The event type that is executed
	 * @param event The event to execute
	 * @param runInBetween The action to run in between 
	 * @return The same event that was executed
	 */
	public <T extends IEvent> T executeEvent(T event, Runnable runInBetween) {
		return events.executeEvent(event, runInBetween);
	}

	/**
	 * 
	 * @param <T> The component type to create
	 * @param class1 Creates and returns the requested component type
	 * @return 
	 */
	public <T extends Component> ComponentRetriever<T> componentRetreiver(Class<T> class1) {
		return new ComponentRetriever<T>(class1);
	}
	
	/**
	 * 
	 * @param clazz The component to search for
	 * @return All entities that contain the component
	 */
	public Set<Entity> getEntitiesWithComponent(Class<? extends Component> clazz) {
		return entities.values().stream().filter(e -> e.hasComponent(clazz)).collect(Collectors.toSet());
	}

	/**
	 * 
	 * @return The EventExecutor object
	 */
	public EventExecutor getEvents() {
		return events;
	}

	/**
	 * Add a system to the systems list.
	 * If the game is in any other state besides NOT_STARTED, the system will be started
	 * 
	 * @param system The ECSSystem to add
	 */
	public void addSystem(GameSystem system) {
		logger.info("Add system: " + system);
		this.systems.add(system);
		Retrievers.inject(system, this);
		if (gameState != GameState.NOT_STARTED) {
			system.startGame(this);
		}
	}
	
	/**
	 * Starts the game if the game is in the NOT_STARTED state.
	 * Starts each of the systems in the systems list.
	 * Fires off the StartGameEvent
	 */
	public void startGame() {
		if (gameState != GameState.NOT_STARTED) {
			throw new IllegalStateException("Game is already started");
		}
		systems.forEach(sys -> sys.startGame(this));
		gameState = GameState.RUNNING;
		events.executePostEvent(new StartGameEvent(this));
	}

	/**
	 * @return The Random object for this Game instance
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * End the game.
	 */
	public void endGame() {
		this.executeCancellableEvent(new GameOverEvent(this), () -> gameState = GameState.GAME_ENDED);
	}
	
	/**
	 * @return The current state of the game
	 */
	public GameState getGameState() {
		return gameState;
	}

	/**
	 * @return True if the state is GAME_ENDED
	 */
	public boolean isGameOver() {
		return gameState == GameState.GAME_ENDED;
	}

	/**
	 * Remove an entity
	 * 
	 * @param entity The entity to remove
	 */
	void removeEntity(Entity entity) {
		entities.remove(entity.getId());
	}

	/**
	 * @param condition The type of entity to search for
	 * @return A list of matching entities.
	 */
	public List<Entity> findEntities(Predicate<Entity> condition) {
		return entities.values().stream().filter(condition).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param <T> The CancellableEvent
	 * @param event The event to execute
	 * @param runInBetween The action to run in between
	 * @return The same event that was executed
	 */
	public <T extends CancellableEvent> T executeCancellableEvent(T event, Runnable runInBetween) {
		return events.executeCancellableEvent(event, runInBetween);
	}

	/**
	 * 
	 * @param entity The id of the entity to get
	 * @return The requested entity object
	 */
	public Entity getEntity(int entity) {
		return entities.get(entity);
	}
	
	/**
	 * Sets a new seed for the random object.
	 * 
	 * @param seed The seed to set
	 */
	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}

	/**
	 * 
	 * @param <T> Generic ECSSystem
	 * @param clazz The system class to search for
	 * @return A list of systems that match the input system
	 */
	public <T extends GameSystem> List<T> findSystemsOfClass(Class<T> clazz) {
		return systems.stream()
				.filter(sys -> clazz.isAssignableFrom(sys.getClass()))
				.map(obj -> clazz.cast(obj))
				.collect(Collectors.toList());
	}

	/**
	 * Also removes any listeners that were listening for that system.
	 * 
	 * @param system The ECSSystem to remove
	 * @return Whether or not the system was successfully removed
	 */
	public boolean removeSystem(GameSystem system) {
		logger.info("Remove system " + system);
		events.removeListenersWithIdentifier(system);
		return systems.remove(system);
	}
	
}
