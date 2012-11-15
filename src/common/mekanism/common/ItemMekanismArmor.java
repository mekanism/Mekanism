package mekanism.common;

import java.util.List;

import net.minecraft.src.*;

public class ItemMekanismArmor extends ItemArmor
{
    public ItemMekanismArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial, int par3, int par4)
    {
        super(par1, par2EnumArmorMaterial, par3, par4);
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
		return "/resources/mekanism/textures/items.png";
	}
}
