package mekanism.common.content.boiler;

import mekanism.api.Coord4D;
import mekanism.common.content.tank.SynchronizedTankData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;

public class BoilerCache extends MultiblockCache<SynchronizedBoilerData>
{
	public ItemStack[] inventory = new ItemStack[2];

	public FluidStack water;
	public FluidStack steam;

	public ContainerEditMode editMode = ContainerEditMode.BOTH;

	@Override
	public void apply(SynchronizedBoilerData data)
	{
		data.inventory = inventory;
		data.waterStored = water;
		data.steamStored = steam;
		data.editMode = editMode;
	}

	@Override
	public void sync(SynchronizedBoilerData data)
	{
		inventory = data.inventory;
		water = data.waterStored;
		steam = data.steamStored;
		editMode = data.editMode;
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		editMode = ContainerEditMode.values()[nbtTags.getInteger("editMode")];

		NBTTagList tagList = nbtTags.getTagList("Items", NBT.TAG_COMPOUND);
		inventory = new ItemStack[2];

		for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
		{
			NBTTagCompound tagCompound = (NBTTagCompound)tagList.getCompoundTagAt(tagCount);
			byte slotID = tagCompound.getByte("Slot");

			if(slotID >= 0 && slotID < 2)
			{
				inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
			}
		}

		if(nbtTags.hasKey("cachedWater"))
		{
			water = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedWater"));
		}
		if(nbtTags.hasKey("cachedSteam"))
		{
			steam = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedSteam"));
		}
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("editMode", editMode.ordinal());

		NBTTagList tagList = new NBTTagList();

		for(int slotCount = 0; slotCount < 2; slotCount++)
		{
			if(inventory[slotCount] != null)
			{
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setByte("Slot", (byte)slotCount);
				inventory[slotCount].writeToNBT(tagCompound);
				tagList.appendTag(tagCompound);
			}
		}

		nbtTags.setTag("Items", tagList);

		if(water != null)
		{
			nbtTags.setTag("cachedWater", water.writeToNBT(new NBTTagCompound()));
		}
		if(steam != null)
		{
			nbtTags.setTag("cachedSteam", steam.writeToNBT(new NBTTagCompound()));
		}
	}
}
