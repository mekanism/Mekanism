package mekanism.common.block.states;

import mekanism.common.block.BlockBounding;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;

public class BlockStateBounding extends BlockState
{
	public static PropertyBool advancedProperty = PropertyBool.create("advanced");

	public BlockStateBounding(BlockBounding block)
	{
		super(block, advancedProperty);
	}
}
