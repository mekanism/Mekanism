package mekanism.api.lasers;

import net.minecraft.util.EnumFacing;

public interface ILaserReceptor
{
	public void receiveLaserEnergy(double energy, EnumFacing side);

	public boolean canLasersDig();
}
