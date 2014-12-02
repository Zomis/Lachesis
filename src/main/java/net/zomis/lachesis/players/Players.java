package net.zomis.lachesis.players;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.zomis.lachesis.core.Entity;
import net.zomis.lachesis.core.Game;
import net.zomis.lachesis.retrievers.ComponentRetriever;
import net.zomis.lachesis.retrievers.Retrievers;

public class Players {
	
	private static final ComponentRetriever<PlayerComponent> component = Retrievers.component(PlayerComponent.class);
	
	/**
	 * Declare this player as having lost the game 
	 */
	public static void loseGame(Entity entity) {
		eliminate(entity, false);
	}
	
	/**
	 * Declare this player as having won the game
	 */
	public static void winGame(Entity entity) {
		eliminate(entity, true);
	}
	
	private static void eliminate(Entity entity, boolean winner) {
		List<Entity> players = new ArrayList<>(entity.getGame().getEntitiesWithComponent(PlayerComponent.class));
		players.sort(Comparator.comparing(e -> e.getComponent(PlayerComponent.class).getIndex()));
		
		// if no one else has been eliminated, the player is at 1st place. Because the player itself has not been eliminated, it should get increased below.
		int playerResultPosition = winner ? 0 : players.size() + 1;
		
		boolean posTaken = false;
		do {
			posTaken = false;
			playerResultPosition += winner ? -1 : +1;
			for (Entity pp : players) {
				PlayerComponent playerComponent = pp.getComponent(PlayerComponent.class);
				if (playerComponent.isEliminated() && playerComponent.getResultPosition() == playerResultPosition) {
					posTaken = true;
					break;
				}
			}
		}
		while (posTaken);
		
		eliminate(entity, winner, playerResultPosition);
	}
	
	private static void eliminate(Entity entity, boolean winner, int resultPosition) {
		PlayerComponent playerData = component.get(entity);
		if (playerData.isEliminated()) {
			throw new IllegalStateException("Can't be eliminated more than once.");
		}
		Game game = entity.getGame();
		game.executeCancellableEvent(new PlayerEliminatedEvent(entity, winner, resultPosition), () -> {
			playerData.setResultPosition(resultPosition);
			playerData.setWinnerDeclaration(winner);
		});
	}

}
