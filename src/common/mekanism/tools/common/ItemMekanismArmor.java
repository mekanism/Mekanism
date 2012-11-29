package mekanism.tools.common;

import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.src.*;

public class ItemMekanismArmor extends ItemArmor
{
    public ItemMekanismArmor(int id, EnumArmorMaterial par2EnumArmorMaterial, int renderIndex, int armorType)
    {
        super(id, par2EnumArmorMaterial, renderIndex, armorType);
        setCreativeTab(Mekanism.tabMekanism);
    }
    
    @Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add("HP: " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}
    
    @Override
	public String getTextureFile() 
	{
		return "/resources/mekanism/textures/tools/items.png";
	}
}
