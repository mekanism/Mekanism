package mekanism.common.capabilities.holder.fluid;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import net.minecraft.util.Direction;

public class ConfigFluidTankHolder extends ConfigHolder<IExtendedFluidTank> implements IFluidTankHolder {

    public ConfigFluidTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addTank(@Nonnull IExtendedFluidTank tank) {
        slots.add(tank);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.FLUID;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> slotInfo instanceof FluidSlotInfo ? ((FluidSlotInfo) slotInfo).getTanks() : Collections.emptyList());
    }
}