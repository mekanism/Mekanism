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
    @Deprecated
    public InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
          @NotNull BlockHitResult hit) {
        TileEntityStructuralMultiblock tile = WorldUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            if (!MekanismUtils.canUseAsWrench(player.getItemInHand(hand)) && !tile.structuralGuiAccessAllowed()) {
                //If the block's multiblock doesn't allow gui access via structural multiblocks (for example the evaporation plant),
                // or if the multiblock is not formed then pass
                return InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        InteractionResult wrenchResult = tile.tryWrench(state, player, hand, hit).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        }
        return tile.onActivate(player, hand, player.getItemInHand(hand));
    }
}
