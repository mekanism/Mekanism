package calclavia.lib.base;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;

public class ItemBase extends Item
{
	public ItemBase(int id, String name, Configuration config, String prefix, CreativeTabs tab)
	{
		super(config.getItem(name, id).getInt());
		this.setUnlocalizedName(prefix + name);
		this.setCreativeTab(tab);
		this.setTextureName(prefix + name);
	}
}
