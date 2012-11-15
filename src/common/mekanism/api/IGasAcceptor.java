package mekanism.api;

import mekanism.api.IStorageTank.EnumGas;
import net.minecraftforge.common.ForgeDirection;

/**
 * Implement this if your tile entity accepts hydrogen from a foreign, external source.
 * @author AidanBrady
 *
 */
public interface IGasAcceptor 
{
	/**
	 * Transfer a certain amount of hydrogen to this acceptor.
	 * @param amount - amount to transfer
	 * @return rejects
	 */
	public int transferGasToAcceptor(int amount, EnumGas type);
	
	/**
	 * Whether or not this tile entity accepts hydrogen from a certain side.
	 * @param side - side to check
	 * @return if tile entity accepts energy
	 */
	public boolean canReceiveGas(ForgeDirection side, EnumGas type);
}
