package micdoodle8.mods.galacticraft.api.world;

/**
 * Used to change the solar multiplier of certain world providers
 * 
 * If you have a solar feature in your mod, check whether the world's provider
 * inherits this class and multiply the solar generation by the solar multiplier
 * double
 * 
 * for example:
 * 
 * if (worldObj.provider instanceof ISolarLevel) solarStrength *= ((ISolarLevel)
 * worldObj.provider).getSolarEnergyMultiplier();
 */
public interface ISolarLevel
{
	public double getSolarEnergyMultiplier();
}
