package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.IFilterAccess;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemFilterCard extends ItemMekanism
{
	public ItemFilterCard()
	{
		super();
		
		setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		
		list.add(EnumColor.GREY + MekanismUtils.localize("gui.data") + ": " + EnumColor.INDIGO + MekanismUtils.localize(getDataType(itemstack)));
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			
			if(tileEntity instanceof IFilterAccess)
			{
				if(player.isSneaking())
				{
					NBTTagCompound data = ((IFilterAccess)tileEntity).getFilterData(new NBTTagCompound());
					
					if(data != null)
					{
						data.setString("dataType", ((IFilterAccess)tileEntity).getDataType());
						setData(stack, data);
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + MekanismUtils.localize("tooltip.filterCard.got").replaceAll("%s", EnumColor.INDIGO + MekanismUtils.localize(data.getString("dataType")) + EnumColor.GREY)));
					}
					
					return true;
				}
				else if(getData(stack) != null)
				{
					if(((IFilterAccess)tileEntity).getDataType().equals(getDataType(stack)))
					{
						((IFilterAccess)tileEntity).setFilterData(getData(stack));
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.DARK_GREEN + MekanismUtils.localize("tooltip.filterCard.set").replaceAll("%s", EnumColor.INDIGO + MekanismUtils.localize(getDataType(stack)) + EnumColor.DARK_GREEN)));
						setData(stack, null);
					}
					else {
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.RED + MekanismUtils.localize("tooltip.filterCard.unequal") + "."));
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void setData(ItemStack itemstack, NBTTagCompound data)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		if(data != null)
		{
			itemstack.stackTagCompound.setTag("data", data);
		}
		else {
			itemstack.stackTagCompound.removeTag("data");
		}
	}

	public NBTTagCompound getData(ItemStack itemstack)
	{
		if(itemstack.stackTagCompound == null)
		{
			return null;
		}
		
		NBTTagCompound data = itemstack.stackTagCompound.getCompoundTag("data");
		
		if(data.hasNoTags())
		{
			return null;
		}
		else {
			return itemstack.stackTagCompound.getCompoundTag("data");
		}
	}

	public String getDataType(ItemStack itemstack)
	{
		NBTTagCompound data = getData(itemstack);
		
		if(data != null)
		{
			return data.getString("dataType");
		}
		
		return "gui.none";
	}
}
