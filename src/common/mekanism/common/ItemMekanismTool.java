package mekanism.common;

import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ItemTool;

public class ItemMekanismTool extends ItemTool
{
    public ItemMekanismTool(int id, int mobBoost, EnumToolMaterial enumtoolmaterial, Block effectiveBlocks[])
    {
        super(id, mobBoost, enumtoolmaterial, effectiveBlocks);
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
