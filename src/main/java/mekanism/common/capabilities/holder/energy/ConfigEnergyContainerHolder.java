package mekanism.common.capabilities.holder.energy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.util.Direction;

public class ConfigEnergyContainerHolder extends ConfigHolder implements IEnergyContainerHolder {

    protected final List<IEnergyContainer> containers = new ArrayList<>();

    public ConfigEnergyContainerHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addContainer(@Nonnull IEnergyContainer container) {
        containers.add(container);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.ENERGY;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction direction) {
        if (direction == null) {
            //If we want the internal, give all of our slots
            return containers;
        }
        TileComponentConfig config = configSupplier.get();
        if (config == null) {
            //If we don't have a config (most likely case is it hasn't been setup yet), just return all slots
            return containers;
        }
        ConfigInfo configInfo = config.getConfig(getTransmissionType());
        if (configInfo == null) {
            //We don't support energy in our configuration at all so just return all
            return containers;
        }
        RelativeSide side = RelativeSide.fromDirections(facingSupplier.get(), direction);
        ISlotInfo slotInfo = configInfo.getSlotInfo(side);
        if (slotInfo instanceof EnergySlotInfo && slotInfo.isEnabled()) {
            return ((EnergySlotInfo) slotInfo).getContainers();
        }
        return Collections.emptyList();
    }
}