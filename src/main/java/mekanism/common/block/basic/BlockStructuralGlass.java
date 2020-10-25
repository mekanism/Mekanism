package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTileGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockStructuralGlass<TILE extends TileEntityStructuralMultiblock> extends BlockTileGlass<TILE, BlockTypeTile<TILE>> {

    public BlockStructuralGlass(BlockTypeTile<TILE> type) {
        super(type);
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        TileEntityStructuralMultiblock tile = WorldUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getItem() instanceof BlockItem && new BlockItemUseContext(player, hand, stack, hit).canPlace()) {
                if (!tile.structuralGuiAccessAllowed() || !tile.hasFormedMultiblock()) {
                    //If the block's multiblock doesn't allow gui access via structural multiblocks (for example the evaporation plant),
                    // or if the multiblock is not formed then pass
                    return ActionResultType.PASS;
                }
            }
            return ActionResultType.SUCCESS;
        }
        return tile.onActivate(player, hand, player.getHeldItem(hand));
    }
}
