package mekanism.common.capabilities.holder.fluid;

import java.util.ArrayList;
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

public class ConfigFluidTankHolder extends ConfigHolder implements IFluidTankHolder {

    protected final List<IExtendedFluidTank> tanks = new ArrayList<>();

    public ConfigFluidTankHolder(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        super(facingSupplier, configSupplier);
    }

    void addTank(@Nonnull IExtendedFluidTank tank) {
        tanks.add(tank);
    }

    @Override
    protected TransmissionType getTransmissionType() {
        return TransmissionType.FLUID;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction, tanks, slotInfo -> {
            if (slotInfo instanceof FluidSlotInfo && slotInfo.isEnabled()) {
                return ((FluidSlotInfo) slotInfo).getTanks();
            }
            return Collections.emptyList();
        });
    }
}