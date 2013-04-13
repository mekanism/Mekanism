package universalelectricity.prefab.flag;

import net.minecraft.nbt.NBTTagCompound;

public class Flag extends FlagBase
{
	/**
	 * The region in which this flag has affect in.
	 */
	public FlagRegion flagRegion;

	public String name;

	public String value;

	public Flag(FlagRegion flagRegion)
	{
		this.flagRegion = flagRegion;
	}

	public Flag(FlagRegion flagRegion, String name, String value)
	{
		this(flagRegion);
		this.name = name;
		this.value = value;

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		this.name = nbt.getString("name");
		this.value = nbt.getString("value");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("name", this.name);
		nbt.setString("value", this.value);

	}
}
