package mekanism.common.capabilities.holder.fluid;

import java.util.Collections;
import java.util.List;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.capabilities.holder.ConfigHolder;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.config.slot.FluidSlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfigFluidTankHolder extends ConfigHolder<IFluidTank> implements IFluidTankHolder {

    public ConfigFluidTankHolder(ISideConfiguration sideConfiguration) {
        super(sideConfiguration);
    }

    void addTank(@NotNull IFluidTank tank) {
        slots.add(tank);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.FLUID;
    }

    @NotNull
    @Override
    public List<IFluidTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction, slotInfo -> slotInfo instanceof FluidSlotInfo info ? info.getTanks() : Collections.emptyList());
    }
}