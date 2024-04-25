package mekanism.common.block.basic;

import mekanism.api.radiation.IRadiationManager;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogisticalSorter extends BlockTileModel<TileEntityLogisticalSorter, Machine<TileEntityLogisticalSorter>> {

    public BlockLogisticalSorter() {
        super(MekanismBlockTypes.LOGISTICAL_SORTER, properties -> properties.mapColor(BlockResourceInfo.STEEL.getMapColor()));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        Direction facing = Attribute.getFacing(state);
        if (facing == null) {
            //Should never be null but if it is for some reason just return the state we already found
            return state;
        }
        Direction oppositeDirection = facing.getOpposite();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        //Note: Check ItemHandler instead of acceptor as the back face cannot connect to transporters
        if (!InventoryUtils.isItemHandler(level, pos.relative(oppositeDirection), facing)) {
            for (Direction dir : EnumUtils.DIRECTIONS) {
                //Skip the side we already know is not a valid acceptor
                Direction opposite = dir.getOpposite();
                if (dir != oppositeDirection && InventoryUtils.isItemHandler(level, pos.relative(dir), opposite)) {
                    state = Attribute.setFacing(state, opposite);
                    break;
                }
            }
        }
        return state;
    }

    @NotNull
    @Override
    public ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        TileEntityLogisticalSorter tile = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
        if (tile == null) {
            //No tile, we can just skip trying to use without an item
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            return genericClientActivated(stack, tile);
        }
        //TODO: Make this be moved into the logistical sorter tile
        if (MekanismUtils.canUseAsWrench(stack)) {
            if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos, tile)) {
                return ItemInteractionResult.FAIL;
            }
            if (player.isShiftKeyDown()) {
                if (IRadiationManager.INSTANCE.isRadiationEnabled() && tile.getRadiationScale() > 0) {
                    //Note: This should always be false for the logistical sorter, but we keep it here for good measure
                    return ItemInteractionResult.FAIL;
                }
                WorldUtils.dismantleBlock(state, world, pos, player);
                return ItemInteractionResult.SUCCESS;
            }
            Direction change = tile.getDirection().getClockWise();
            if (!tile.hasConnectedInventory()) {
                for (Direction dir : EnumUtils.DIRECTIONS) {
                    Direction opposite = dir.getOpposite();
                    if (InventoryUtils.isItemHandler(world, pos.relative(dir), opposite)) {
                        change = opposite;
                        break;
                    }
                }
            }
            tile.setFacing(change);
            world.updateNeighborsAt(pos, this);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @NotNull
    @Override
    @Deprecated
    public BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos pos,
          @NotNull BlockPos neighborPos) {
        if (!world.isClientSide()) {
            TileEntityLogisticalSorter sorter = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
            Direction opposite = dir.getOpposite();
            if (sorter != null && !sorter.hasConnectedInventory() && InventoryUtils.isItemHandler(sorter.getLevel(), neighborPos, opposite)) {
                sorter.setFacing(opposite);
                state = sorter.getBlockState();
            }
        }
        return super.updateShape(state, dir, facingState, world, pos, neighborPos);
    }
}