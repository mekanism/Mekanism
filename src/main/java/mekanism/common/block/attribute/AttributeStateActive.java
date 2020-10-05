package mekanism.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;

public class AttributeStateActive implements AttributeState {

    private static final BooleanProperty activeProperty = BooleanProperty.create("active");

    public boolean isActive(BlockState state) {
        return state.get(activeProperty);
    }

    public BlockState setActive(@Nonnull BlockState state, boolean active) {
        return state.with(activeProperty, active);
    }

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState.getBlock(), AttributeStateActive.class)) {
            newState = newState.with(activeProperty, oldState.get(activeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@Nonnull BlockState state) {
        return state.with(activeProperty, false);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(activeProperty);
    }
}