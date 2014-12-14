package mekanism.common.item;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemPortableTeleporter extends ItemEnergized
{
	public ItemPortableTeleporter()
	{
		super(1000000);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		entityplayer.openGui(Mekanism.instance, 14, world, 0, 0, 0);
		return itemstack;
	}

	public int calculateEnergyCost(Entity entity, Coord4D coords)
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

		int distance = (int)entity.getDistance(coords.getPos().getX(), coords.getPos().getY(), coords.getPos().getZ());

		neededEnergy+=(distance*10);

		return neededEnergy;
	}

	public String getStatusAsString(int i)
	{
		switch(i)
		{
			case 0:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.notReady");
			case 1:
				return EnumColor.DARK_GREEN + MekanismUtils.localize("gui.teleporter.ready");
			case 2:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.needsEnergy");
			case 3:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.exceeds");
			case 4:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.noLink");
			default:
				return EnumColor.DARK_RED + MekanismUtils.localize("gui.teleporter.notReady");
		}
	}

	public int getStatus(ItemStack itemstack)
	{
		if(itemstack.getTagCompound() == null)
		{
			return 0;
		}

		return itemstack.getTagCompound().getInteger("status");
	}

	public void setStatus(ItemStack itemstack, int status)
	{
		if(itemstack.getTagCompound() == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.getTagCompound().setInteger("status", status);
	}

	public int getDigit(ItemStack itemstack, int index)
	{
		if(itemstack.getTagCompound() == null)
		{
			return 0;
		}

		return itemstack.getTagCompound().getInteger("digit"+index);
	}

	public void setDigit(ItemStack itemstack, int index, int digit)
	{
		if(itemstack.getTagCompound() == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.getTagCompound().setInteger("digit"+index, digit);
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
}
