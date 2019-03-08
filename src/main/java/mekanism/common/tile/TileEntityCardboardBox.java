package mekanism.common.tile;

import mekanism.common.block.BlockCardboardBox.BlockData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TileEntityCardboardBox extends TileEntity
{
	public BlockData storedData;

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(nbtTags.hasKey("storedData"))
		{
			storedData = BlockData.read(nbtTags.getCompoundTag("storedData"));
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(storedData != null)
		{
			nbtTags.setTag("storedData", storedData.write(new NBTTagCompound()));
		}
		
		return nbtTags;
	}
}
