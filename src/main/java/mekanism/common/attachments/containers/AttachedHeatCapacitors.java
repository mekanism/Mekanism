package mekanism.common.attachments.containers;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class AttachedHeatCapacitors extends AttachedContainers<IHeatCapacitor> implements IMekanismHeatHandler {

    AttachedHeatCapacitors(List<IHeatCapacitor> capacitors, @Nullable IContentsListener listener) {
        super(capacitors, listener);
    }

    @Override
    protected ContainerType<IHeatCapacitor, ?, ?> getContainerType() {
        return ContainerType.HEAT;
    }

    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction side) {
        return containers;
    }
}