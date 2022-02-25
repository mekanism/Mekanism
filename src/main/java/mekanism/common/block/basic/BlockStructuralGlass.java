package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTileGlass;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.WrenchResult;
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

public class BlockStructuralGlass<TILE extends TileEntityStructuralMultiblock> extends BlockTileGlass<TILE, BlockTypeTile<TILE>> {

    public BlockStructuralGlass(BlockTypeTile<TILE> type) {
        super(type);
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        TileEntityStructuralMultiblock tile = WorldUtils.getTileEntity(TileEntityStructuralMultiblock.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            if (!MekanismUtils.canUseAsWrench(player.getItemInHand(hand))) {
                if (!tile.structuralGuiAccessAllowed() || !tile.hasFormedMultiblock()) {
                    //If the block's multiblock doesn't allow gui access via structural multiblocks (for example the evaporation plant),
                    // or if the multiblock is not formed then pass
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.SUCCESS;
        } else if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return InteractionResult.SUCCESS;
        }
        return tile.onActivate(player, hand, player.getItemInHand(hand));
    }
}
