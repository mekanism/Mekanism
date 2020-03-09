package mekanism.common.multiblock;

import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.PacketHandler;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.network.PacketBuffer;

public class TileEntityInternalMultiblock extends TileEntityMekanism {

    //TODO: Make this actually be a UUID?
    public String multiblockUUID;

    public TileEntityInternalMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            if (dataStream.readBoolean()) {
                multiblockUUID = PacketHandler.readString(dataStream);
            } else {
                multiblockUUID = null;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        if (multiblockUUID != null) {
            data.add(true);
            data.add(multiblockUUID);
        } else {
            data.add(false);
        }
        return data;
    }

    public void setMultiblock(String id) {
        multiblockUUID = id;
    }

    public String getMultiblock() {
        return multiblockUUID;
    }
}