package mekanism.common.item;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.Mekanism;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemPortableTeleporter extends ItemEnergized implements IOwnerItem
{
	public ItemPortableTeleporter()
	{
		super(1000000);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(SecurityUtils.getOwnerDisplay(entityplayer.getName(), getOwner(itemstack)));
		
		if(getFrequency(itemstack) != null)
		{
			list.add(EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + getFrequency(itemstack));
			list.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY + LangUtils.localize("gui." + (isPrivateMode(itemstack) ? "private" : "public")));
		}
		
		if(!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey))
		{
			list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
		}
		else {
			super.addInformation(itemstack, entityplayer, list, flag);
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote)
		{
			if(getOwner(itemstack) == null)
			{
				setOwner(itemstack, entityplayer.getName());
				entityplayer.addChatMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("gui.nowOwn")));
			}
			else {
				if(SecurityUtils.canAccess(entityplayer, itemstack))
				{
					entityplayer.openGui(Mekanism.instance, 14, world, 0, 0, 0);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
			}
		}
		
		return itemstack;
	}

	public static double calculateEnergyCost(Entity entity, Coord4D coords)
	{
		if(coords == null)
		{
			return 0;
		}

		int neededEnergy = 1000;

		if(entity.worldObj.provider.getDimension() != coords.dimensionId)
		{
			neededEnergy += 10000;
		}

		int distance = (int)entity.getDistance(coords.xCoord, coords.yCoord, coords.zCoord);

		neededEnergy+=(distance*10);

		return neededEnergy;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
	
	@Override
	public String getOwner(ItemStack stack) 
	{
		if(ItemDataUtils.hasData(stack, "owner"))
		{
			return ItemDataUtils.getString(stack, "owner");
		}
		
		return null;
	}

	@Override
	public void setOwner(ItemStack stack, String owner) 
	{
		setFrequency(stack, null);
		setPrivateMode(stack, false);
		
		if(owner == null || owner.isEmpty())
		{
			ItemDataUtils.removeData(stack, "owner");
			return;
		}
		
		ItemDataUtils.setString(stack, "owner", owner);
	}
	
	@Override
	public boolean hasOwner(ItemStack stack)
	{
		return true;
	}
	
	public boolean isPrivateMode(ItemStack stack) 
	{
		return ItemDataUtils.getBoolean(stack, "private");
	}

	public void setPrivateMode(ItemStack stack, boolean isPrivate) 
	{
		ItemDataUtils.setBoolean(stack, "private", isPrivate);
	}
	
	public String getFrequency(ItemStack stack) 
	{
		if(ItemDataUtils.hasData(stack, "frequency"))
		{
			return ItemDataUtils.getString(stack, "frequency");
		}
		
		return null;
	}

	public void setFrequency(ItemStack stack, String frequency) 
	{
		if(frequency == null || frequency.isEmpty())
		{
			ItemDataUtils.removeData(stack, "frequency");
			return;
		}
		
		ItemDataUtils.setString(stack, "frequency", frequency);
	}
}
