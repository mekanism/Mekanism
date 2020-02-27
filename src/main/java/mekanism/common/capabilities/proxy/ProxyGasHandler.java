package mekanism.common.capabilities.proxy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ISidedGasHandler;
import net.minecraft.util.Direction;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyGasHandler implements IGasHandler {

    private final ISidedGasHandler gasHandler;
    @Nullable
    private final Direction side;
    private final boolean readOnly;

    //TODO: Should this take a supplier for the gas handler in case it somehow gets invalidated??
    public ProxyGasHandler(ISidedGasHandler gasHandler, @Nullable Direction side) {
        this.gasHandler = gasHandler;
        this.side = side;
        this.readOnly = this.side == null;
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
        if (readOnly) {
            return stack;
        }
        return gasHandler.insertGas(tank, stack, side, action);
    }

    @Override
    public GasStack extractGas(int tank, int amount, Action action) {
        if (readOnly) {
            return GasStack.EMPTY;
        }
        return gasHandler.extractGas(tank, amount, side, action);
    }

    @Override
    public GasStack insertGas(GasStack stack, Action action) {
        if (readOnly) {
            return stack;
        }
        return gasHandler.insertGas(stack, side, action);
    }

    @Override
    public GasStack extractGas(int amount, Action action) {
        if (readOnly) {
            return GasStack.EMPTY;
        }
        return gasHandler.extractGas(amount, side, action);
    }

    @Override
    public GasStack extractGas(GasStack stack, Action action) {
        if (readOnly) {
            return GasStack.EMPTY;
        }
        return gasHandler.extractGas(stack, side, action);
    }
}