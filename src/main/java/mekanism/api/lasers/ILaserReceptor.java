package mekanism.api.lasers;

import net.minecraftforge.common.util.ForgeDirection;

public interface ILaserReceptor
{
	public void receiveLaserEnergy(double energy, ForgeDirection side);

	public boolean canLasersDig();
}
