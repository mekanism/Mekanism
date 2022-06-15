package mekanism.common.integration.lookingat.jade;

import mekanism.common.integration.lookingat.HwylaLookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IServerDataProvider;

public class JadeDataProvider implements IServerDataProvider<BlockEntity> {

    static final JadeDataProvider INSTANCE = new JadeDataProvider();

    @Override
    public ResourceLocation getUid() {
        return JadeConstants.BLOCK_DATA;
    }

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity tile, boolean showDetails) {
        if (tile instanceof TileEntityBoundingBlock boundingBlock) {
            //If we are a bounding block that has a position set, redirect the check to the main location
            if (!boundingBlock.hasReceivedCoords() || tile.getBlockPos().equals(boundingBlock.getMainPos())) {
                //If the coords haven't been received, exit
                return;
            }
            tile = WorldUtils.getTileEntity(world, boundingBlock.getMainPos());
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