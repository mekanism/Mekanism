package mekanism.common.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemWalkieTalkie extends ItemMekanism
{
	public static ModelResourceLocation OFF_MODEL = new ModelResourceLocation("mekanism:WalkieTalkie", "inventory");
	
	public static Map<Integer, ModelResourceLocation> CHANNEL_MODELS = new HashMap<Integer, ModelResourceLocation>();
	
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
	
	public static ModelResourceLocation getModel(int channel)
	{
		if(CHANNEL_MODELS.get(channel) == null)
		{
			CHANNEL_MODELS.put(channel, new ModelResourceLocation("mekanism:WalkieTalkie_ch" + channel, "inventory"));
		}
		
		return CHANNEL_MODELS.get(channel);
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
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.areItemsEqual(oldStack, newStack);
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
