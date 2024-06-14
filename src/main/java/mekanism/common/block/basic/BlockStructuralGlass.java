package mekanism.common.block.basic;

import mekanism.common.block.prefab.BlockTileGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockStructuralGlass<TILE extends TileEntityStructuralMultiblock> extends BlockTileGlass<TILE, BlockTypeTile<TILE>> {

    public BlockStructuralGlass(BlockTypeTile<TILE> type) {
        super(type);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        TileEntityStructuralMultiblock tile = WorldUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
        if (tile == null) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            if (!MekanismUtils.canUseAsWrench(stack) && !tile.structuralGuiAccessAllowed()) {
                //If the block's multiblock doesn't allow gui access via structural multiblocks (for example the evaporation plant),
                // or if the multiblock is not formed then pass
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            return ItemInteractionResult.SUCCESS;
        }
        ItemInteractionResult wrenchResult = tile.tryWrench(state, player, stack).getInteractionResult();
        if (wrenchResult.result() != InteractionResult.PASS) {
            return wrenchResult;
        }
        return tile.onActivate(player, hand, stack);
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        //We handle opening the gui via useItemOn
        return InteractionResult.PASS;
    }
}
