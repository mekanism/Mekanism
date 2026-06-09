package mekanism.common.block.basic;

import mekanism.common.block.prefab.BlockTileGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockStructuralGlass<TILE extends TileEntityStructuralMultiblock> extends BlockTileGlass<TILE, BlockTypeTile<TILE>> {

    public BlockStructuralGlass(BlockTypeTile<TILE> type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
          InteractionHand hand, BlockHitResult hit) {
        TileEntityStructuralMultiblock tile = WorldUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
        if (tile == null) {
            return InteractionResult.FAIL;
        } else if (world.isClientSide()) {
            if (!MekanismUtils.canUseAsWrench(stack) && !tile.structuralGuiAccessAllowed()) {
                //If the block's multiblock doesn't allow gui access via structural multiblocks (for example the evaporation plant),
                // or if the multiblock is not formed then pass
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            return InteractionResult.SUCCESS;
        }
        InteractionResult wrenchResult = tile.tryWrench(state, player, stack).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        }
        return tile.onActivate(player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        //We handle opening the gui via useItemOn
        return InteractionResult.PASS;
    }
}
