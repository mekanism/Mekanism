package mekanism.common.capabilities.basic;

import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 30/04/16.
 */
public class DefaultCableOutputter implements IStrictEnergyOutputter {

    public static void register() {
        CapabilityManager.INSTANCE.register(IStrictEnergyOutputter.class, new NullStorage<>(), DefaultCableOutputter::new);
    }

    @Override
    public double pullEnergy(Direction side, double amount, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canOutputEnergy(Direction side) {
        return true;
    }
}