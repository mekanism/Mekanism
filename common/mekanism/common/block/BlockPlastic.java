package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlastic extends Block
{
	public BlockPlastic(int id)
	{
		super(id, Material.wood);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
		if(id == Mekanism.slickPlasticID)
		{
			slipperiness = 0.98F;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		if(blockID == Mekanism.plasticID)
		{
			blockIcon = register.registerIcon("mekanism:PlasticBlock");
		}
		else if(blockID == Mekanism.slickPlasticID)
		{
			blockIcon = register.registerIcon("mekanism:SlickPlasticBlock");
		}
		else if(blockID == Mekanism.glowPlasticID)
		{
			blockIcon = register.registerIcon("mekanism:GlowPlasticBlock");
		}
		else if(blockID == Mekanism.reinforcedPlasticID)
		{
			blockIcon = register.registerIcon("mekanism:ReinforcedPlasticBlock");
		}
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int id, CreativeTabs creativetabs, List list)
	{
		for(int i = 0; i < EnumColor.DYES.length; i++)
		{
			list.add(new ItemStack(id, 1, i));
		}
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z)
	{
		return getRenderColor(world.getBlockMetadata(x, y, z));
	}

	@Override
	public int getRenderColor(int meta)
	{
		EnumColor colour = EnumColor.DYES[meta];
		return (int)(colour.getColor(0)*255) << 16 | (int)(colour.getColor(1)*255) << 8 | (int)(colour.getColor(2)*255);

	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		if(blockID == Mekanism.glowPlasticID)
		{
			return 10;
		}

		return 0;
	}

	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if (meta != colour)
		{
			world.setBlockMetadataWithNotify(x, y, z, colour, 3);
			return true;
		}
		return false;
	}
}
