package mekanism.common.block.prefab;

import javax.annotation.Nonnull;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockBasicMultiblock<TILE extends TileEntityMekanism> extends BlockTile<TILE, BlockTypeTile<TILE>> {

    public BlockBasicMultiblock(BlockTypeTile<TILE> type) {
        this(type, BlockBehaviour.Properties.of(Material.METAL).strength(5, 9).requiresCorrectToolForDrops());
    }

    public BlockBasicMultiblock(BlockTypeTile<TILE> type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            if (!MekanismUtils.canUseAsWrench(player.getItemInHand(hand))) {
                if (!tile.hasGui() || !tile.getMultiblock().isFormed()) {
                    //If the block doesn't have a gui (frames of things like the evaporation plant), or the multiblock is not formed then pass
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