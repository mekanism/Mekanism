package mekanism.tools.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
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
		list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}
}
