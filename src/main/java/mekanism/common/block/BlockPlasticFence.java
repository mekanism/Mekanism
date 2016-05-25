package mekanism.common.block;

import static mekanism.common.block.states.BlockStatePlastic.colorProperty;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlasticFence extends BlockFence
{
	public BlockPlasticFence()
	{
		super(Material.CLAY);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {NORTH, EAST, WEST, SOUTH, colorProperty});
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(colorProperty, EnumDyeColor.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(colorProperty).getMetadata();
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs creativetabs, List<ItemStack> list)
    {
        for(int i = 0; i < EnumColor.DYES.length; i++)
        {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass)
    {
        return getRenderColor(world.getBlockState(pos));
    }

    @Override
    public int getRenderColor(IBlockState state)
    {
		EnumDyeColor color = state.getValue(colorProperty);
		EnumColor dye = EnumColor.DYES[color.getDyeDamage()];
		
		return (int)(dye.getColor(0)*255) << 16 | (int)(dye.getColor(1)*255) << 8 | (int)(dye.getColor(2)*255);
    }

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}
	
	public static class PlasticFenceStateMapper extends StateMapperBase
	{
		@Override
		protected ModelResourceLocation getModelResourceLocation(IBlockState state)
		{
			String properties = "east=" + state.getValue(EAST) + ",";
			properties += "north=" + state.getValue(NORTH) + ",";
			properties += "south=" + state.getValue(SOUTH) + ",";
			properties += "west=" + state.getValue(WEST);
			ResourceLocation baseLocation = new ResourceLocation("mekanism", "PlasticFence");
			return new ModelResourceLocation(baseLocation, properties);
		}
	}
}
