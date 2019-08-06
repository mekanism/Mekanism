package mekanism.api.lasers;

import net.minecraft.util.Direction;

public interface ILaserReceptor {

    void receiveLaserEnergy(double energy, Direction side);

    boolean canLasersDig();
}