package mekanism.common.capabilities;

import mekanism.api.IConfigCardAccess;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultConfigCardAccess implements IConfigCardAccess {

    public static void register() {
        CapabilityManager.INSTANCE.register(IConfigCardAccess.class, new NullStorage<>(), DefaultConfigCardAccess::new);
    }
}
