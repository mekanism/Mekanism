package mekanism.common.capabilities;

import io.netty.buffer.ByteBuf;
import mekanism.api.TileNetworkList;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultTileNetwork implements ITileNetwork {

    public static void register() {
        CapabilityManager.INSTANCE.register(ITileNetwork.class, new NullStorage<>(), DefaultTileNetwork::new);
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        return data;
    }
}
