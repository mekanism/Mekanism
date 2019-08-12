package mekanism.api.energy;

import javax.annotation.Nullable;
import net.minecraft.util.Direction;

/**
 * Implement this if your TileEntity can accept energy.
 *
 * @author AidanBrady
 */
public interface IStrictEnergyAcceptor {

    /**
     * Transfer a certain amount of energy to this acceptor.
     *
     * @param amount   - amount to transfer
     * @param simulate - if the operation should be simulated
     *
     * @return energy used
     */
    double acceptEnergy(@Nullable Direction side, double amount, boolean simulate);

    /**
     * Whether or not this tile entity can accept energy from a certain side.
     *
     * @param side - side to check
     *
     * @return if tile entity accepts energy
     */
    boolean canReceiveEnergy(Direction side);
}