package mekanism.common.block.states;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;

public class BlockStateFacing extends BlockState
{
	public static PropertyDirection facingProperty = PropertyDirection.create("facing");

	public BlockStateFacing(Block block, PropertyEnum typeProperty)
	{
		super(block, facingProperty, typeProperty);
	}
}
