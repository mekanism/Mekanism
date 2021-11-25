package mekanism.generators.common.block.turbine;

import javax.annotation.Nonnull;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.item.ItemTurbineBlade;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockTurbineRotor extends BlockTileModel<TileEntityTurbineRotor, BlockTypeTile<TileEntityTurbineRotor>> {

    private static final VoxelShape bounds = box(6, 0, 6, 10, 16, 10);

    public BlockTurbineRotor() {
        super(GeneratorsBlockTypes.TURBINE_ROTOR);
    }

    @Override
    @Deprecated
    public void onRemove(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
        if (!world.isClientSide && state.hasTileEntity() && (!state.is(newState.getBlock()) || !newState.hasTileEntity())) {
            TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos);
            if (tile != null) {
                int amount = tile.getHousedBlades();
                if (amount > 0) {
                    popResource(world, pos, GeneratorsItems.TURBINE_BLADE.getItemStack(amount));
                }
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType use(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        } else if (world.isClientSide) {
            return genericClientActivated(player, hand);
        } else if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
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
                    player.inventory.setChanged();
                }
            }
        } else if (stack.getItem() instanceof ItemTurbineBlade) {
            if (stack.getCount() < stack.getMaxStackSize()) {
                if (tile.removeBlade()) {
                    if (!player.isCreative()) {
                        stack.grow(1);
                        player.inventory.setChanged();
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        return bounds;
    }
}