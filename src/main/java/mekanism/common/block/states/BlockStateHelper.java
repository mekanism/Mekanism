package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

//TODO: Set default state for different blocks if the default is not ideal
public class BlockStateHelper {

    public static final DirectionProperty facingProperty = BlockStateProperties.FACING;
    public static final DirectionProperty horizontalFacingProperty = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty activeProperty = BooleanProperty.create("active");
    //Cardboard Box storage
    public static final BooleanProperty storageProperty = BooleanProperty.create("storage");
    //Water Logged: TODO should we add some generic fluid logging property? Evaluate once fluids are in forge again
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static BlockState getDefaultState(@Nonnull BlockState state) {
        Block block = state.getBlock();
        if (block instanceof IStateActive) {
            //Default things to not being active
            state = state.with(activeProperty, false);
        }
        if (block instanceof IStateWaterLogged) {
            //Default the blocks to not being waterlogged, they have code to force waterlogging to true if being placed in water
            state = state.with(WATERLOGGED, false);
        }
        return state;
    }

    public static void fillBlockStateContainer(Block block, StateContainer.Builder<Block, BlockState> builder) {
        List<IProperty<?>> properties = new ArrayList<>();
        if (block instanceof IStateFacing) {
            properties.add(((IStateFacing) block).getFacingProperty());
        }
        if (block instanceof IStateActive) {
            properties.add(activeProperty);
        }
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateWaterLogged) {
            properties.add(WATERLOGGED);
        }
        if (!properties.isEmpty()) {
            builder.add(properties.toArray(new IProperty[0]));
        }
    }

    @Nullable
    public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, BlockItemUseContext context) {
        if (state == null) {
            return null;
        }
        //TODO: I don't know if there is a tile entity yet so this stuff may not really matter
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (block instanceof IStateFacing) {
            IStateFacing blockFacing = (IStateFacing) block;
            //TODO: Somehow weight this stuff towards context.getFace(), so that it has a higher likelihood of going with the face that was clicked on
            Direction newDirection = Direction.SOUTH;
            if (blockFacing.supportsDirection(Direction.DOWN) && blockFacing.supportsDirection(Direction.UP)) {
                PlayerEntity player = context.getPlayer();
                float rotationPitch = player == null ? 0 : player.rotationPitch;
                int height = Math.round(rotationPitch);
                if (height >= 65) {
                    newDirection = Direction.UP;
                } else if (height <= -65) {
                    newDirection = Direction.DOWN;
                }
            }
            if (newDirection != Direction.DOWN && newDirection != Direction.UP) {
                //TODO: Can this just use newDirection = context.getPlacementHorizontalFacing().getOpposite(); or is that not accurate
                int side = MathHelper.floor((context.getPlacementYaw() * 4.0F / 360.0F) + 0.5D) & 3;
                switch (side) {
                    case 0:
                        newDirection = Direction.NORTH;
                        break;
                    case 1:
                        newDirection = Direction.EAST;
                        break;
                    case 2:
                        newDirection = Direction.SOUTH;
                        break;
                    case 3:
                        newDirection = Direction.WEST;
                        break;
                }
            }
            state = blockFacing.setDirection(state, newDirection);
        }
        if (block instanceof IStateWaterLogged) {
            IFluidState fluidState = context.getWorld().getFluidState(context.getPos());
            state = state.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }
        //TODO: Set the proper defaults for the below ones, maybe do it by setting property defaults of everything
        // Also ensure that when the state changes from the tile the state is actually updated
        /*if (block instanceof IStateActive) {
            //TODO: False by default??
            state = state.with(activeProperty, ((IStateActive) block).isActive(tile));
        }
        if (block instanceof IStateStorage) {
            //TODO: Do this based on if something is getting boxed up
            state = state.with(storageProperty, isStoring(tile));
        }*/
        return state;
    }

    public static BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rotation) {
        //TODO: use the world and pos to check that it still fits (used for multiblocks like digital miner)
        return rotate(state, rotation);
    }

    public static BlockState rotate(BlockState state, Rotation rotation) {
        Block block = state.getBlock();
        if (block instanceof IStateFacing) {
            IStateFacing blockFacing = (IStateFacing) block;
            return rotate(blockFacing, blockFacing.getFacingProperty(), state, rotation);
        }
        return state;
    }

    public static BlockState mirror(BlockState state, Mirror mirror) {
        Block block = state.getBlock();
        if (block instanceof IStateFacing) {
            IStateFacing blockFacing = (IStateFacing) block;
            DirectionProperty property = blockFacing.getFacingProperty();
            return rotate(blockFacing, property, state, mirror.toRotation(state.get(property)));
        }
        return state;
    }

    private static BlockState rotate(IStateFacing blockFacing, DirectionProperty property, BlockState state, Rotation rotation) {
        return blockFacing.setDirection(state, rotation.rotate(state.get(property)));
    }

    private static boolean isStoring(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntityCardboardBox) {
            return ((TileEntityCardboardBox) tile).storedData != null;
        }
        return false;
    }

    @Nonnull
    private static ConnectionType getStateConnection(@Nonnull TileEntity tile, @Nonnull Direction side) {
        if (tile instanceof TileEntitySidedPipe) {
            return ((TileEntitySidedPipe) tile).getConnectionType(side);
        }
        return ConnectionType.NONE;
    }
}