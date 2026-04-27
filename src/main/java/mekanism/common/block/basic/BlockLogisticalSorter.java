package mekanism.common.block.basic;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockLogisticalSorter extends BlockTileModel<TileEntityLogisticalSorter, Machine<TileEntityLogisticalSorter>> {

    public BlockLogisticalSorter(BlockBehaviour.Properties properties) {
        super(MekanismBlockTypes.LOGISTICAL_SORTER, defaultProperties(properties).mapColor(BlockResourceInfo.STEEL.getMapColor()));
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
    protected BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess scheduledTickAccess, @NotNull BlockPos currentPos,
          @NotNull Direction facing, @NotNull BlockPos facingPos, @NotNull BlockState facingState, @NotNull RandomSource random) {
        if (!level.isClientSide()) {
            TileEntityLogisticalSorter sorter = WorldUtils.getTileEntity(TileEntityLogisticalSorter.class, level, currentPos);
            Direction opposite = facing.getOpposite();
            if (sorter != null && !sorter.hasConnectedInventory() && InventoryUtils.isItemHandler(sorter.getLevel(), facingPos, opposite)) {
                sorter.setFacing(opposite);
                state = sorter.getBlockState();
            }
        }
        return super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }
}