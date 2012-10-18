package net.uberkat.obsidian.common;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemTool;

public class ItemObsidianTool extends ItemTool
{
    public ItemObsidianTool(int par1, int par2, EnumToolMaterial par3EnumToolMaterial, Block par4ArrayOfBlock[])
    {
        super(par1, par2, par3EnumToolMaterial, par4ArrayOfBlock);
    }
    
    public String getTextureFile()
    {
    	return "/textures/items.png";
    }
}
