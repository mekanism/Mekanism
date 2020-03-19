package mekanism.common.block.attribute;

import java.util.Collection;
import javax.annotation.Nonnull;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

public class AttributeStateFacing implements Attribute {

    private DirectionProperty facingProperty;
    private FacePlacementType placementType;

    public AttributeStateFacing() {
        this(BlockStateHelper.horizontalFacingProperty);
    }

    public AttributeStateFacing(DirectionProperty facingProperty) {
        this(facingProperty, FacePlacementType.PLAYER_LOCATION);
    }

    public AttributeStateFacing(DirectionProperty facingProperty, FacePlacementType placementType) {
        this.facingProperty = facingProperty;
        this.placementType = placementType;
    }

    //TODO: Try to also add some sort of helpers from this for rotation (maybe fully move rotation out of TEs)
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

    public enum FacePlacementType {
        /** Set the face based on the player's relative location to the placement location. */
        PLAYER_LOCATION,
        /** Set the face based on the direction of the block face selected. */
        SELECTED_FACE
    }
}
