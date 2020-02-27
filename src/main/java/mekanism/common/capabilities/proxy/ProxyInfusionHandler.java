package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.infuse.IInfusionHandler;
import mekanism.api.infuse.ISidedInfusionHandler;
import mekanism.api.infuse.InfusionStack;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyInfusionHandler implements IInfusionHandler {

    private final ISidedInfusionHandler infusionHandler;
    @Nullable
    private final Direction side;
    private final boolean readOnly;

    //TODO: Should this take a supplier for the item handler in case it somehow gets invalidated??
    public ProxyInfusionHandler(ISidedInfusionHandler infusionHandler, @Nullable Direction side) {
        this.infusionHandler = infusionHandler;
        this.side = side;
        this.readOnly = this.side == null;
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
    public int getInfusionTankCapacity(int tank) {
        return infusionHandler.getInfusionTankCapacity(tank, side);
    }

    @Override
    public boolean isInfusionValid(int tank, InfusionStack stack) {
        return !readOnly || infusionHandler.isInfusionValid(tank, stack, side);
    }

    @Override
    public InfusionStack insertInfusion(int tank, InfusionStack stack, Action action) {
        if (readOnly) {
            return stack;
        }
        return infusionHandler.insertInfusion(tank, stack, side, action);
    }

    @Override
    public InfusionStack extractInfusion(int tank, int amount, Action action) {
        if (readOnly) {
            return InfusionStack.EMPTY;
        }
        return infusionHandler.extractInfusion(tank, amount, side, action);
    }
}