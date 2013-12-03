package mekanism.common.item;

import java.util.List;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.EnumColor;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

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
	
	public ItemBlockOre(int id, Block block)
	{
		super(id);
		metaBlock = block;
		setHasSubtypes(true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add("Hold " + EnumColor.AQUA + "shift" + EnumColor.GREY + " for details.");
		}
		else {
			if(itemstack.getItemDamage() == 0)
			{
				list.add("A strong mineral that can be found");
				list.add("at nearly any height in the world.");
				list.add("It is known to have many uses in");
				list.add("the construction of machinery.");
			}
			else if(itemstack.getItemDamage() == 1)
			{
				list.add("A common, conductive material that");
				list.add("can be used in the production of.");
				list.add("wires. It's ability to withstand");
				list.add("high heats also makes it essential");
				list.add("to advanced machinery.");
			}
			else if(itemstack.getItemDamage() == 2)
			{
				list.add("A lightweight, yet sturdy, conductive");
				list.add("material that is found slighly less");
				list.add("commonly than Copper.");
			}
		}
	}
	
	@Override
	public int getMetadata(int i)
	{
		return i;
	}
	
	@Override
	public Icon getIconFromDamage(int i)
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
