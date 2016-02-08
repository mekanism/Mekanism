package mekanism.common.block.states;

import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockOre;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.IStringSerializable;

/**
 * Created by ben on 23/12/14.
 */
public class BlockStateOre extends BlockState
{
	public static final PropertyEnum<EnumOreType> typeProperty = PropertyEnum.create("type", EnumOreType.class);

	public BlockStateOre(BlockOre block)
	{
		super(block, typeProperty);
	}

	public static enum EnumOreType implements IStringSerializable
	{
		OSMIUM,
		COPPER,
		TIN;

		@Override
		public String getName()
		{
			return name().toLowerCase();
		}
	}
}
