package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.basic.DefaultStorageHelper.DefaultStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DefaultStrictEnergyHandler implements IStrictEnergyHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IStrictEnergyHandler.class, new DefaultStorage<>(), DefaultStrictEnergyHandler::new);
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public FloatingLong getEnergy(int container) {
        return FloatingLong.ZERO;
    }

    @Override
    public void setEnergy(int container, FloatingLong energy) {
    }

    @Override
    public FloatingLong getMaxEnergy(int container) {
        return FloatingLong.ZERO;
    }

    @Override
    public FloatingLong getNeededEnergy(int container) {
        return FloatingLong.ZERO;
    }

    @Override
    public FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
        return amount;
    }

    @Override
    public FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
        return FloatingLong.ZERO;
    }
}