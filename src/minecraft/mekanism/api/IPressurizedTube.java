package mekanism.api;

import net.minecraft.tileentity.TileEntity;

public interface IPressurizedTube 
{
	/**
	 * Whether or not this tube can transfer gas.
	 * @return if the tube can transfer gas
	 */
	public boolean canTransferGas(TileEntity fromTile);
}
