package mekanism.common.block.states;

import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.tier.BaseTier;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;

public class BlockStateFacing extends BlockStateContainer {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing");

    public BlockStateFacing(Block block, PropertyEnum<?> typeProperty, PropertyBool activeProperty,
          PropertyEnum<BaseTier> tierProperty, PropertyEnum<RecipeType> recipeProperty) {
        super(block, facingProperty, typeProperty, activeProperty, tierProperty, recipeProperty);
    }

    public BlockStateFacing(Block block, PropertyEnum<?> typeProperty, PropertyBool activeProperty) {
        super(block, facingProperty, typeProperty, activeProperty);
    }

    public BlockStateFacing(Block block, PropertyEnum<?> typeProperty) {
        super(block, facingProperty, typeProperty);
    }

    public BlockStateFacing(Block block) {
        super(block, facingProperty);
    }
}
