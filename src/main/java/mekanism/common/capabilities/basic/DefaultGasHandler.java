package mekanism.common.capabilities.basic;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultGasHandler implements IGasHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IGasHandler.class, new NullStorage<>(), DefaultGasHandler::new);
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return IGasHandler.NONE;
    }
}