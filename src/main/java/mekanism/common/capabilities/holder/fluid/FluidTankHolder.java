package mekanism.common.capabilities.holder.fluid;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.util.Direction;

public class FluidTankHolder extends BasicHolder<IExtendedFluidTank> implements IFluidTankHolder {

    FluidTankHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addTank(@Nonnull IExtendedFluidTank tank, RelativeSide... sides) {
        addSlotInternal(tank, sides);
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction);
    }
}