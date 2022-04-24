package mekanism.common.capabilities.holder.energy;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;

public class EnergyContainerHolder extends BasicHolder<IEnergyContainer> implements IEnergyContainerHolder {

    EnergyContainerHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addContainer(@Nonnull IEnergyContainer container, RelativeSide... sides) {
        addSlotInternal(container, sides);
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction direction) {
        return getSlots(direction);
    }
}