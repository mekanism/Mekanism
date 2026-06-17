package mekanism.generators.common.block.turbine;

import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.item.ItemTurbineBlade;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class BlockTurbineRotor extends BlockTileModel<TileEntityTurbineRotor, BlockTypeTile<TileEntityTurbineRotor>> {

    public BlockTurbineRotor(BlockBehaviour.Properties properties) {
        super(GeneratorsBlockTypes.TURBINE_ROTOR, defaultProperties(properties).mapColor(MapColor.COLOR_GRAY));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
          InteractionHand hand, BlockHitResult hit) {
        TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos);
        if (tile == null) {
            //No tile, we can just skip trying to use without an item
            return InteractionResult.PASS;
        } else if (world.isClientSide()) {
            return genericClientActivated(world, stack, tile);
        }
        InteractionResult wrenchResult = tile.tryWrench(world, state, player, stack).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        }
        if (!player.isShiftKeyDown()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemTurbineBlade) {
                if (tile.addBlade(world, true)) {
                    stack.consume(1, player);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        } else if (stack.isEmpty()) {
            if (tile.removeBlade(world)) {
                if (!player.isCreative()) {
                    player.setItemInHand(hand, GeneratorsItems.TURBINE_BLADE.asStack());
                    //TODO - 26.2: I don't think this setChanged call or the one lower down are necessary anymore?
                    player.getInventory().setChanged();
                }
                return InteractionResult.SUCCESS_SERVER;
            }
        } else if (stack.getItem() instanceof ItemTurbineBlade) {
            if (stack.count() < stack.getMaxStackSize()) {
                if (tile.removeBlade(world)) {
                    if (!player.isCreative()) {
                        stack.grow(1);
                        player.getInventory().setChanged();
                    }
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}