package mekanism.common.block.attribute;

import java.util.List;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class AttributeStateActive implements AttributeState {

    private static final BooleanProperty activeProperty = BooleanProperty.create("active");

    private final int ambientLight;

    AttributeStateActive(int ambientLight) {
        this.ambientLight = ambientLight;
    }

    public boolean isActive(BlockState state) {
        return state.getValue(activeProperty);
    }

    public BlockState setActive(@NotNull BlockState state, boolean active) {
        return state.setValue(activeProperty, active);
    }

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState, AttributeStateActive.class)) {
            newState = newState.setValue(activeProperty, oldState.getValue(activeProperty));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@NotNull BlockState state) {
        return state.setValue(activeProperty, false);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(activeProperty);
    }

    @Override
    public void adjustProperties(BlockBehaviour.Properties props) {
        if (ambientLight > 0) {
            //If we have ambient light, adjust the light level to factor in the ambient light level when it is active
            BlockStateHelper.applyLightLevelAdjustments(props, state -> isActive(state) ? ambientLight : 0);
        }
    }
}