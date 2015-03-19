package mekanism.common.content.matrix;

import mekanism.common.multiblock.MultiblockCache;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class MatrixCache extends MultiblockCache<SynchronizedMatrixData>
{
	public ItemStack[] inventory = new ItemStack[2];
	
	@Override
	public void apply(SynchronizedMatrixData data) 
	{
		data.inventory = inventory;
	}

	@Override
	public void sync(SynchronizedMatrixData data) 
	{
		inventory = data.inventory;
	}

	@Override
	public void load(NBTTagCompound nbtTags) 
	{
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
	}

	@Override
	public void save(NBTTagCompound nbtTags) 
	{
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
	}
}
