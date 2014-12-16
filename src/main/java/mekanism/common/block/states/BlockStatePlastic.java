package mekanism.common.block.states;

import mekanism.api.EnumColor;
import mekanism.common.MekanismBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;

public class BlockStatePlastic extends BlockState
{
	public static PropertyEnum colorProperty = PropertyEnum.create("color", EnumColor.class, EnumColor.DYES);
	public static PropertyEnum typeProperty = PropertyEnum.create("type", PlasticBlockType.class);

	public BlockStatePlastic()
	{
		super(MekanismBlocks.PlasticBlock, colorProperty, typeProperty);
	}

	public static enum PlasticBlockType
	{
		PLASTIC,
		SLICK,
		GLOW,
		REINFORCED,
		ROAD;
	}
}
