package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStatePlastic;

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
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlasticFence extends BlockFence
{
	public BlockPlasticFence()
	{
		super(Material.wood);
        setUnlocalizedName("mekanism:PlasticFence");
		setCreativeTab(Mekanism.tabMekanism);
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
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
        EnumColor colour = (EnumColor)state.getValue(BlockStatePlastic.colorProperty);
        return (int)(colour.getColor(0)*255) << 16 | (int)(colour.getColor(1)*255) << 8 | (int)(colour.getColor(2)*255);
    }

    @Override
    public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color)
    {
        IBlockState state = world.getBlockState(pos);
        EnumColor newColor = EnumColor.DYES[color.getDyeDamage()];

        EnumColor current = (EnumColor)state.getValue(BlockStatePlastic.colorProperty);
        if (current != newColor)
        {
            world.setBlockState(pos, state.withProperty(BlockStatePlastic.colorProperty, newColor));
            return true;
        }

        return false;
    }

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
	}

    public int getMetaFromState(IBlockState state)
    {
        return ((EnumColor)state.getValue(BlockStatePlastic.colorProperty)).getMetaValue();
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(BlockStatePlastic.colorProperty, EnumColor.DYES[meta]);
    }

    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] {NORTH, EAST, WEST, SOUTH, BlockStatePlastic.colorProperty});
    }

}
