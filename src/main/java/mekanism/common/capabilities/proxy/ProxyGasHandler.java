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
public class ProxyGasHandler implements IGasHandler {

    private final ISidedGasHandler gasHandler;
    @Nullable
    private final Direction side;
    private final boolean readOnly;
    private final boolean readOnlyInsert;
    private final boolean readOnlyExtract;

    //TODO: Should this take a supplier for the gas handler in case it somehow gets invalidated??
    public ProxyGasHandler(ISidedGasHandler gasHandler, @Nullable Direction side, @Nullable IHolder holder) {
        this.gasHandler = gasHandler;
        this.side = side;
        this.readOnly = this.side == null;
        this.readOnlyInsert = holder != null && !holder.canInsert(side);
        this.readOnlyExtract = holder != null && !holder.canExtract(side);
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
        return readOnly || readOnlyInsert ? stack : gasHandler.insertGas(tank, stack, side, action);
    }

    @Override
    public GasStack extractGas(int tank, int amount, Action action) {
        return readOnly || readOnlyExtract ? GasStack.EMPTY : gasHandler.extractGas(tank, amount, side, action);
    }

    @Override
    public GasStack insertGas(GasStack stack, Action action) {
        return readOnly || readOnlyInsert ? stack : gasHandler.insertGas(stack, side, action);
    }

    @Override
    public GasStack extractGas(int amount, Action action) {
        return readOnly || readOnlyExtract ? GasStack.EMPTY : gasHandler.extractGas(amount, side, action);
    }

    @Override
    public GasStack extractGas(GasStack stack, Action action) {
        return readOnly || readOnlyExtract ? GasStack.EMPTY : gasHandler.extractGas(stack, side, action);
    }
}