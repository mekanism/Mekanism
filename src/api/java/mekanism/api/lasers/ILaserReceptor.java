package mekanism.api.lasers;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Expose this as a capability on your TileEntity to handle what happens when a laser hits it.
 */
public interface ILaserReceptor {

    /**
     * Called to receive energy from a laser when the block is hit by a laser.
     *
     * @param energy      Energy received.
     * @param transaction The transaction that this operation is a part of.
     *
     * @return Energy received
     */
    long receiveLaserEnergy(long energy, TransactionContext transaction);

    /**
     * Checks if a laser can break this receptor.
     *
     * @return {@code false} to not allow the laser to break this block.
     */
    boolean canLasersDig();
}