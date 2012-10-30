package net.uberkat.obsidian.common;

import net.minecraft.src.*;

public class ItemObsidianArmor extends ItemArmor
{
    public ItemObsidianArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial, int par3, int par4)
    {
        super(par1, par2EnumArmorMaterial, par3, par4);
        setCreativeTab(ObsidianIngots.tabOBSIDIAN);
    }
    
	public String getTextureFile() {
		return "/textures/items.png";
	}
}
