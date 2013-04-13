package universalelectricity.prefab.flag;

import net.minecraft.nbt.NBTTagCompound;

public abstract class FlagBase
{
	public abstract void readFromNBT(NBTTagCompound nbt);

	public abstract void writeToNBT(NBTTagCompound nbt);

	public NBTTagCompound getNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		try
		{
			this.writeToNBT(nbt);
		}
		catch (Exception e)
		{
			System.out.println("Failed to read flag");
			e.printStackTrace();
		}

		return nbt;
	}
}
