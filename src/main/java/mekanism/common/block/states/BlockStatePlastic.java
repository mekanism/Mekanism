package mekanism.common.block.states;

import mekanism.api.EnumColor;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockPlastic;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

public class BlockStatePlastic extends BlockState
{
	public static PropertyEnum<EnumDyeColor> colorProperty = PropertyEnum.create("color", EnumDyeColor.class);

	public BlockStatePlastic(BlockPlastic block)
	{
		super(block, colorProperty);
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

	public static class PlasticBlockStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			PlasticBlockType type = ((BlockPlastic)state.getBlock()).type;
			String property = "type=" + type.getName();

			ResourceLocation baseLocation = new ResourceLocation("mekanism", "plastic_block");
			return new ModelResourceLocation(baseLocation, property);
		}
	}
}
