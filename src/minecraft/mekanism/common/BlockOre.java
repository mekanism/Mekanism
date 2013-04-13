package mekanism.common;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple ore block IDs. 
 * 0: Osmium Ore
 * @author AidanBrady
 *
 */
public class BlockOre extends Block
{
	public Icon[] icons = new Icon[256];
	
	public BlockOre(int id)
	{
		super(id, Material.rock);
		setHardness(3F);
		setResistance(5F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void registerIcons(IconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:OsmiumOre");
	}
	
	@Override
	public Icon getIcon(int side, int meta)
	{
		switch(meta)
		{
			case 0:
				return icons[0];
		}
		return null;
	}
	
	@Override
	public int damageDropped(int i)
	{
		return i;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
	}
}
