package net.zomis.lachesis.events;



public interface CancellableEvent extends IEvent {

	void setCancelled(boolean cancelled);
	boolean isCancelled();
	
}
