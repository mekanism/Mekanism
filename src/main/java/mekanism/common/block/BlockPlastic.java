package mekanism.common.block;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.client.ClientProxy;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
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
		setHardness(this == MekanismBlocks.ReinforcedPlasticBlock ? 50F : 5F);
		setResistance(this == MekanismBlocks.ReinforcedPlasticBlock ? 2000F : 10F);
		setCreativeTab(Mekanism.tabMekanism);
		
		if(this == MekanismBlocks.SlickPlasticBlock)
		{
			slipperiness = 0.98F;
		}
	}

/*
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
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
	public Vec3 modifyAcceleration(World world, BlockPos pos, Entity e, Vec3 motion)
	{
		if(this == MekanismBlocks.RoadPlasticBlock)
		{
			double boost = 1.6;

			double a = Math.atan2(motion.xCoord, motion.zCoord);
			return new Vec3(motion.xCoord + Math.sin(a) * boost * slipperiness, motion.yCoord, motion.zCoord + Math.cos(a) * boost * slipperiness);
		}
		return motion;
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state);
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
	public int getLightValue()
	{
		if(this == MekanismBlocks.GlowPlasticBlock)
		{
			return 10;
		}

		return 0;
	}
}
