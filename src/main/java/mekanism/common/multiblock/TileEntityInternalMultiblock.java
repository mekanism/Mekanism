package mekanism.common.multiblock;

import io.netty.buffer.ByteBuf;
import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.base.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityInternalMultiblock extends TileEntityMekanism {

    public String multiblockUUID;

    public TileEntityInternalMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void onUpdate() {
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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