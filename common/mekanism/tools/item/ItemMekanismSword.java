package mekanism.tools.item;

import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemMekanismSword extends ItemSword
{
    public ItemMekanismSword(int id, EnumToolMaterial enumtoolmaterial)
    {
        super(id, enumtoolmaterial);
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
