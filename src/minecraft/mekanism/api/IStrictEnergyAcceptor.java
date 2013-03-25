package mekanism.api;

import net.minecraftforge.common.ForgeDirection;

public interface IStrictEnergyAcceptor 
{
	/**
	 * Transfer a certain amount of energy to this acceptor.
	 * @param amount - amount to transfer
	 * @return rejects
	 */
	public double transferEnergyToAcceptor(double amount);
	
	/**
	 * Whether or not this tile entity accepts energy from a certain side.
	 * @param side - side to check
	 * @return if tile entity accepts energy
	 */
	public boolean canReceiveEnergy(ForgeDirection side);
}
