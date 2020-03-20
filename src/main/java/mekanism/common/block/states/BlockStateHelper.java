package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeStateFacing.FacePlacementType;
import mekanism.common.tile.TileEntityCardboardBox;
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
import org.jetbrains.annotations.Contract;

//TODO: Set default state for different blocks if the default is not ideal
public class BlockStateHelper {

    public static final DirectionProperty facingProperty = BlockStateProperties.FACING;
    public static final DirectionProperty horizontalFacingProperty = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty activeProperty = BooleanProperty.create("active");
    //Cardboard Box storage
    public static final BooleanProperty storageProperty = BooleanProperty.create("storage");
    //Fluid logged. TODO: We may eventually want to make this not be using the same exact property as WATERLOGGED but name it differently
    public static final BooleanProperty FLUID_LOGGED = BlockStateProperties.WATERLOGGED;

    public static BlockState getDefaultState(@Nonnull BlockState state) {
        Block block = state.getBlock();
        if (Attribute.has(block, AttributeStateActive.class)) {
            //Default things to not being active
            state = state.with(activeProperty, false);
        }
        if (block instanceof IStateFluidLoggable) {
            //Default the blocks to not being waterlogged, they have code to force waterlogging to true if being placed in water
            state = state.with(FLUID_LOGGED, false);
        }
        return state;
    }

    public static void fillBlockStateContainer(Block block, StateContainer.Builder<Block, BlockState> builder) {
        List<IProperty<?>> properties = new ArrayList<>();
        Attribute.ifHas(block, AttributeStateFacing.class, (attr) -> properties.add(attr.getFacingProperty()));

        if (Attribute.has(block, AttributeStateActive.class)) {
            properties.add(activeProperty);
        }
        if (block instanceof IStateStorage) {
            properties.add(storageProperty);
        }
        if (block instanceof IStateFluidLoggable) {
            properties.add(FLUID_LOGGED);
        }
        if (!properties.isEmpty()) {
            builder.add(properties.toArray(new IProperty[0]));
        }
    }

    @Contract("_, null, _ -> null")
    public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, BlockItemUseContext context) {
        return getStateForPlacement(block, state, context.getWorld(), context.getPos(), context.getPlayer(), context.getFace());
    }

    @Contract("_, null, _, _, _, _ -> null")
    public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, @Nonnull IWorld world, @Nonnull BlockPos pos, @Nullable PlayerEntity player, @Nonnull Direction face) {
        if (state == null) {
            return null;
        }
        if (Attribute.has(block, AttributeStateFacing.class)) {
            AttributeStateFacing blockFacing = Attribute.get(block, AttributeStateFacing.class);
            Direction newDirection = Direction.SOUTH;
            if (blockFacing.getPlacementType() == FacePlacementType.PLAYER_LOCATION) {
                //TODO: Somehow weight this stuff towards context.getFace(), so that it has a higher likelihood of going with the face that was clicked on
                if (blockFacing.supportsDirection(Direction.DOWN) && blockFacing.supportsDirection(Direction.UP)) {
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
                    float placementYaw = player == null ? 0 : player.rotationYaw;
                    int side = MathHelper.floor((placementYaw * 4.0F / 360.0F) + 0.5D) & 3;
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

            } else {
                newDirection = blockFacing.supportsDirection(face) ? face : Direction.NORTH;
            }

            state = blockFacing.setDirection(state, newDirection);
        }
        if (block instanceof IStateFluidLoggable) {
            IFluidState fluidState = world.getFluidState(pos);
            state = state.with(FLUID_LOGGED, fluidState.getFluid() == Fluids.WATER);
        }
        //TODO: I don't know if there is a tile entity yet so this stuff may not really matter
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
        if (Attribute.has(block, AttributeStateFacing.class)) {
            AttributeStateFacing blockFacing = Attribute.get(block, AttributeStateFacing.class);
            return rotate(blockFacing, blockFacing.getFacingProperty(), state, rotation);
        }
        return state;
    }

    public static BlockState mirror(BlockState state, Mirror mirror) {
        Block block = state.getBlock();
        if (Attribute.has(block, AttributeStateFacing.class)) {
            AttributeStateFacing blockFacing = Attribute.get(block, AttributeStateFacing.class);
            DirectionProperty property = blockFacing.getFacingProperty();
            return rotate(blockFacing, property, state, mirror.toRotation(state.get(property)));
        }
        return state;
    }

    private static BlockState rotate(AttributeStateFacing blockFacing, DirectionProperty property, BlockState state, Rotation rotation) {
        return blockFacing.setDirection(state, rotation.rotate(state.get(property)));
    }

    private static boolean isStoring(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntityCardboardBox) {
            return ((TileEntityCardboardBox) tile).storedData != null;
        }
        return false;
    }

    public static BlockState copyStateData(BlockState oldState, BlockState newState) {
        Block oldBlock = oldState.getBlock();
        Block newBlock = newState.getBlock();
        if (Attribute.has(oldBlock, newBlock, AttributeStateFacing.class)) {
            newState = newState.with(Attribute.get(newBlock, AttributeStateFacing.class).getFacingProperty(), oldState.get(Attribute.get(oldBlock, AttributeStateFacing.class).getFacingProperty()));
        }
        if (Attribute.has(oldBlock, newBlock, AttributeStateActive.class)) {
            newState = newState.with(activeProperty, oldState.get(activeProperty));
        }
        if (oldBlock instanceof IStateStorage && newBlock instanceof IStateStorage) {
            newState = newState.with(storageProperty, oldState.get(storageProperty));
        }
        if (oldBlock instanceof IStateFluidLoggable && newBlock instanceof IStateFluidLoggable) {
            newState = newState.with(FLUID_LOGGED, oldState.get(FLUID_LOGGED));
        }
        return newState;
    }
}