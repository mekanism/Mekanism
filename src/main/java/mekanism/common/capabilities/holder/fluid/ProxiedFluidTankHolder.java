package mekanism.common.capabilities.holder.fluid;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.holder.ProxiedHolder;
import net.minecraft.util.Direction;

public class ProxiedFluidTankHolder extends ProxiedHolder implements IFluidTankHolder {

    private final Function<Direction, List<IExtendedFluidTank>> tankFunction;

    public static ProxiedFluidTankHolder create(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IExtendedFluidTank>> tankFunction) {
        return new ProxiedFluidTankHolder(insertPredicate, extractPredicate, tankFunction);
    }

    private ProxiedFluidTankHolder(Predicate<Direction> insertPredicate, Predicate<Direction> extractPredicate,
          Function<Direction, List<IExtendedFluidTank>> tankFunction) {
        super(insertPredicate, extractPredicate);
        this.tankFunction = tankFunction;
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getTanks(@Nullable Direction side) {
        return tankFunction.apply(side);
    }
}