package mekanism.common.capabilities.energy;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DynamicStrictEnergyHandler extends DynamicHandler<IEnergyContainer> implements IMekanismStrictEnergyHandler {

    public DynamicStrictEnergyHandler(Function<Direction, List<IEnergyContainer>> tankSupplier, InteractPredicate canExtract, InteractPredicate canInsert,
          @Nullable IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return containerSupplier.apply(side);
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
        //If we can insert into the specific tank from that side, try to. Otherwise exit
        return canInsert.test(container, side) ? IMekanismStrictEnergyHandler.super.insertEnergy(container, amount, side, action) : amount;
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific tank from a given side, try to. Otherwise exit
        return canExtract.test(container, side) ? IMekanismStrictEnergyHandler.super.extractEnergy(container, amount, side, action) : FloatingLong.ZERO;
    }
}