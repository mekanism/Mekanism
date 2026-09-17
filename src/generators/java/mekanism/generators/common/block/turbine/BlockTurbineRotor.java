package mekanism.generators.common.block.turbine;

import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.item.ItemTurbineBlade;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class BlockTurbineRotor extends BlockTileModel<TileEntityTurbineRotor, BlockTypeTile<TileEntityTurbineRotor>> {

    public BlockTurbineRotor(BlockBehaviour.Properties properties) {
        super(GeneratorsBlockTypes.TURBINE_ROTOR, defaultProperties(properties).mapColor(MapColor.COLOR_GRAY));
    }

    /// Similar to vanilla's implementation of [net.minecraft.world.level.block.ChiseledBookShelfBlock#useItemOn]
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ItemTurbineBlade) {
            if (!player.isShiftKeyDown()) {
                TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos);
                if (tile == null) {
                    //No tile, we can just skip trying to use without an item
                    return InteractionResult.PASS;
                } else if (!world.isClientSide() && tile.addBlade(world, true)) {
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            } else if (stack.count() < stack.getMaxStackSize()) {
                //If holding shift and not a full stack, allow it to try removing them
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    /// Similar to vanilla's implementation of [net.minecraft.world.level.block.ChiseledBookShelfBlock#useWithoutItem]
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, level, pos);
        if (tile != null && tile.getHousedBlades() > 0) {
            if (!level.isClientSide() && tile.removeBlade(level)) {
                ItemStack stack = GeneratorsItems.TURBINE_BLADE.asStack();
                if (!player.addItem(stack)) {
                    player.drop(stack, false, Prediction.SERVER_ONLY);
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}