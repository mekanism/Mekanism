package mekanism.common;

import net.minecraft.src.*;

public class ItemMekanismArmor extends ItemArmor
{
    public ItemMekanismArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial, int par3, int par4)
    {
        super(par1, par2EnumArmorMaterial, par3, par4);
        setCreativeTab(Mekanism.tabMekanism);
    }
    
    @Override
	public String getTextureFile() 
	{
		return "/resources/mekanism/textures/items.png";
	}
}
