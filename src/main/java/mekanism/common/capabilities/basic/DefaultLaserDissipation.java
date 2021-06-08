package mekanism.common.capabilities.basic;

import mekanism.api.lasers.ILaserDissipation;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultLaserDissipation implements ILaserDissipation {

    public static void register() {
        CapabilityManager.INSTANCE.register(ILaserDissipation.class, new NullStorage<>(), DefaultLaserDissipation::new);
    }

    @Override
    public double getDissipationPercent() {
        return 0;
    }

    @Override
    public double getRefractionPercent() {
        return 0;
    }
}