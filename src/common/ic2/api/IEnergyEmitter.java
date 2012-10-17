package ic2.api;

import net.minecraft.src.TileEntity;

/**
 * For internal usage only.
 *
 * @see IEnergySource
 * @see IEnergyConductor
 */
public interface IEnergyEmitter extends IEnergyTile {
	/**
	 * Determine if this emitter can emit energy to an adjacent receiver.
	 * 
	 * @param receiver receiver
	 * @param direction direction the receiver is from the emitter
	 * @return Whether energy should be emitted
	 */
	boolean emitsEnergyTo(TileEntity receiver, Direction direction);
}

