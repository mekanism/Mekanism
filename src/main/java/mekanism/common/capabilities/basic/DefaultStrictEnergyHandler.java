package mekanism.common.capabilities.basic;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.basic.DefaultStorageHelper.DefaultStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

@ParametersAreNonnullByDefault
public class DefaultStrictEnergyHandler implements IStrictEnergyHandler {

    public static void register() {
        CapabilityManager.INSTANCE.register(IStrictEnergyHandler.class, new DefaultStorage<>(), DefaultStrictEnergyHandler::new);
    }

    @Override
    public int getEnergyContainerCount() {
        return 1;
    }

    @Override
    public double getEnergy(int container) {
        return 0;
    }

    @Override
    public void setEnergy(int container, double energy) {
    }

    @Override
    public double getMaxEnergy(int container) {
        return 0;
    }

    @Override
    public double getNeededEnergy(int container) {
        return 0;
    }

    @Override
    public double insertEnergy(int container, double amount, Action action) {
        return amount;
    }

    @Override
    public double extractEnergy(int container, double amount, Action action) {
        return 0;
    }
}