package mekanism.common.capabilities.basic;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultGasHandler implements IGasHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IGasHandler.class, new NullStorage<>(), DefaultGasHandler::new);
    }

    @Override
    public GasStack getStack() {
        return GasStack.EMPTY;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public int fill(GasStack stack, Action action) {
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drain(GasStack stack, Action action) {
        return GasStack.EMPTY;
    }

    @Nonnull
    @Override
    public GasStack drain(int amount, Action action) {
        return GasStack.EMPTY;
    }
}