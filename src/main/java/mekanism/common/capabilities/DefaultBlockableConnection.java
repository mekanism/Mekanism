package mekanism.common.capabilities;

import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultBlockableConnection implements IBlockableConnection {

    public static void register() {
        CapabilityManager.INSTANCE
              .register(IBlockableConnection.class, new NullStorage<>(), DefaultBlockableConnection::new);
    }

    @Override
    public boolean canConnectMutual(EnumFacing side) {
        return false;
    }

    @Override
    public boolean canConnect(EnumFacing side) {
        return false;
    }
}
