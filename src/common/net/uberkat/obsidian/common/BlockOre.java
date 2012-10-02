package net.uberkat.obsidian.common;

import java.util.List;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import net.minecraft.src.*;

/**
 * Block class for handling multiple ore block IDs. 
 * 0: Platinum Ore
 * @author AidanBrady
 *
 */
public class BlockOre extends Block
{
	public BlockOre(int i)
	{
		super(i, Material.rock);
		setHardness(3F);
		setResistance(5F);
		setCreativeTab(CreativeTabs.tabBlock);
		setRequiresSelfNotify();
	}
	
	public int getBlockTextureFromSideAndMetadata(int side, int meta)
	{
		switch(meta)
		{
			case 0:
				return 3;
		}
		return 0;
	}
	
	public int damageDropped(int i)
	{
		return i;
	}
	
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
	}
	
	public String getTextureFile()
	{
		return "/obsidian/terrain.png";
	}
}
