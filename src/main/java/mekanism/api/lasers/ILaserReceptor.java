package mekanism.api.lasers;

import net.minecraft.util.EnumFacing;

public interface ILaserReceptor {

    void receiveLaserEnergy(double energy, EnumFacing side);

    boolean canLasersDig();
}
