package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.ISidedGasHandler;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyGasHandler extends ProxyHandler implements IGasHandler {

    private final ISidedGasHandler gasHandler;

    public ProxyGasHandler(ISidedGasHandler gasHandler, @Nullable Direction side, @Nullable IHolder holder) {
        super(side, holder);
        this.gasHandler = gasHandler;
    }

    @Override
    public int getGasTankCount() {
        return gasHandler.getGasTankCount(side);
    }

    @Override
    public GasStack getGasInTank(int tank) {
        return gasHandler.getGasInTank(tank, side);
    }

    @Override
    public void setGasInTank(int tank, GasStack stack) {
        if (!readOnly) {
            gasHandler.setGasInTank(tank, stack, side);
        }
    }

    @Override
    public int getGasTankCapacity(int tank) {
        return gasHandler.getGasTankCapacity(tank, side);
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        return !readOnly || gasHandler.isGasValid(tank, stack, side);
    }

    @Override
    public GasStack insertGas(int tank, GasStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : gasHandler.insertGas(tank, stack, side, action);
    }

    @Override
    public GasStack extractGas(int tank, int amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? GasStack.EMPTY : gasHandler.extractGas(tank, amount, side, action);
    }

    @Override
    public GasStack insertGas(GasStack stack, Action action) {
        return readOnly || readOnlyInsert.getAsBoolean() ? stack : gasHandler.insertGas(stack, side, action);
    }

    @Override
    public GasStack extractGas(int amount, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? GasStack.EMPTY : gasHandler.extractGas(amount, side, action);
    }

    @Override
    public GasStack extractGas(GasStack stack, Action action) {
        return readOnly || readOnlyExtract.getAsBoolean() ? GasStack.EMPTY : gasHandler.extractGas(stack, side, action);
    }
}