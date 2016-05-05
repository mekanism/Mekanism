package mekanism.common.block.states;

import mekanism.common.block.BlockCardboardBox;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

public class BlockStateCardboardBox extends BlockState
{
	public static PropertyBool storageProperty = PropertyBool.create("storage");

	public BlockStateCardboardBox(BlockCardboardBox block)
	{
		super(block, storageProperty);
	}

	public static class CardboardBoxStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			String property = "storage=" + state.getValue(storageProperty);
			
			ResourceLocation baseLocation = new ResourceLocation("mekanism", "CardboardBox");
			return new ModelResourceLocation(baseLocation, property);
		}
	}
}
