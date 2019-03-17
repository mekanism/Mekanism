package mekanism.common.capabilities;

import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 30/04/16.
 */
public class DefaultCableOutputter implements IStrictEnergyOutputter {

    public static void register() {
        CapabilityManager.INSTANCE
              .register(IStrictEnergyOutputter.class, new NullStorage<>(), DefaultCableOutputter::new);
    }

    @Override
    public double pullEnergy(EnumFacing side, double amount, boolean simulate) {
        return 0;
    }

    @Override
    public boolean canOutputEnergy(EnumFacing side) {
        return true;
    }
}
