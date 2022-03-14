package mekanism.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class AttributeStateOpen implements AttributeState {

    public static final AttributeStateOpen INSTANCE = new AttributeStateOpen();

    @Override
    public BlockState copyStateData(BlockState oldState, BlockState newState) {
        if (Attribute.has(newState, AttributeStateOpen.class)) {
            newState = newState.setValue(BlockStateProperties.OPEN, oldState.getValue(BlockStateProperties.OPEN));
        }
        return newState;
    }

    @Override
    public BlockState getDefaultState(@Nonnull BlockState state) {
        return state.setValue(BlockStateProperties.OPEN, false);
    }

    @Override
    public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
        properties.add(BlockStateProperties.OPEN);
    }
}