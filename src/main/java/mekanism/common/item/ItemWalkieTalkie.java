package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemWalkieTalkie extends ItemMekanism
{
	public ItemWalkieTalkie()
	{
		super();
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List<String> list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		list.add((getOn(itemstack) ? EnumColor.DARK_GREEN : EnumColor.DARK_RED) + LangUtils.localize("gui." + (getOn(itemstack) ? "on" : "off")));
		list.add(EnumColor.DARK_AQUA + LangUtils.localize("tooltip.channel") + ": " + EnumColor.GREY + getChannel(itemstack));
	}

/*
	@Override
	public IIcon getIconIndex(ItemStack itemStack)
	{
		if(!getOn(itemStack))
		{
			return icons[0];
		}

		return icons[getChannel(itemStack)];
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:WalkieTalkieOff");

		for(int i = 1; i <= 9; i++)
		{
			icons[i] = register.registerIcon("mekanism:WalkieTalkie_ch" + i);
		}
	}
*/

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		if(player.isSneaking())
		{
			setOn(itemStack, !getOn(itemStack));
		}

		return itemStack;
	}

	public void setOn(ItemStack itemStack, boolean on)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setBoolean("on", on);
	}

	public boolean getOn(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return false;
		}

		return itemStack.getTagCompound().getBoolean("on");
	}

	public void setChannel(ItemStack itemStack, int channel)
	{
		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.getTagCompound().setInteger("channel", channel);
	}

	public int getChannel(ItemStack itemStack)
	{
		if(itemStack.getTagCompound() == null)
		{
			return 1;
		}

		int channel = itemStack.getTagCompound().getInteger("channel");

		if(channel == 0)
		{
			setChannel(itemStack, 1);
		}

		return itemStack.getTagCompound().getInteger("channel");
	}
}
