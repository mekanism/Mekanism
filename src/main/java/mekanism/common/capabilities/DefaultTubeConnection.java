package mekanism.common.capabilities;

import mekanism.api.gas.ITubeConnection;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

@Deprecated
public class DefaultTubeConnection implements ITubeConnection {

    public static void register() {
        CapabilityManager.INSTANCE.register(ITubeConnection.class, new NullStorage<>(), DefaultTubeConnection::new);
    }

    @Override
    public boolean canTubeConnect(EnumFacing side) {
        return false;
    }
}
