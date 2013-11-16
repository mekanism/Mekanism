package mekanism.induction.common.base;

import mekanism.common.Mekanism;
import mekanism.induction.common.MekanismInduction;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;

/**
 * 
 * @author AidanBrady
 * 
 */
public class ItemBase extends Item
{
	public ItemBase(String name, int id)
	{
		super(MekanismInduction.CONFIGURATION.get(Configuration.CATEGORY_ITEM, name, id).getInt(id));
		this.setCreativeTab(Mekanism.tabMekanism);
		this.setUnlocalizedName(MekanismInduction.PREFIX + name);
		this.setTextureName(MekanismInduction.PREFIX + name);
	}
}
