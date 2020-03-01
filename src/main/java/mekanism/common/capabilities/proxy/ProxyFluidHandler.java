package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.api.fluid.ISidedFluidHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyFluidHandler implements IExtendedFluidHandler {

    private final ISidedFluidHandler fluidHandler;
    @Nullable
    private final Direction side;
    private final boolean readOnly;
    private final boolean readOnlyInsert;
    private final boolean readOnlyExtract;

    //TODO: Should this take a supplier for the fluid handler in case it somehow gets invalidated??
    public ProxyFluidHandler(ISidedFluidHandler fluidHandler, @Nullable Direction side, @Nullable IHolder holder) {
        this.fluidHandler = fluidHandler;
        this.side = side;
        this.readOnly = this.side == null;
        this.readOnlyInsert = holder != null && !holder.canInsert(side);
        this.readOnlyExtract = holder != null && !holder.canExtract(side);
    }

    @Override
    public int getTanks() {
        return fluidHandler.getTanks(side);
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidHandler.getFluidInTank(tank, side);
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        if (!readOnly) {
            fluidHandler.setFluidInTank(tank, stack, side);
        }
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidHandler.getTankCapacity(tank, side);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return !readOnly || fluidHandler.isFluidValid(tank, stack, side);
    }

    @Override
    public FluidStack insertFluid(int tank, FluidStack stack, Action action) {
        return readOnly || readOnlyInsert ? stack : fluidHandler.insertFluid(tank, stack, side, action);
    }

    @Override
    public FluidStack extractFluid(int tank, int amount, Action action) {
        return readOnly || readOnlyExtract ? FluidStack.EMPTY : fluidHandler.extractFluid(tank, amount, side, action);
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, Action action) {
        return readOnly || readOnlyInsert ? stack : fluidHandler.insertFluid(stack, side, action);
    }

    @Override
    public FluidStack extractFluid(int amount, Action action) {
        return readOnly || readOnlyExtract ? FluidStack.EMPTY : fluidHandler.extractFluid(amount, side, action);
    }

    @Override
    public FluidStack extractFluid(FluidStack stack, Action action) {
        return readOnly || readOnlyExtract ? FluidStack.EMPTY : fluidHandler.extractFluid(stack, side, action);
    }
}