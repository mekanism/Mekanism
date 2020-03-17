package mekanism.common.block.attribute;

import java.util.Collection;
import javax.annotation.Nonnull;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

public class AttributeStateFacing implements Attribute {

    private DirectionProperty facingProperty;

    public AttributeStateFacing() {
        this(BlockStateHelper.horizontalFacingProperty);
    }

    public AttributeStateFacing(DirectionProperty facingProperty) {
        this.facingProperty = facingProperty;
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

    public Collection<Direction> getSupportedDirections() {
        return getFacingProperty().getAllowedValues();
    }

    public boolean supportsDirection(Direction direction) {
        return getSupportedDirections().contains(direction);
    }
}
