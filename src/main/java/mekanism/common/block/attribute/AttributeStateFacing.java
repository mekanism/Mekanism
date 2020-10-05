package mekanism.common.block.attribute;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import org.jetbrains.annotations.Contract;

public class AttributeStateFacing implements AttributeState {

    private final DirectionProperty facingProperty;
    private final FacePlacementType placementType;
    private final boolean canRotate;

    public AttributeStateFacing() {
        this(true);
    }

    public AttributeStateFacing(boolean canRotate) {
        this(BlockStateProperties.HORIZONTAL_FACING, canRotate);
    }

    public AttributeStateFacing(DirectionProperty facingProperty) {
        this(facingProperty, true);
    }

    public AttributeStateFacing(DirectionProperty facingProperty, boolean canRotate) {
        this(facingProperty, FacePlacementType.PLAYER_LOCATION, canRotate);
    }

    public AttributeStateFacing(DirectionProperty facingProperty, FacePlacementType placementType) {
        this(facingProperty, placementType, true);
    }

    public AttributeStateFacing(DirectionProperty facingProperty, FacePlacementType placementType, boolean canRotate) {
        this.facingProperty = facingProperty;
        this.placementType = placementType;
        this.canRotate = canRotate;
    }

    public boolean canRotate() {
        return canRotate;
    }

    public Direction getDirection(BlockState state) {
        return state.get(getFacingProperty());
    }

    public BlockState setDirection(@Nonnull BlockState state, Direction newDirection) {
        return supportsDirection(newDirection) ? state.with(getFacingProperty(), newDirection) : state;
    }

    @Nonnull
    public DirectionProperty getFacingProperty() {
        return facingProperty;
    }

    @Nonnull
    public FacePlacementType getPlacementType() {
        return placementType;
    }

    public Collection<Direction> getSupportedDirections() {
        return getFacingProperty().getAllowedValues();
    }

    public boolean supportsDirection(Direction direction) {
        return getSupportedDirections().contains(direction);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(getFacingProperty());
    }

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState.getBlock(), AttributeStateFacing.class)) {
            DirectionProperty oldFacingProperty = Attribute.get(oldState.getBlock(), AttributeStateFacing.class).getFacingProperty();
            newState = newState.with(Attribute.get(newState.getBlock(), AttributeStateFacing.class).getFacingProperty(), oldState.get(oldFacingProperty));
        }
        return newState;
    }

    @Override
    @Contract("_, null, _, _, _, _ -> null")
    public BlockState getStateForPlacement(Block block, @Nullable BlockState state, @Nonnull IWorld world, @Nonnull BlockPos pos, @Nullable PlayerEntity player,
          @Nonnull Direction face) {
        if (state == null) {
            return null;
        }
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
        return state;
    }

    public static BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rotation) {
        return rotate(state, rotation);
    }

    public static BlockState rotate(BlockState state, Rotation rotation) {
        Block block = state.getBlock();
        if (Attribute.has(block, AttributeStateFacing.class)) {
            AttributeStateFacing blockFacing = Attribute.get(block, AttributeStateFacing.class);
            if (blockFacing.canRotate()) {
                return rotate(blockFacing, blockFacing.getFacingProperty(), state, rotation);
            }
        }
        return state;
    }

    public static BlockState mirror(BlockState state, Mirror mirror) {
        Block block = state.getBlock();
        if (Attribute.has(block, AttributeStateFacing.class)) {
            AttributeStateFacing blockFacing = Attribute.get(block, AttributeStateFacing.class);
            if (blockFacing.canRotate()) {
                DirectionProperty property = blockFacing.getFacingProperty();
                return rotate(blockFacing, property, state, mirror.toRotation(state.get(property)));
            }
        }
        return state;
    }

    private static BlockState rotate(AttributeStateFacing blockFacing, DirectionProperty property, BlockState state, Rotation rotation) {
        return blockFacing.setDirection(state, rotation.rotate(state.get(property)));
    }

    public enum FacePlacementType {
        /** Set the face based on the player's relative location to the placement location. */
        PLAYER_LOCATION,
        /** Set the face based on the direction of the block face selected. */
        SELECTED_FACE
    }
}
