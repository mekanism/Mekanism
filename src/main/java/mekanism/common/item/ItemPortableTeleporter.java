package mekanism.common.item;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.security.IOwnerItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
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
		list.add(SecurityUtils.getOwnerDisplay(entityplayer.getCommandSenderName(), getOwner(itemstack)));
		
		if(getFrequency(itemstack) != null)
		{
			list.add(EnumColor.INDIGO + LangUtils.localize("gui.frequency") + ": " + EnumColor.GREY + getFrequency(itemstack));

			String name = "trusted";

			ISecurityTile.SecurityMode access= getAccess(itemstack);
			if(access == ISecurityTile.SecurityMode.PUBLIC) {
				name = "public";
			} else if(access == ISecurityTile.SecurityMode.PRIVATE) {
				name = "private";
			}

			list.add(EnumColor.INDIGO + LangUtils.localize("gui.mode") + ": " + EnumColor.GREY + LangUtils.localize("gui." + name));
		}
		
		super.addInformation(itemstack, entityplayer, list, flag);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(!world.isRemote)
		{
			if(getOwner(itemstack) == null)
			{
				setOwner(itemstack, entityplayer.getCommandSenderName());
				entityplayer.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("gui.nowOwn")));
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

		if(entity.worldObj.provider.dimensionId != coords.dimensionId)
		{
			neededEnergy+=10000;
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
		if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("owner"))
		{
			return stack.stackTagCompound.getString("owner");
		}
		
		return null;
	}

	@Override
	public void setOwner(ItemStack stack, String owner) 
	{
		setFrequency(stack, null);
		setAccess(stack, ISecurityTile.SecurityMode.PUBLIC);
		
		if(owner == null || owner.isEmpty())
		{
			stack.stackTagCompound.removeTag("owner");
			return;
		}
		
		stack.stackTagCompound.setString("owner", owner);
	}
	
	@Override
	public boolean hasOwner(ItemStack stack)
	{
		return true;
	}
	
	public ISecurityTile.SecurityMode getAccess(ItemStack stack)
	{
		if(stack.stackTagCompound != null)
		{
			if(stack.stackTagCompound.hasKey("access")) {
				return ISecurityTile.SecurityMode.values()[stack.stackTagCompound.getInteger("access")];
			} else {
				boolean priv = stack.stackTagCompound.getBoolean("private");

				if(priv)
					return ISecurityTile.SecurityMode.PRIVATE;
			}
		}
		
		return ISecurityTile.SecurityMode.PUBLIC;
	}

	public void setAccess(ItemStack stack, ISecurityTile.SecurityMode access)
	{
		if(stack.stackTagCompound == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		stack.stackTagCompound.setInteger("access", access.ordinal());
	}
	
	public String getFrequency(ItemStack stack) 
	{
		if(stack.stackTagCompound != null && stack.stackTagCompound.hasKey("frequency"))
		{
			return stack.stackTagCompound.getString("frequency");
		}
		
		return null;
	}

	public void setFrequency(ItemStack stack, String frequency) 
	{
		if(stack.stackTagCompound == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}
		
		if(frequency == null || frequency.isEmpty())
		{
			stack.stackTagCompound.removeTag("frequency");
			return;
		}
		
		stack.stackTagCompound.setString("frequency", frequency);
	}
}
