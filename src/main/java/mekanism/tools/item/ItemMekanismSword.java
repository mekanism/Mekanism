package mekanism.tools.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemMekanismSword extends ItemSword
{
	public ItemMekanismSword(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(MekanismUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
