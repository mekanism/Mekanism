package universalelectricity.prefab.flag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.vector.Region3;

/**
 * A defined region.
 * 
 * @author Calclavia
 * 
 */
public class FlagRegion extends FlagBase
{
	/**
	 * The region in which this flag has affect in.
	 */
	public FlagWorld flagWorld;

	public String name;
	public Region3 region;
	private final List<Flag> flags = new ArrayList<Flag>();

	public FlagRegion(FlagWorld worldFlagData)
	{
		this.flagWorld = worldFlagData;
	}

	public FlagRegion(FlagWorld flagWorld, String name, Region3 region)
	{
		this.flagWorld = flagWorld;
		this.name = name;
		this.region = region;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		this.name = nbt.getName();

		Vector3 startVector = Vector3.readFromNBT(nbt.getCompoundTag("min"));
		Vector3 endVector = Vector3.readFromNBT(nbt.getCompoundTag("max"));

		this.region = new Region3(startVector, endVector);

		/**
		 * Child Data
		 */
		NBTTagList flagList = nbt.getTagList("flags");

		for (int i = 0; i < flagList.tagCount(); i++)
		{
			NBTTagCompound childNode = (NBTTagCompound) flagList.tagAt(i);

			try
			{
				Flag flag = new Flag(this);
				flag.readFromNBT(childNode);
				this.flags.add(flag);
			}
			catch (Exception e)
			{
				System.out.println("Mod Flag: Failed to read flag data: " + childNode.getName());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setName(this.name);

		nbt.setTag("min", this.region.min.writeToNBT(new NBTTagCompound()));
		nbt.setTag("max", this.region.max.writeToNBT(new NBTTagCompound()));

		NBTTagList flagList = new NBTTagList();

		for (Flag flag : this.getFlags())
		{
			try
			{
				flagList.appendTag(flag.getNBT());
			}
			catch (Exception e)
			{
				System.out.println("Failed to save world flag data: " + flag.name);
				e.printStackTrace();
			}
		}

		nbt.setTag("flags", flagList);
	}

	public boolean containsValue(String flagName, String checkValue, Vector3 position)
	{
		for (Flag flag : this.flags)
		{
			if (flag.name.equalsIgnoreCase(flagName) && flag.value.equalsIgnoreCase(checkValue))
			{
				return true;
			}
		}

		return false;
	}

	public boolean setFlag(String flagName, String value)
	{
		this.removeFlag(flagName);

		if (value != null && value != "")
		{
			if (!containsFlag(flagName))
			{
				return this.flags.add(new Flag(this, flagName, value));
			}
		}

		return false;
	}

	public boolean containsFlag(String flagName)
	{
		for (Flag region : this.flags)
		{
			if (region.name.equalsIgnoreCase(flagName))
			{
				return true;
			}
		}
		return false;
	}

	public boolean removeFlag(String flagName)
	{
		for (Flag region : this.flags)
		{
			if (region.name.equalsIgnoreCase(flagName))
			{
				this.flags.remove(region);
				return true;
			}
		}

		return false;
	}

	public List<Flag> getFlags()
	{
		Iterator<Flag> it = this.flags.iterator();

		while (it.hasNext())
		{
			Flag flag = it.next();

			if (flag == null)
			{
				it.remove();
				continue;
			}

			if (flag.name == null || flag.name == "")
			{
				it.remove();
				continue;
			}
		}

		return this.flags;
	}

	public void edit(Vector3 position, int radius)
	{
		Vector3 minVec = new Vector3(position.intX() - radius, 0, position.intZ() - radius);
		Vector3 maxVec = new Vector3(position.intX() + radius, this.flagWorld.world.getHeight(), position.intZ() + radius);
		this.region = new Region3(minVec, maxVec);
	}
}
