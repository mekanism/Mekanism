package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemConfigurationCard extends ItemMekanism
{
	public ItemConfigurationCard()
	{
		super();
		
		setMaxStackSize(1);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		
		list.add(EnumColor.GREY + LangUtils.localize("gui.data") + ": " + EnumColor.INDIGO + LangUtils.localize(getDataType(itemstack)));
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			
			if(tileEntity instanceof IConfigCardAccess)
			{
				if(player.isSneaking())
				{
					NBTTagCompound data = getBaseData(tileEntity);
					
					if(tileEntity instanceof ISpecialConfigData)
					{
						data = ((ISpecialConfigData)tileEntity).getConfigurationData(data);
					}
					
					if(data != null)
					{
						data.setString("dataType", getNameFromTile(tileEntity));
						setData(stack, data);
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + LangUtils.localize("tooltip.configurationCard.got").replaceAll("%s", EnumColor.INDIGO + LangUtils.localize(data.getString("dataType")) + EnumColor.GREY)));
					}
					
					return true;
				}
				else if(getData(stack) != null)
				{
					if(getNameFromTile(tileEntity).equals(getDataType(stack)))
					{
						setBaseData(getData(stack), tileEntity);
						
						if(tileEntity instanceof ISpecialConfigData)
						{
							((ISpecialConfigData)tileEntity).setConfigurationData(getData(stack));
						}
						
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.DARK_GREEN + LangUtils.localize("tooltip.configurationCard.set").replaceAll("%s", EnumColor.INDIGO + LangUtils.localize(getDataType(stack)) + EnumColor.DARK_GREEN)));
						setData(stack, null);
					}
					else {
						player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.RED + LangUtils.localize("tooltip.configurationCard.unequal") + "."));
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	private NBTTagCompound getBaseData(TileEntity tile)
	{
		NBTTagCompound nbtTags = new NBTTagCompound();
		
		if(tile instanceof IRedstoneControl)
		{
			nbtTags.setInteger("controlType", ((IRedstoneControl)tile).getControlType().ordinal());
		}
		
		if(tile instanceof ISideConfiguration)
		{
			((ISideConfiguration)tile).getConfig().write(nbtTags);
			((ISideConfiguration)tile).getEjector().write(nbtTags);
		}
		
		return nbtTags;
	}
	
	private void setBaseData(NBTTagCompound nbtTags, TileEntity tile)
	{
		if(tile instanceof IRedstoneControl)
		{
			((IRedstoneControl)tile).setControlType(RedstoneControl.values()[nbtTags.getInteger("controlType")]);
		}
		
		if(tile instanceof ISideConfiguration)
		{
			((ISideConfiguration)tile).getConfig().read(nbtTags);
			((ISideConfiguration)tile).getEjector().read(nbtTags);
		}
	}
	
	private String getNameFromTile(TileEntity tile)
	{
		String ret = Integer.toString(tile.hashCode());
		
		if(tile instanceof TileEntityContainerBlock)
		{
			ret = tile.getBlockType().getUnlocalizedName() + "." + ((TileEntityContainerBlock)tile).fullName + ".name";
		}
		
		if(tile instanceof ISpecialConfigData)
		{
			ret = ((ISpecialConfigData)tile).getDataType();
		}
		
		return ret;
	}
	
	public void setData(ItemStack itemstack, NBTTagCompound data)
	{
		if(data != null)
		{
			ItemDataUtils.setCompound(itemstack, "data", data);
		}
		else {
			ItemDataUtils.removeData(itemstack, "data");
		}
	}

	public NBTTagCompound getData(ItemStack itemstack)
	{
		NBTTagCompound data = ItemDataUtils.getCompound(itemstack, "data");
		
		if(data.hasNoTags())
		{
			return null;
		}
		else {
			return ItemDataUtils.getCompound(itemstack, "data");
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
