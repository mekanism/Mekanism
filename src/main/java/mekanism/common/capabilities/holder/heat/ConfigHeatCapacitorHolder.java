package mekanism.common.capabilities.holder.heat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import net.minecraft.util.Direction;

public class ConfigHeatCapacitorHolder extends ConfigHolder implements IHeatCapacitorHolder {

    protected final List<IHeatCapacitor> capacitors = new ArrayList<>();

    protected ConfigHeatCapacitorHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction direction) {
        return getSlots(direction, capacitors, slotInfo -> {
            if (slotInfo instanceof HeatSlotInfo && slotInfo.isEnabled()) {
                return ((HeatSlotInfo) slotInfo).getHeatCapacitors();
            }
            return Collections.emptyList();
        });
    }

    void addCapacitor(@Nonnull IHeatCapacitor capacitor) {
        capacitors.add(capacitor);
    }
}
