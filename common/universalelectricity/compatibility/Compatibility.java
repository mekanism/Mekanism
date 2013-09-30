package universalelectricity.compatibility;

import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.electricity.NetworkLoader;
import cpw.mods.fml.common.Loader;

/**
 * The Universal Electricity compatibility module allows your mod to be compatible with most major
 * power systems in Minecraft.
 * 
 * @author Calclavia, Micdoodle
 */
public class Compatibility
{
	/** Version of build craft api compiled with */
	public static String BCx_VERSION = "@BCxVersion@";
	/** Version of industrial craft api compiled with */
	public static String ICx_VERSION = "@ICxVersion@";
	/** Version of thermal expansion api compiled with */
	public static String TEx_VERSION = "@TExVersion@";

	/** Has the initiate method been called */
	public static boolean INIT = false;

	/** Ratio of Build craft(MJ) power to UE power(KW). Multiply BC3 power by this to convert to UE */
	public static float BC3_RATIO = 1;
	/**
	 * Ratio of Industrial craft(EU) power to UE power(KW). Multiply IC2 power by this to convert to
	 * UE
	 */
	public static float IC2_RATIO = 0.04f;

	/**
	 * Ratio of UE power(KW) to Industrial craft(EU) power. Multiply UE power by this to convert it
	 * to IC2 power
	 */
	public static float TO_IC2_RATIO = 1 / IC2_RATIO;
	/**
	 * Ratio of UE power(KW) to Build craft(MJ) power. Multiply UE power by this to convert it to
	 * BC3 power
	 */
	public static float TO_BC_RATIO = 1 / BC3_RATIO;

	/** You must call this function to enable the Universal Network module. */
	public static void initiate()
	{
		if (!INIT)
		{
			/** Outputs basic version information */
			System.out.println("[UniversalElectricity] Loading compatibility API version " + UniversalElectricity.VERSION);
			System.out.println("[UniversalElectricity] Compiled with IndustrialCraft API version " + Compatibility.ICx_VERSION);
			System.out.println("[UniversalElectricity] Compiled with BuildCraft API version " + Compatibility.BCx_VERSION);
			System.out.println("[UniversalElectricity] Compiled with ThermalExpansion  API version " + Compatibility.TEx_VERSION);

			/** Loads the configuration and sets all the values. */
			UniversalElectricity.CONFIGURATION.load();
			IC2_RATIO = (float) UniversalElectricity.CONFIGURATION.get("Compatiblity", "IndustrialCraft Conversion Ratio", IC2_RATIO).getDouble(IC2_RATIO);
			BC3_RATIO = (float) UniversalElectricity.CONFIGURATION.get("Compatiblity", "BuildCraft Conversion Ratio", BC3_RATIO).getDouble(BC3_RATIO);
			TO_IC2_RATIO = 1 / IC2_RATIO;
			TO_BC_RATIO = 1 / BC3_RATIO;
			UniversalElectricity.CONFIGURATION.save();

			NetworkLoader.setNetworkClass(UniversalNetwork.class);
		}
	}

	/** Checks using the FML loader too see if IC2 is loaded */
	public static boolean isIndustrialCraft2Loaded()
	{
		return Loader.isModLoaded("IC2");
	}

	/** Checks using the FML loader too see if BC3 is loaded */
	public static boolean isBuildcraftLoaded()
	{
		return Loader.isModLoaded("BuildCraft|Energy");
	}

	// TODO add Thermal expansion isLoaded check
}
