package mekanism.common.integration.lookingat.wthit;

import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mekanism.common.integration.lookingat.HwylaLookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WTHITDataProvider implements IServerDataProvider<BlockEntity> {

    static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

    @Override
    public void appendServerData(CompoundTag data, IServerAccessor<BlockEntity> serverAccessor, IPluginConfig config) {
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
        HwylaLookingAtHelper helper = new HwylaLookingAtHelper();
        LookingAtUtils.addInfo(helper, tile, true, true);
        //Add our data if we have any
        helper.finalizeData(data);
    }
}