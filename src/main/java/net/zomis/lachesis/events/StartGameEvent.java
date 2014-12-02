package net.zomis.lachesis.events;

import net.zomis.lachesis.core.Game;

public class StartGameEvent implements IEvent {
	
	private final Game game;

	public StartGameEvent(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}

}
