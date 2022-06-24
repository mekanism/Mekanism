package mekanism.common.block.basic;

import mekanism.api.MekanismAPI;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogisticalSorter extends BlockTileModel<TileEntityLogisticalSorter, Machine<TileEntityLogisticalSorter>> {

    public BlockLogisticalSorter() {
        super(MekanismBlockTypes.LOGISTICAL_SORTER);
    }

    @Override
    public void setTileData(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, @NotNull TileEntityMekanism tile) {
        if (tile instanceof TileEntityLogisticalSorter sorter && !sorter.hasConnectedInventory()) {
            BlockPos tilePos = tile.getBlockPos();
            for (Direction dir : EnumUtils.DIRECTIONS) {
                BlockEntity tileEntity = WorldUtils.getTileEntity(world, tilePos.relative(dir));
                if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                    sorter.setFacing(dir.getOpposite());
                    break;
                }
            }
        }
    }

    @NotNull
    @Override
    @Deprecated
    public InteractionResult use(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
          @NotNull BlockHitResult hit) {
        TileEntityLogisticalSorter tile = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return genericClientActivated(player, hand);
        }
        //TODO: Make this be moved into the logistical sorter tile
        ItemStack stack = player.getItemInHand(hand);
        if (MekanismUtils.canUseAsWrench(stack)) {
            if (!MekanismAPI.getSecurityUtils().canAccessOrDisplayError(player, tile)) {
                return InteractionResult.FAIL;
            }
            if (player.isShiftKeyDown()) {
                WorldUtils.dismantleBlock(state, world, pos);
                return InteractionResult.SUCCESS;
            }
            Direction change = tile.getDirection().getClockWise();
            if (!tile.hasConnectedInventory()) {
                for (Direction dir : EnumUtils.DIRECTIONS) {
                    BlockEntity tileEntity = WorldUtils.getTileEntity(world, pos.relative(dir));
                    if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                        change = dir.getOpposite();
                        break;
                    }
                }
            }
            tile.setFacing(change);
            world.updateNeighborsAt(pos, this);
            return InteractionResult.SUCCESS;
        }
        return tile.openGui(player);
    }

    @NotNull
    @Override
    @Deprecated
    public BlockState updateShape(BlockState state, @NotNull Direction dir, @NotNull BlockState facingState, @NotNull LevelAccessor world, @NotNull BlockPos pos,
          @NotNull BlockPos neighborPos) {
        if (!world.isClientSide()) {
            TileEntityLogisticalSorter sorter = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
            if (sorter != null && !sorter.hasConnectedInventory()) {
                BlockEntity tileEntity = WorldUtils.getTileEntity(world, neighborPos);
                if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                    sorter.setFacing(dir.getOpposite());
                    state = sorter.getBlockState();
                }
            }
        }
        return super.updateShape(state, dir, facingState, world, pos, neighborPos);
    }
}