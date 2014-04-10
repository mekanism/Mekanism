package mekanism.common.block;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.tile.TileEntityPlasticBlock;

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlastic extends Block
{
	public Icon[] icons = new Icon[256];

	public int numColours = 16;

	public String[] names = {"PlasticBlock", "SlickPlasticBlock", "GlowPlasticBlock", "ReinforcedPlasticBlock"};

	public BlockPlastic(int id)
	{
		super(id, Material.clay);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:PlasticBlock");
		icons[1] = register.registerIcon("mekanism:SlickPlasticBlock");
		icons[2] = register.registerIcon("mekanism:GlowPlasticBlock");
		icons[3] = register.registerIcon("mekanism:ReinforcedPlasticBlock");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		return icons[world.getBlockMetadata(x,y,z)];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icons[meta>>4];
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
		for(int i = 0; i < numColours*4; i++)
		{
			list.add(new ItemStack(id, 1, i));
		}
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if(metadata == 2)
		{
			return 10;
		}

		return 0;
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityPlasticBlock();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		TileEntityPlasticBlock tile = (TileEntityPlasticBlock)world.getBlockTileEntity(x, y, z);
		return new ItemStack(blockID, 1, tile.getItemMeta());
	}
}
