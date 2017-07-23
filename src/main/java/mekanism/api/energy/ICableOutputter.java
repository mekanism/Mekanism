package mekanism.api.energy;

import net.minecraft.util.EnumFacing;

/**
 * @deprecated functionality has been replaced by {@link IStrictEnergyOutputter#canOutputEnergy(net.minecraft.util.EnumFacing)}
 * Class remains solely to prevent crashes on load
 */
@Deprecated
public interface ICableOutputter
{
	public boolean canOutputTo(EnumFacing side);
}
