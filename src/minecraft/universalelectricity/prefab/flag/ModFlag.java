package universalelectricity.prefab.flag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import universalelectricity.core.vector.Vector3;

public class ModFlag extends FlagBase
{
	/**
	 * An array of world flag data. Each representing a world.
	 */
	private final List<FlagWorld> flagWorlds = new ArrayList<FlagWorld>();

	/**
	 * Initiates a new mod flag data and loads everything from NBT into memory. Only exists server
	 * side.
	 * 
	 * @param nbt
	 */
	public ModFlag(NBTTagCompound nbt)
	{
		this.readFromNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		// A list containing all dimension ID and data within it.
		Iterator dimensions = nbt.getTags().iterator();

		while (dimensions.hasNext())
		{
			NBTTagCompound dimensionCompound = (NBTTagCompound) dimensions.next();

			try
			{
				int dimensionID = Integer.parseInt(dimensionCompound.getName().replace("dim_", ""));
				World world = DimensionManager.getWorld(dimensionID);
				FlagWorld flagWorld = new FlagWorld(world);
				flagWorld.readFromNBT(dimensionCompound);
				this.flagWorlds.add(flagWorld);
			}
			catch (Exception e)
			{
				System.out.println("Mod Flag: Failed to read dimension data: " + dimensionCompound.getName());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		for (FlagWorld worldData : this.flagWorlds)
		{
			try
			{
				nbt.setTag("dim_" + worldData.world.provider.dimensionId, worldData.getNBT());
			}
			catch (Exception e)
			{
				System.out.println("Mod Flag: Failed to save world flag data: " + worldData.world);
				e.printStackTrace();
			}
		}
	}

	public FlagWorld getFlagWorld(World world)
	{
		FlagWorld worldData = null;

		if (world != null)
		{
			for (FlagWorld data : this.flagWorlds)
			{
				if (data.world != null && data.world.provider != null)
				{
					if (data.world.provider.dimensionId == world.provider.dimensionId)
					{
						worldData = data;
						break;
					}
				}
			}

			// If data is null, create it.
			if (worldData == null)
			{
				worldData = new FlagWorld(world);
				this.flagWorlds.add(worldData);
			}
		}

		return worldData;
	}

	public boolean containsValue(World world, String flagName, String checkValue, Vector3 position)
	{
		return this.getFlagWorld(world).containsValue(flagName, checkValue, position);
	}

	public List<FlagWorld> getFlagWorlds()
	{
		return this.flagWorlds;
	}
}
