package ic2.api.energy.event;

import net.minecraftforge.event.world.WorldEvent;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;

/**
 * Base class for energy net events, don't use it directly.
 *
 * See ic2/api/energy/usage.txt for an overall description of the energy net api.
 */
public class EnergyTileEvent extends WorldEvent {
	public EnergyTileEvent(IEnergyTile tile) {
		super(EnergyNet.instance.getWorld(tile));

		if (getWorld() == null) throw new NullPointerException("world is null");

		this.tile = tile;
	}

	public final IEnergyTile tile;
}

