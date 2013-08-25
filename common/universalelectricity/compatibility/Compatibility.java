package universalelectricity.compatibility;

import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.NetworkLoader;
import cpw.mods.fml.common.Loader;

/**
 * The Universal Electricity compatiblity module allows your mod to be compatible with most major
 * power systems in Minecraft.
 * 
 * @author Calclavia, Micdoodle
 * 
 */
public class Compatibility
{
	/**
	 * Universal Electricity measures in Kilowatts.
	 * 
	 * Multiply this to convert foreign energy into UE Joules.
	 */
	public static float BC3_RATIO = 1;
	public static float IC2_RATIO = 0.4f;

	/**
	 * Multiply this to convert UE Joules into foreign energy. The reciprocal conversion ratio.
	 */
	public static float TO_IC2_RATIO = 1 / IC2_RATIO;
	public static float TO_BC_RATIO = 1 / BC3_RATIO;

	/**
	 * You must call this function to enable the Universal Network module.
	 */
	public static void initiate()
	{
		/**
		 * Loads the configuration and sets all the values.
		 */
		UniversalElectricity.CONFIGURATION.load();
		IC2_RATIO = (float) UniversalElectricity.CONFIGURATION.get("Compatiblity", "IndustrialCraft Conversion Ratio", IC2_RATIO).getDouble(IC2_RATIO);
		BC3_RATIO = (float) UniversalElectricity.CONFIGURATION.get("Compatiblity", "BuildCraft Conversion Ratio", BC3_RATIO).getDouble(BC3_RATIO);
		TO_IC2_RATIO = 1 / IC2_RATIO;
		TO_BC_RATIO = 1 / BC3_RATIO;
		UniversalElectricity.CONFIGURATION.save();
		NetworkLoader.setNetworkClass(UniversalNetwork.class);
	}

	public static boolean isIndustrialCraft2Loaded()
	{
		return Loader.isModLoaded("IC2");
	}

	public static boolean isBuildcraftLoaded()
	{
		return Loader.isModLoaded("BuildCraft|Energy");
	}
}
