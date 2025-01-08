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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockTurbineRotor extends BlockTileModel<TileEntityTurbineRotor, BlockTypeTile<TileEntityTurbineRotor>> {

    public BlockTurbineRotor() {
        super(GeneratorsBlockTypes.TURBINE_ROTOR, properties -> properties.mapColor(MapColor.COLOR_GRAY));
    }

    @NotNull
    @Override
    @Deprecated
    public InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
          @NotNull BlockHitResult hit) {
        TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return genericClientActivated(player, hand, tile);
        }
        InteractionResult wrenchResult = tile.tryWrench(state, player, hand, hit).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemTurbineBlade) {
                if (tile.addBlade(true)) {
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                }
            }
        } else if (stack.isEmpty()) {
            if (tile.removeBlade()) {
                if (!player.isCreative()) {
                    player.setItemInHand(hand, GeneratorsItems.TURBINE_BLADE.getItemStack());
                    player.getInventory().setChanged();
                }
            }
        } else if (stack.getItem() instanceof ItemTurbineBlade) {
            if (stack.getCount() < stack.getMaxStackSize()) {
                if (tile.removeBlade()) {
                    if (!player.isCreative()) {
                        stack.grow(1);
                        player.getInventory().setChanged();
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}