package mekanism.common.block.attribute;

import javax.annotation.Nonnull;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.block.BlockState;

public class AttributeStateActive implements Attribute {

    public boolean isActive(BlockState state) {
        return state.get(BlockStateHelper.activeProperty);
    }

    public BlockState setActive(@Nonnull BlockState state, boolean active) {
        return state.with(BlockStateHelper.activeProperty, active);
    }
}