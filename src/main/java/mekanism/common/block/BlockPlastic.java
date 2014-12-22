package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.client.ClientProxy;
import mekanism.common.Mekanism;
import mekanism.common.block.states.BlockStatePlastic;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPlastic extends Block
{
	public BlockPlastic()
	{
		super(Material.wood);
		setCreativeTab(Mekanism.tabMekanism);
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureMap register)
	{
		if(this == MekanismBlocks.PlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:PlasticBlock");
		}
		else if(this == MekanismBlocks.SlickPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:SlickPlasticBlock");
		}
		else if(this == MekanismBlocks.GlowPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:GlowPlasticBlock");
		}
		else if(this == MekanismBlocks.ReinforcedPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:ReinforcedPlasticBlock");
		}
		else if(this == MekanismBlocks.RoadPlasticBlock)
		{
			blockIcon = register.registerIcon("mekanism:RoadPlasticBlock");
		}
	}
*/

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity e)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getValue(BlockStatePlastic.typeProperty) == PlasticBlockType.ROAD)
		{
			double boost = 1.6;

			double a = Math.atan2(e.motionX, e.motionZ);
			e.motionX += Math.sin(a) * boost * slipperiness;
			e.motionZ += Math.cos(a) * boost * slipperiness;
		}
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
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
	public int getLightValue(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getValue(BlockStatePlastic.typeProperty) == PlasticBlockType.GLOW)
		{
			return 10;
		}
		return 0;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
	{
		IBlockState state = world.getBlockState(pos);
		if(state.getValue(BlockStatePlastic.typeProperty) == PlasticBlockType.REINFORCED)
		{
			return 2000;
		}
		return 10;
	}

	@Override
	public float getBlockHardness(World worldIn, BlockPos pos)
	{
		IBlockState state = worldIn.getBlockState(pos);
		if(state.getValue(BlockStatePlastic.typeProperty) == PlasticBlockType.REINFORCED)
		{
			return 50;
		}
		return 5;
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
}
