package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item class for handling multiple ore block IDs.
 * 0: Osmium Ore
 * 1: Copper Ore
 * 2: Tin Ore
 * @author AidanBrady
 *
 */
public class ItemBlockOre extends ItemBlock
{
	public Block metaBlock;

	public ItemBlockOre(Block block)
	{
		super(block);
		metaBlock = block;
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			list.add("Hold " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " for details.");
		}
		else {
			list.addAll(MekanismUtils.splitLines(MekanismUtils.localize("tooltip." + getUnlocalizedName(itemstack).replace("tile.OreBlock.", ""))));
		}
	}

	@Override
	public int getMetadata(int i)
	{
		return i;
	}

	@Override
	public IIcon getIconFromDamage(int i)
	{
		return metaBlock.getIcon(2, i);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String name = "";

		switch(itemstack.getItemDamage())
		{
			case 0:
				name = "OsmiumOre";
				break;
			case 1:
				name = "CopperOre";
				break;
			case 2:
				name = "TinOre";
				break;
			default:
				name = "Unknown";
				break;
		}

		return getUnlocalizedName() + "." + name;
	}
}
