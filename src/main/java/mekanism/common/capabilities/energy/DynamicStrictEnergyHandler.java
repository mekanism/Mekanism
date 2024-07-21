package mekanism.common.capabilities.energy;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DynamicStrictEnergyHandler extends DynamicHandler<IEnergyContainer> implements IMekanismStrictEnergyHandler {

    public DynamicStrictEnergyHandler(Function<Direction, List<IEnergyContainer>> tankSupplier, Predicate<@Nullable Direction> canExtract,
          Predicate<@Nullable Direction> canInsert, @Nullable IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return containerSupplier.apply(side);
    }

    @Override
    public long insertEnergy(int container, long amount, @Nullable Direction side, Action action) {
        //If we can insert into the specific side, try to. Otherwise exit
        return canInsert.test(side) ? IMekanismStrictEnergyHandler.super.insertEnergy(container, amount, side, action) : amount;
    }

    @Override
    public long extractEnergy(int container, long amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific side, try to. Otherwise exit
        return canExtract.test(side) ? IMekanismStrictEnergyHandler.super.extractEnergy(container, amount, side, action) : 0L;
    }

    @Override
    public long insertEnergy(long amount, @Nullable Direction side, Action action) {
        //If we can insert into the specific side, try to. Otherwise exit
        return canInsert.test(side) ? IMekanismStrictEnergyHandler.super.insertEnergy(amount, side, action) : amount;
    }

    @Override
    public long extractEnergy(long amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific side, try to. Otherwise exit
        return canExtract.test(side) ? IMekanismStrictEnergyHandler.super.extractEnergy(amount, side, action) : 0L;
    }
}