package mekanism.common.item;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.entity.EntityRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRobit extends ItemEnergized implements ISustainedInventory
{
	public ItemRobit()
	{
		super(100000);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);

		list.add(EnumColor.INDIGO + MekanismUtils.localize("tooltip.name") + ": " + EnumColor.GREY + getName(itemstack));
		list.add(EnumColor.AQUA + MekanismUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float posX, float posY, float posZ)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if(tileEntity instanceof TileEntityChargepad)
		{
			TileEntityChargepad chargepad = (TileEntityChargepad)tileEntity;
			if(!chargepad.isActive)
			{
				if(!world.isRemote)
				{
					EntityRobit robit = new EntityRobit(world, x+0.5, y+0.1, z+0.5);

					robit.setHome(Coord4D.get(chargepad));
					robit.setEnergy(getEnergy(itemstack));
					robit.setOwner(entityplayer.getCommandSenderName());
					robit.setInventory(getInventory(itemstack));
					robit.setName(getName(itemstack));

					world.spawnEntityInWorld(robit);
				}

				entityplayer.setCurrentItemOrArmor(0, null);

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}

	public void setName(ItemStack itemstack, String name)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setString("name", name);
	}

	public String getName(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return "Robit";
		}

		String name = itemstack.stackTagCompound.getString("name");

		return name.equals("") ? "Robit" : name;
	}

	@Override
	public void setInventory(NBTTagList nbtTags, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.stackTagCompound.setTag("Items", nbtTags);
		}
	}

	@Override
	public NBTTagList getInventory(Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];

			if(itemStack.stackTagCompound == null)
			{
				return null;
			}

			return itemStack.stackTagCompound.getTagList("Items", 10);
		}

		return null;
	}
}
