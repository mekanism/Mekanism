package mekanism.common.capabilities.holder.heat;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeatCapacitorHolder extends BasicHolder<IHeatCapacitor> implements IHeatCapacitorHolder {

    HeatCapacitorHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addCapacitor(@NotNull IHeatCapacitor tank, RelativeSide... sides) {
        addSlotInternal(tank, sides);
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction direction) {
        return getSlots(direction);
    }
}
