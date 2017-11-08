package mekanism.common.capabilities;

import io.netty.buffer.ByteBuf;

import mekanism.common.base.ITileNetwork;
import mekanism.common.base.TileNetworkList;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultTileNetwork implements ITileNetwork
{
	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception {}

	@Override
	public TileNetworkList getNetworkedData(TileNetworkList data)
	{
		return data;
	}
	
	public static void register()
	{
        CapabilityManager.INSTANCE.register(ITileNetwork.class, new NullStorage<>(), DefaultTileNetwork.class);
	}
}
