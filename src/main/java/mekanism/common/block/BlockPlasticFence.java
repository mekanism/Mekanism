package mekanism.common.block;

import static mekanism.common.block.states.BlockStatePlastic.colorProperty;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlasticFence extends BlockFence
{
	public BlockPlasticFence()
	{
		super(Material.clay);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] {NORTH, EAST, WEST, SOUTH, colorProperty});
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(NORTH, false).withProperty(EAST, false)
				.withProperty(WEST, false).withProperty(SOUTH, false).withProperty(colorProperty, EnumDyeColor.byMetadata(meta));
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
        EnumColor colour = EnumColor.AQUA;
        return (int)(colour.getColor(0)*255) << 16 | (int)(colour.getColor(1)*255) << 8 | (int)(colour.getColor(2)*255);
    }

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}
}
