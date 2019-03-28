package mekanism.common.block.states;

import mekanism.common.block.BlockBounding;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;

public class BlockStateBounding extends BlockStateContainer {

    public static PropertyBool advancedProperty = PropertyBool.create("advanced");

    public BlockStateBounding(BlockBounding block) {
        super(block, advancedProperty);
    }
}
