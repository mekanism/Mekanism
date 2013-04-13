package ic2.api.energy.tile;

import net.minecraft.tileentity.TileEntity;

import ic2.api.Direction;

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

