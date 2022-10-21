package mekanism.common.capabilities.holder.energy;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyContainerHolder extends BasicHolder<IEnergyContainer> implements IEnergyContainerHolder {

    EnergyContainerHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addContainer(@NotNull IEnergyContainer container, RelativeSide... sides) {
        addSlotInternal(container, sides);
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction direction) {
        return getSlots(direction);
    }
}