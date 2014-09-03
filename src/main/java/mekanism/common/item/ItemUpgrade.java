package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

public class ItemUpgrade extends ItemMekanism implements IUpgradeItem
{
	private Upgrade upgrade;
	
	public ItemUpgrade(Upgrade type)
	{
		super();
		upgrade = type;
		setMaxStackSize(type.getMax());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add(MekanismUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + "shift" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.forDetails"));
		}
		else {
			list.addAll(MekanismUtils.splitLines(getUpgradeType(itemstack).getDescription()));
		}
	}
	
	@Override
	public Upgrade getUpgradeType(ItemStack stack) 
	{
		return upgrade;
	}
}
