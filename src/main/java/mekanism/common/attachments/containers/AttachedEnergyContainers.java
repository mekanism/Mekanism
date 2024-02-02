package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AttachedEnergyContainers extends AttachedContainers<IEnergyContainer> implements IMekanismStrictEnergyHandler {

    public AttachedEnergyContainers(List<IEnergyContainer> containers) {
        super(containers);
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return containers;
    }
}