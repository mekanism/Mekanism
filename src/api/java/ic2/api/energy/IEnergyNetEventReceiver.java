package ic2.api.energy;

import ic2.api.energy.tile.IEnergyTile;

/**
 * Interface for handlers being invoked after energy net changes.
 *
 * <p>Normally it's enough to listen for the neighbor update, which also happens, but using this
 * mechanism allows listening to events that are not adjacent. Mechanisms that inject energy over
 * a distance or provide compatibility with 3rd party energy may benefit from it.
 *
 * <p>The events will be fired after validation and only for successful changes.
 *
 * <p>WARNING: The events may (and will) be invoked from arbitrary threads. It's up to the
 * implementation of IEnergyNetEventReceiver to properly synchronize them.
 */
public interface IEnergyNetEventReceiver {
	void onAdd(IEnergyTile tile);
	void onRemove(IEnergyTile tile);
}
