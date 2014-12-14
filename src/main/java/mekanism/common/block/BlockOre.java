package mekanism.common.block;

import java.util.List;

import mekanism.common.Mekanism;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSpriteRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple ore block IDs.
 * 0: Osmium Ore
 * 1: Copper Ore
 * 2: Tin Ore
 * @author AidanBrady
 *
 */
public class BlockOre extends Block
{
	public TextureAtlasSprite[] icons = new TextureAtlasSprite[256];

	public BlockOre()
	{
		super(Material.rock);
		setHardness(3F);
		setResistance(5F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureAtlasSpriteRegister register)
	{
		icons[0] = register.registerIcon("mekanism:OsmiumOre");
		icons[1] = register.registerIcon("mekanism:CopperOre");
		icons[2] = register.registerIcon("mekanism:TinOre");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(int side, int meta)
	{
		return icons[meta];
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
		list.add(new ItemStack(item, 1, 2));
	}
}
