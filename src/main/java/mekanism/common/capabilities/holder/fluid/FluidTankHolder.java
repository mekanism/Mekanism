package mekanism.common.capabilities.holder.fluid;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IFluidTank;
import mekanism.common.capabilities.holder.BasicHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTankHolder extends BasicHolder<IFluidTank> implements IFluidTankHolder {

    FluidTankHolder(Supplier<Direction> facingSupplier) {
        super(facingSupplier);
    }

    void addTank(@NotNull IFluidTank tank, RelativeSide... sides) {
        addSlotInternal(tank, sides);
    }

    @NotNull
    @Override
    public List<IFluidTank> getTanks(@Nullable Direction direction) {
        return getSlots(direction);
    }
}