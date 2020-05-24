package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.slurry.ISidedSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxySlurryHandler extends ProxyHandler implements ISlurryHandler {

    private final ISidedSlurryHandler slurryHandler;

    public ProxySlurryHandler(ISidedSlurryHandler slurryHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.slurryHandler = slurryHandler;
    }

    @Override
    public int getSlurryTankCount() {
        return slurryHandler.getSlurryTankCount(side);
    }

    @Override
    public SlurryStack getSlurryInTank(int tank) {
        return slurryHandler.getSlurryInTank(tank, side);
    }

    @Override
    public void setSlurryInTank(int tank, SlurryStack stack) {
        if (!readOnly) {
            slurryHandler.setSlurryInTank(tank, stack, side);
        }
    }

    @Override
    public long getSlurryTankCapacity(int tank) {
        return slurryHandler.getSlurryTankCapacity(tank, side);
    }

    @Override
    public boolean isSlurryValid(int tank, SlurryStack stack) {
        return !readOnly || slurryHandler.isSlurryValid(tank, stack, side);
    }

    @Override
    public SlurryStack insertSlurry(int tank, SlurryStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : slurryHandler.insertSlurry(tank, stack, side, action);
    }

    @Override
    public SlurryStack extractSlurry(int tank, long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? SlurryStack.EMPTY : slurryHandler.extractSlurry(tank, amount, side, action);
    }

    @Override
    public SlurryStack insertSlurry(SlurryStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : slurryHandler.insertSlurry(stack, side, action);
    }

    @Override
    public SlurryStack extractSlurry(long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? SlurryStack.EMPTY : slurryHandler.extractSlurry(amount, side, action);
    }

    @Override
    public SlurryStack extractSlurry(SlurryStack stack, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? SlurryStack.EMPTY : slurryHandler.extractSlurry(stack, side, action);
    }
}