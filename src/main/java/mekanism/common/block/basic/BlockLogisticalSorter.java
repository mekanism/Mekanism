package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.IMekWrench;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockLogisticalSorter extends BlockTileModel<TileEntityLogisticalSorter, Machine<TileEntityLogisticalSorter>> {

    public BlockLogisticalSorter() {
        super(MekanismBlockTypes.LOGISTICAL_SORTER);
    }

    //TODO: updatePostPlacement?? for rotating to a block if not attached to any container yet

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityLogisticalSorter) {
            TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter) tile;
            if (!transporter.hasConnectedInventory()) {
                BlockPos tilePos = tile.getPos();
                for (Direction dir : EnumUtils.DIRECTIONS) {
                    TileEntity tileEntity = MekanismUtils.getTileEntity(world, tilePos.offset(dir));
                    if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                        transporter.setFacing(dir.getOpposite());
                        break;
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        //TODO: Make this be moved into the logistical sorter tile
        TileEntityLogisticalSorter tile = MekanismUtils.getTileEntity(TileEntityLogisticalSorter.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            IMekWrench wrenchHandler = MekanismUtils.getWrench(stack);
            if (wrenchHandler != null) {
                if (wrenchHandler.canUseWrench(stack, player, hit.getPos())) {
                    if (SecurityUtils.canAccess(player, tile)) {
                        if (player.isSneaking()) {
                            MekanismUtils.dismantleBlock(state, world, pos);
                            return ActionResultType.SUCCESS;
                        }
                        Direction change = tile.getDirection().rotateY();
                        if (!tile.hasConnectedInventory()) {
                            for (Direction dir : EnumUtils.DIRECTIONS) {
                                TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos.offset(dir));
                                if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                                    change = dir.getOpposite();
                                    break;
                                }
                            }
                        }
                        tile.setFacing(change);
                        world.notifyNeighborsOfStateChange(pos, this);
                    } else {
                        SecurityUtils.displayNoAccess(player);
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return tile.openGui(player);
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tile).onNeighborChange(neighborBlock);
            }
            if (tile instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) tile;
                if (!sorter.hasConnectedInventory()) {
                    for (Direction dir : EnumUtils.DIRECTIONS) {
                        TileEntity tileEntity = MekanismUtils.getTileEntity(world, pos.offset(dir));
                        if (InventoryUtils.isItemHandler(tileEntity, dir)) {
                            sorter.setFacing(dir.getOpposite());
                            return;
                        }
                    }
                }
            }
        }
    }
}