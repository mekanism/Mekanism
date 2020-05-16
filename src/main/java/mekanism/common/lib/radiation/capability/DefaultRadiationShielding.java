package mekanism.common.lib.radiation.capability;

import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultRadiationShielding implements IRadiationShielding {

    public static void register() {
        CapabilityManager.INSTANCE.register(IRadiationShielding.class, new NullStorage<>(), DefaultRadiationShielding::new);
    }

    @Override
    public double getRadiationShielding() {
        return 0;
    }
}
