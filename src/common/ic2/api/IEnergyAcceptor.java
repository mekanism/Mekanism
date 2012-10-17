package ic2.api;

import net.minecraft.src.TileEntity;

/**
 * For internal usage only.
 *
 * @see IEnergySink
 * @see IEnergyConductor
 */
public interface IEnergyAcceptor extends IEnergyTile {
	/**
	 * Determine if this acceptor can accept current from an adjacent emitter in a direction.
	 * 
	 * @param emitter energy emitter
	 * @param direction direction the energy is being received from
	 */
	boolean acceptsEnergyFrom(TileEntity emitter, Direction direction);
}

