package mekanism.common.capabilities;

import mekanism.api.TileNetworkList;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultTileNetwork implements ITileNetwork {

    public static void register() {
        CapabilityManager.INSTANCE.register(ITileNetwork.class, new NullStorage<>(), DefaultTileNetwork::new);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        return data;
    }
}