package mekanism.api.lasers;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import net.minecraft.util.Direction;

public interface ILaserReceptor {

    void receiveLaserEnergy(@Nonnull FloatingLong energy, Direction side);

    boolean canLasersDig();
}