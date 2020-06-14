package mekanism.common.capabilities.holder.heat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.HeatSlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
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
        if (direction == null) {
            //If we want the internal, give all of our slots
            return capacitors;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config (most likely case is it hasn't been setup yet), just return all slots
            return capacitors;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't support gases in our configuration at all so just return all
            return capacitors;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        if (slotInfo instanceof HeatSlotInfo && slotInfo.isEnabled()) {
            return ((HeatSlotInfo) slotInfo).getHeatCapacitors();
        }
        return Collections.emptyList();
    }

    void addCapacitor(@Nonnull IHeatCapacitor capacitor) {
        capacitors.add(capacitor);
    }
}
