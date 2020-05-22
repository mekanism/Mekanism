package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.ISidedPigmentHandler;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyPigmentHandler extends ProxyHandler implements IPigmentHandler {

    private final ISidedPigmentHandler pigmentHandler;

    public ProxyPigmentHandler(ISidedPigmentHandler pigmentHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.pigmentHandler = pigmentHandler;
    }

    @Override
    public int getPigmentTankCount() {
        return pigmentHandler.getPigmentTankCount(side);
    }

    @Override
    public PigmentStack getPigmentInTank(int tank) {
        return pigmentHandler.getPigmentInTank(tank, side);
    }

    @Override
    public void setPigmentInTank(int tank, PigmentStack stack) {
        if (!readOnly) {
            pigmentHandler.setPigmentInTank(tank, stack, side);
        }
    }

    @Override
    public long getPigmentTankCapacity(int tank) {
        return pigmentHandler.getPigmentTankCapacity(tank, side);
    }

    @Override
    public boolean isPigmentValid(int tank, PigmentStack stack) {
        return !readOnly || pigmentHandler.isPigmentValid(tank, stack, side);
    }

    @Override
    public PigmentStack insertPigment(int tank, PigmentStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : pigmentHandler.insertPigment(tank, stack, side, action);
    }

    @Override
    public PigmentStack extractPigment(int tank, long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? PigmentStack.EMPTY : pigmentHandler.extractPigment(tank, amount, side, action);
    }

    @Override
    public PigmentStack insertPigment(PigmentStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : pigmentHandler.insertPigment(stack, side, action);
    }

    @Override
    public PigmentStack extractPigment(long amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? PigmentStack.EMPTY : pigmentHandler.extractPigment(amount, side, action);
    }

    @Override
    public PigmentStack extractPigment(PigmentStack stack, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? PigmentStack.EMPTY : pigmentHandler.extractPigment(stack, side, action);
    }
}