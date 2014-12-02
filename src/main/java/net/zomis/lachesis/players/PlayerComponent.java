package net.zomis.lachesis.players;

import net.zomis.lachesis.core.Component;

public class PlayerComponent extends Component {

	private final int index;
	private int resultPosition;
	private Boolean winnerDeclaration;
	private String name;

	public PlayerComponent(int index, String name) {
		this.index = index;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
	
	/**
	 * @return Return the ranking the player got in this game. 1 is the top winner.
	 */
	public int getResultPosition() {
		return resultPosition;
	}
	
	/**
	 * @return True if this player has been declared as winning or losing the game
	 */
	public boolean isEliminated() {
		return this.resultPosition != 0;
	}
	
	/**
	 * @return True if player was declared winner, false if player was declared loser. Null if player hasn't been eliminated yet.
	 */
	public Boolean getWinnerDeclaration() {
		return winnerDeclaration;
	}

	@Override
	public String toString() {
		return "PlayerComponent [index=" + index + ", name=" + name + "]";
	}

	void setResultPosition(int resultPosition) {
		this.resultPosition = resultPosition;
	}

	void setWinnerDeclaration(Boolean winner) {
		this.winnerDeclaration = winner;
	}

}
