package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WTHITDataProvider implements IDataProvider<BlockEntity> {

    static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

    @Override
    public void appendData(IDataWriter dataWriter, IServerAccessor<BlockEntity> serverAccessor, IPluginConfig config) {
        BlockEntity tile = serverAccessor.getTarget();
        if (tile instanceof TileEntityBoundingBlock boundingBlock) {
            //If we are a bounding block that has a position set, redirect the check to the main location
            if (!boundingBlock.hasReceivedCoords() || tile.getBlockPos().equals(boundingBlock.getMainPos())) {
                //If the coords haven't been received, exit
                return;
            }
            tile = WorldUtils.getTileEntity(serverAccessor.getWorld(), boundingBlock.getMainPos());
            if (tile == null) {
                //If there is no tile where the bounding block thinks the main tile is, exit
                return;
            }
        }
        WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
        LookingAtUtils.addInfo(helper, tile, true, true);
        dataWriter.add(WTHITLookingAtHelper.class, result -> result.add(helper));
    }
}