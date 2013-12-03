package calclavia.lib.base;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.Configuration;
import universalelectricity.compatibility.ItemUniversalElectric;

public abstract class ItemElectricBase extends ItemUniversalElectric
{
	public ItemElectricBase(int id, String name, Configuration config, String prefix, CreativeTabs tab)
	{
		super(config.getItem(name, id).getInt());
		this.setUnlocalizedName(prefix + name);
		this.setCreativeTab(tab);
		this.setTextureName(prefix + name);
	}
}
