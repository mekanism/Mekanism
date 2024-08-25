package mekanism.common.capabilities.holder.heat;

import java.util.Collections;
import java.util.List;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigHeatCapacitorHolder extends ConfigHolder<IHeatCapacitor> implements IHeatCapacitorHolder {

    protected ConfigHeatCapacitorHolder(ISideConfiguration sideConfiguration) {
        super(sideConfiguration);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> slotInfo instanceof HeatSlotInfo info ? info.getHeatCapacitors() : Collections.emptyList());
    }

    void addCapacitor(@NotNull IHeatCapacitor capacitor) {
        slots.add(capacitor);
    }
}
