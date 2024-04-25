package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AttachedEnergyContainers extends AttachedContainers<IEnergyContainer> implements IMekanismStrictEnergyHandler {

    AttachedEnergyContainers(List<IEnergyContainer> containers, @Nullable IContentsListener listener) {
        super(containers, listener);
    }

    @Override
    protected ContainerType<IEnergyContainer, ?, ?> getContainerType() {
        return ContainerType.ENERGY;
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return containers;
    }
}