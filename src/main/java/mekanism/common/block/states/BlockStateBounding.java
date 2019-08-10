package mekanism.common.block.states;

import javafx.beans.property.BooleanProperty;
import mekanism.common.block.BlockBounding;
import net.minecraft.block.state.BlockStateContainer;

public class BlockStateBounding extends BlockStateContainer {

    public static BooleanProperty advancedProperty = BooleanProperty.create("advanced");

    public BlockStateBounding(BlockBounding block) {
        super(block, advancedProperty);
    }
}