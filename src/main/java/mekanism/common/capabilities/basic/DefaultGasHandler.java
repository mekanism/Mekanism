package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultGasHandler implements IGasHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IGasHandler.class, new NullStorage<>(), DefaultGasHandler::new);
    }

    @Override
    public int getGasTankCount() {
        return 1;
    }

    @Override
    public GasStack getGasInTank(int tank) {
        return GasStack.EMPTY;
    }

    @Override
    public void setGasInTank(int tank, GasStack stack) {
    }

    @Override
    public long getGasTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        return true;
    }

    @Override
    public GasStack insertGas(int tank, GasStack stack, Action action) {
        return stack;
    }

    @Override
    public GasStack extractGas(int tank, long amount, Action action) {
        return GasStack.EMPTY;
    }
}