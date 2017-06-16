package mekanism.common.item;

import java.util.List;
import java.util.UUID;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketSecurityUpdate.SecurityPacket;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import mekanism.common.security.IOwnerItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemPortableTeleporter extends ItemEnergized implements IOwnerItem
{
	public ItemPortableTeleporter()
	{
		super(1000000);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag)
	{
		list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
		
		if(getFrequency(itemstack) != null)
		{
			list.add(EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + getFrequency(itemstack));
			list.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY + LangUtils.localize("gui." + (isPrivateMode(itemstack) ? "private" : "public")));
		}
		
		super.addInformation(itemstack, world, list, flag);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, EnumHand hand)
	{
		ItemStack itemstack = entityplayer.getHeldItem(hand);
		
		if(!world.isRemote)
		{
			if(getOwnerUUID(itemstack) == null)
			{
				setOwnerUUID(itemstack, entityplayer.getUniqueID());
				Mekanism.packetHandler.sendToAll(new SecurityUpdateMessage(SecurityPacket.UPDATE, entityplayer.getUniqueID(), null));
				entityplayer.sendMessage(new TextComponentString(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("gui.nowOwn")));
			}
			else {
				if(SecurityUtils.canAccess(entityplayer, itemstack))
				{
					entityplayer.openGui(Mekanism.instance, 14, world, hand.ordinal(), 0, 0);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
			}
		}
		
		return new ActionResult(EnumActionResult.SUCCESS, itemstack);
	}

	public static double calculateEnergyCost(Entity entity, Coord4D coords)
	{
		if(coords == null)
		{
			return 0;
		}

		int neededEnergy = 1000;

		if(entity.world.provider.getDimension() != coords.dimensionId)
		{
			neededEnergy += 10000;
		}

		int distance = (int)entity.getDistance(coords.x, coords.y, coords.z);

		neededEnergy+=(distance*10);

		return neededEnergy;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
	
	@Override
	public UUID getOwnerUUID(ItemStack stack) 
	{
		if(ItemDataUtils.hasData(stack, "ownerUUID"))
		{
			return UUID.fromString(ItemDataUtils.getString(stack, "ownerUUID"));
		}
		
		return null;
	}

	@Override
	public void setOwnerUUID(ItemStack stack, UUID owner) 
	{
		setFrequency(stack, null);
		setPrivateMode(stack, false);
		
		if(owner == null)
		{
			ItemDataUtils.removeData(stack, "ownerUUID");
			return;
		}
		
		ItemDataUtils.setString(stack, "ownerUUID", owner.toString());
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
