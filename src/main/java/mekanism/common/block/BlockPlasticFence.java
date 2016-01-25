package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;

import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
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
        EnumColor colour = EnumColor.DYES[getMetaFromState(state)];
        return (int)(colour.getColor(0)*255) << 16 | (int)(colour.getColor(1)*255) << 8 | (int)(colour.getColor(2)*255);

    }

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}
}
