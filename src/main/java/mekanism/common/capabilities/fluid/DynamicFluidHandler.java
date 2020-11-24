package mekanism.common.capabilities.fluid;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DynamicFluidHandler extends DynamicHandler<IExtendedFluidTank> implements IMekanismFluidHandler {

    public DynamicFluidHandler(Function<Direction, List<IExtendedFluidTank>> tankSupplier, InteractPredicate canExtract, InteractPredicate canInsert,
          @Nullable IContentsListener listener) {
        super(tankSupplier, canExtract, canInsert, listener);
    }

    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return containerSupplier.apply(side);
    }

    @Override
    public FluidStack insertFluid(int tank, FluidStack stack, @Nullable Direction side, Action action) {
        //If we can insert into the specific tank from that side, try to. Otherwise exit
        return canInsert.test(tank, side) ? IMekanismFluidHandler.super.insertFluid(tank, stack, side, action) : stack;
    }

    @Override
    public FluidStack extractFluid(int tank, int amount, @Nullable Direction side, Action action) {
        //If we can extract from a specific tank from a given side, try to. Otherwise exit
        return canExtract.test(tank, side) ? IMekanismFluidHandler.super.extractFluid(tank, amount, side, action) : FluidStack.EMPTY;
    }
}