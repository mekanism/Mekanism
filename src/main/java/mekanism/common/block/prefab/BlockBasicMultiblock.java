package mekanism.common.block.prefab;

import java.util.function.UnaryOperator;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockBasicMultiblock<TILE extends TileEntityMekanism> extends BlockTile<TILE, BlockTypeTile<TILE>> {

    public BlockBasicMultiblock(BlockTypeTile<TILE> type, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        this(type, propertiesModifier.apply(BlockBehaviour.Properties.of().strength(5, 9).requiresCorrectToolForDrops()));
    }

    public BlockBasicMultiblock(BlockTypeTile<TILE> type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, pos);
        if (tile == null) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            if (!MekanismUtils.canUseAsWrench(player.getItemInHand(hand))) {
                if (!tile.hasGui() || !tile.getMultiblock().isFormed()) {
                    //If the block doesn't have a gui (frames of things like the evaporation plant), or the multiblock is not formed then pass
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
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