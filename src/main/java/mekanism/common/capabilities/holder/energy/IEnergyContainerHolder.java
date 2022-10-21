package mekanism.common.capabilities.holder.energy;

import java.util.List;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEnergyContainerHolder extends IHolder {

    @NotNull
    List<IEnergyContainer> getEnergyContainers(@Nullable Direction side);
}