package universalelectricity.prefab.flag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.vector.Region3;

/**
 * Data structure for world protection.
 * 
 * @author Calclavia
 * 
 */
public class FlagWorld extends FlagBase
{
	public static final String GLOBAL_REGION = "dimension";

	public World world;
	private final List<FlagRegion> regions = new ArrayList<FlagRegion>();

	public FlagWorld(World world)
	{
		this.world = world;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		// A list containing all flags within it for this world.
		Iterator<NBTTagCompound> childCompounds = nbt.getTags().iterator();

		while (childCompounds.hasNext())
		{
			NBTTagCompound childCompound = childCompounds.next();

			try
			{
				FlagRegion flagRegion = new FlagRegion(this);
				flagRegion.readFromNBT(childCompound);
				this.regions.add(flagRegion);
			}
			catch (Exception e)
			{
				System.out.println("Mod Flag: Failed to read flag data: " + childCompound.getName());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		for (FlagRegion region : this.regions)
		{
			try
			{
				NBTTagCompound flagCompound = new NBTTagCompound();
				region.writeToNBT(flagCompound);
				nbt.setTag(region.name, flagCompound);
			}
			catch (Exception e)
			{
				System.out.println("Failed to save world flag data: " + region.name);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets all the flags that have an effect in this position.
	 * 
	 * @param position
	 * @return
	 */
	public List<Flag> getFlagsInPosition(Vector3 position)
	{
		List<Flag> returnFlags = new ArrayList<Flag>();

		for (FlagRegion flagRegion : this.regions)
		{
			if (flagRegion.region.isIn(position) || flagRegion.name.equalsIgnoreCase(GLOBAL_REGION))
			{
				for (Flag flag : flagRegion.getFlags())
				{
					returnFlags.add(flag);
				}
			}
		}

		return returnFlags;
	}

	/**
	 * Gets all the values of the flags in this position.
	 */
	public List<String> getValues(String flagName, Vector3 position)
	{
		List<String> values = new ArrayList<String>();

		for (Flag flag : this.getFlagsInPosition(position))
		{
			values.add(flag.value);
		}

		return values;
	}

	/**
	 * Checks if there is a flag in this position that has a specific value.
	 */
	public boolean containsValue(String flagName, String checkValue, Vector3 position)
	{
		for (Flag flag : this.getFlagsInPosition(position))
		{
			if (flag.name.equalsIgnoreCase(flagName) && flag.value.equalsIgnoreCase(checkValue))
			{
				return true;
			}
		}

		return false;
	}

	public boolean addRegion(String name, Vector3 position, int radius)
	{
		Vector3 minVec = new Vector3(position.intX() - radius, 0, position.intZ() - radius);
		Vector3 maxVec = new Vector3(position.intX() + radius, this.world.getHeight(), position.intZ() + radius);

		return this.regions.add(new FlagRegion(this, name, new Region3(minVec, maxVec)));
	}

	public FlagRegion getRegion(String name)
	{
		for (FlagRegion region : this.regions)
		{
			if (region.name.equals(name))
			{
				return region;
			}
		}
		return null;
	}

	/**
	 * Gets all regions that intersect this point.
	 */
	public List<FlagRegion> getRegions(Vector3 position)
	{
		List<FlagRegion> returnRegions = new ArrayList<FlagRegion>();
		for (FlagRegion region : this.regions)
		{
			if (region.region.isIn(position))
			{
				returnRegions.add(region);
			}
		}
		return returnRegions;
	}

	public boolean removeRegion(String name)
	{
		for (FlagRegion region : this.regions)
		{
			if (region.name.equals(name))
			{
				this.regions.remove(region);
				return true;
			}
		}

		return false;
	}

	public List<FlagRegion> getRegions()
	{
		Iterator<FlagRegion> it = this.regions.iterator();
		while (it.hasNext())
		{
			FlagRegion region = it.next();

			if (region == null)
			{
				it.remove();
				continue;
			}

			if (region.name == null || region.name == "")
			{
				it.remove();
				continue;
			}
		}

		return this.regions;
	}
}
