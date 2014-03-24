package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWalkieTalkie extends ItemMekanism
{
	public Icon[] icons = new Icon[256];

	public ItemWalkieTalkie(int id)
	{
		super(id);
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		list.add(getOn(itemstack) ? EnumColor.DARK_GREEN + "On" : EnumColor.DARK_RED + "Off");
		list.add(EnumColor.DARK_AQUA + "Channel: " + EnumColor.GREY + getChannel(itemstack));
	}

	@Override
	public Icon getIconIndex(ItemStack itemStack)
	{
		if(!getOn(itemStack))
		{
			return icons[0];
		}

		return icons[getChannel(itemStack)];
	}

	@Override
	public void registerIcons(IconRegister register)
	{
		icons[0] = register.registerIcon("mekanism:WalkieTalkieOff");

		for(int i = 1; i <= 9; i++)
		{
			icons[i] = register.registerIcon("mekanism:WalkieTalkie_ch" + i);
		}
	}

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
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setBoolean("on", on);
	}

	public boolean getOn(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return false;
		}

		return itemStack.stackTagCompound.getBoolean("on");
	}

	public void setChannel(ItemStack itemStack, int channel)
	{
		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		itemStack.stackTagCompound.setInteger("channel", channel);
	}

	public int getChannel(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound == null)
		{
			return 1;
		}

		int channel = itemStack.stackTagCompound.getInteger("channel");

		if(channel == 0)
		{
			setChannel(itemStack, 1);
		}

		return itemStack.stackTagCompound.getInteger("channel");
	}
}
