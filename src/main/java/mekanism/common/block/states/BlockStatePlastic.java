package mekanism.common.block.states;

import mekanism.api.EnumColor;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockPlastic;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.IStringSerializable;

public class BlockStatePlastic extends BlockState
{
	public static PropertyEnum colorProperty = PropertyEnum.create("color", EnumDyeColor.class);
	public static PropertyEnum typeProperty = PropertyEnum.create("type", PlasticBlockType.class);

	public BlockStatePlastic(BlockPlastic block)
	{
		super(block, colorProperty, typeProperty);
	}

	public static enum PlasticBlockType implements IStringSerializable
	{
		PLASTIC,
		SLICK,
		GLOW,
		REINFORCED,
		ROAD;

		@Override
		public String getName()
		{
			return name().toLowerCase();
		}
	}
}
