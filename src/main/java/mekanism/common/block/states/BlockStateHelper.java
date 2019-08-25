package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IColor;
import mekanism.api.text.EnumColor;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.property.PropertyConnection;
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
    //NOTE: This currently is only using the set of colors the transporter supports as it is the only thing that needs this
    // There is a method to create this supporting all colors but it is currently unused
    public static final PropertyColor colorProperty = PropertyColor.createTransporter("color");
    //Sided pipe properties
    public static final PropertyConnection downConnectionProperty = PropertyConnection.create("down");
    public static final PropertyConnection upConnectionProperty = PropertyConnection.create("up");
    public static final PropertyConnection northConnectionProperty = PropertyConnection.create("north");
    public static final PropertyConnection southConnectionProperty = PropertyConnection.create("south");
    public static final PropertyConnection westConnectionProperty = PropertyConnection.create("west");
    public static final PropertyConnection eastConnectionProperty = PropertyConnection.create("east");
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
        return state;
    }

    public static void fillBlockStateContainer(Block block, StateContainer.Builder<Block, BlockState> builder) {
        List<IProperty> properties = new ArrayList<>();
        if (block instanceof IStateFacing) {
            properties.add(((IStateFacing) block).getFacingProperty());
        }
        if (block instanceof IStateActive) {
            properties.add(activeProperty);
        }
        //TODO: Make color be an extended state
        /*if (block instanceof IStateColor) {
            properties.add(colorProperty);
        }*/
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateConnection) {
            properties.add(downConnectionProperty);
            properties.add(upConnectionProperty);
            properties.add(northConnectionProperty);
            properties.add(southConnectionProperty);
            properties.add(westConnectionProperty);
            properties.add(eastConnectionProperty);
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
            Direction newDirection = Direction.SOUTH;
            //TODO: Will context.getNearestLookingDirection() do what we need
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
        if (block instanceof IStateColor) {
            //TODO: move this to https://github.com/MinecraftForge/MinecraftForge/pull/5564
            state = state.with(BlockStateHelper.colorProperty, getColor(tile));
        }
        if (block instanceof IStateStorage) {
            //TODO: Do this based on if something is getting boxed up
            state = state.with(storageProperty, isStoring(tile));
        }
        if (block instanceof IStateConnection) {
            //TODO: Move this to https://github.com/MinecraftForge/MinecraftForge/pull/5564
            //Add all the different connection types
            state = state.with(downConnectionProperty, getStateConnection(tile, Direction.DOWN));
            state = state.with(upConnectionProperty, getStateConnection(tile, Direction.UP));
            state = state.with(northConnectionProperty, getStateConnection(tile, Direction.NORTH));
            state = state.with(southConnectionProperty, getStateConnection(tile, Direction.SOUTH));
            state = state.with(westConnectionProperty, getStateConnection(tile, Direction.WEST));
            state = state.with(eastConnectionProperty, getStateConnection(tile, Direction.EAST));
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

    @Nonnull
    private static IColor getColor(@Nonnull TileEntity tile) {
        EnumColor color = null;
        if (tile instanceof TileEntitySidedPipe) {
            color = ((TileEntitySidedPipe) tile).getRenderColor();
        }
        return color == null ? EnumColor.NONE : color;
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