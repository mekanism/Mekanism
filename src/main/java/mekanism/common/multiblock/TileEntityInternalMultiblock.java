package mekanism.common.multiblock;

import mekanism.api.TileNetworkList;
import mekanism.common.PacketHandler;
import mekanism.common.base.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.network.PacketBuffer;
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
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            if (dataStream.readBoolean()) {
                multiblockUUID = dataStream.readString();
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