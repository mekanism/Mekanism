package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityReactorLaserFocusMatrix extends TileEntityReactorBlock implements ILaserReceptor {

    @Override
    public boolean isFrame() {
        return false;
    }

    @Override
    public void receiveLaserEnergy(double energy, Direction side) {
        if (getReactor() != null) {
            getReactor().addTemperatureFromEnergyInput(energy);
        }
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.LASER_RECEPTOR_CAPABILITY) {
            return Capabilities.LASER_RECEPTOR_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}