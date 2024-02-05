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
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return containers;
    }

    @Override
    protected boolean isContainerCompatible(IEnergyContainer a, IEnergyContainer b) {
        //TODO - 1.20.4: Compare usage for resistive heater block items
        return a.getEnergy().equals(b.getEnergy());
    }
}