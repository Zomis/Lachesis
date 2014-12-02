package net.zomis.lachesis.events;

import net.zomis.lachesis.core.Game;

public class GameOverEvent implements CancellableEvent {

	private boolean cancelled;
	private final Game game;

	public GameOverEvent(Game game) {
		this.game = game;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	public Game getGame() {
		return game;
	}
	
}
