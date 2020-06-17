package mekanism.common.item.block;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class ItemBlockModificationStation extends ItemBlockMachine {

    public ItemBlockModificationStation(BlockTileModel<TileEntityModificationStation, BlockTypeTile<TileEntityModificationStation>> block) {
        super(block);
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        List<BlockPos> checkList = new ArrayList<>();
        Direction side = MekanismUtils.getRight(Attribute.getFacing(state));
        checkList.add(context.getPos().up());
        checkList.add(context.getPos().offset(side));
        checkList.add(context.getPos().up().offset(side));
        if (checkList.stream().anyMatch(pos -> !MekanismUtils.isValidReplaceableBlock(context.getWorld(), pos))) {
            return false;
        }
        return super.placeBlock(context, state);
    }
}