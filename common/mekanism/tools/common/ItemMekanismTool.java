package mekanism.tools.common;

import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

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
	public void registerIcons(IconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
