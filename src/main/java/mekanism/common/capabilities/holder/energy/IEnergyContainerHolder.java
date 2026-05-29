package mekanism.common.capabilities.holder.energy;

import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface IEnergyContainerHolder extends IHolder {

    @Nullable
    IEnergyContainer getContainer(@Nullable Direction side);
}