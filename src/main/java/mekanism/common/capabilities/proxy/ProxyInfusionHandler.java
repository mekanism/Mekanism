package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.ISidedInfusionHandler;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyInfusionHandler extends ProxyHandler implements IInfusionHandler {

    private final ISidedInfusionHandler infusionHandler;

    public ProxyInfusionHandler(ISidedInfusionHandler infusionHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.infusionHandler = infusionHandler;
    }

    @Override
    public int getInfusionTankCount() {
        return infusionHandler.getInfusionTankCount(side);
    }

    @Override
    public InfusionStack getInfusionInTank(int tank) {
        return infusionHandler.getInfusionInTank(tank, side);
    }

    @Override
    public void setInfusionInTank(int tank, InfusionStack stack) {
        if (!readOnly) {
            infusionHandler.setInfusionInTank(tank, stack, side);
        }
    }

    @Override
    public long getInfusionTankCapacity(int tank) {
        return infusionHandler.getInfusionTankCapacity(tank, side);
    }

    @Override
    public boolean isInfusionValid(int tank, InfusionStack stack) {
        return !readOnly || infusionHandler.isInfusionValid(tank, stack, side);
    }

    @Override
    public InfusionStack insertInfusion(int tank, InfusionStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : infusionHandler.insertInfusion(tank, stack, side, action);
    }

    @Override
    public InfusionStack extractInfusion(int tank, long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? InfusionStack.EMPTY : infusionHandler.extractInfusion(tank, amount, side, action);
    }

    @Override
    public InfusionStack insertInfusion(InfusionStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : infusionHandler.insertInfusion(stack, side, action);
    }

    @Override
    public InfusionStack extractInfusion(long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? InfusionStack.EMPTY : infusionHandler.extractInfusion(amount, side, action);
    }

    @Override
    public InfusionStack extractInfusion(InfusionStack stack, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? InfusionStack.EMPTY : infusionHandler.extractInfusion(stack, side, action);
    }
}