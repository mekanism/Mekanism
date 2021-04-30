package mekanism.common.capabilities.holder.heat;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.util.Direction;

public class HeatCapacitorHolder extends BasicHolder<IHeatCapacitor> implements IHeatCapacitorHolder {

    HeatCapacitorHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addCapacitor(@Nonnull IHeatCapacitor tank, RelativeSide... sides) {
        addSlotInternal(tank, sides);
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction direction) {
        return getSlots(direction);
    }
}
