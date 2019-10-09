package mekanism.common.capabilities.basic;

import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultBlockableConnection implements IBlockableConnection {

    public static void register() {
        CapabilityManager.INSTANCE.register(IBlockableConnection.class, new NullStorage<>(), DefaultBlockableConnection::new);
    }

    @Override
    public boolean canConnectMutual(Direction side) {
        return false;
    }

    @Override
    public boolean canConnect(Direction side) {
        return false;
    }
}