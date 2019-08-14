package mekanism.common.block.states;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;

public interface IStateFacing {

    //TODO: Try to also add some sort of helpers from this for rotation (maybe fully move rotation out of TEs)
    default Direction getDirection(BlockState state) {
        return state.get(getFacingProperty());
    }

    default BlockState setDirection(@Nonnull BlockState state, Direction newDirection) {
        return supportsDirection(newDirection) ? state.with(getFacingProperty(), newDirection) : state;
    }

    @Nonnull
    default DirectionProperty getFacingProperty() {
        return BlockStateHelper.horizontalFacingProperty;
    }

    default Collection<Direction> getSupportedDirections() {
        return getFacingProperty().getAllowedValues();
    }

    default boolean supportsDirection(Direction direction) {
        return getSupportedDirections().contains(direction);
    }
}