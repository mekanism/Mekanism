package mekanism.api;

import net.minecraftforge.common.ForgeDirection;

/**
 * Implement this if your tile entity accepts energy from a foreign, external source.
 * @author AidanBrady
 *
 */
public interface IEnergyAcceptor 
{
	/**
	 * Transfer a certain amount of energy to this acceptor.
	 * @param amount - amount to transfer
	 * @return rejects
	 */
	public int transferToAcceptor(int amount);
	
	/**
	 * Whether or not this tile entity accepts energy from a certain side.
	 * @param side - side to check
	 * @return if tile entity accepts energy
	 */
	public boolean canReceive(ForgeDirection side);
}
