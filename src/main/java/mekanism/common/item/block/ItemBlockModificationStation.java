package mekanism.common.item.block;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class ItemBlockModificationStation extends ItemBlockMachine {

    public ItemBlockModificationStation(BlockTileModel<TileEntityModificationStation, BlockTypeTile<TileEntityModificationStation>> block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockPlaceContext context, @Nonnull BlockState state) {
        Direction side = MekanismUtils.getRight(Attribute.getFacing(state));
        BlockPos pos = context.getClickedPos();
        List<BlockPos> checkList = Arrays.asList(pos.above(), pos.relative(side), pos.above().relative(side));
        return WorldUtils.areBlocksValidAndReplaceable(context.getLevel(), checkList) && super.placeBlock(context, state);
    }
}