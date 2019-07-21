package mekanism.common.block.states;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.item.EnumDyeColor;

public class BlockStatePlastic extends BlockStateContainer {

    public static PropertyEnum<EnumDyeColor> colorProperty = PropertyEnum.create("color", EnumDyeColor.class);

    public BlockStatePlastic(Block block) {
        super(block, colorProperty);
    }
}