package universalelectricity.core;

import java.io.File;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Loader;

/**
 * General Universal Electricity class.
 * 
 * @author Calclavia
 * 
 */
public class UniversalElectricity
{
	/**
	 * The version of the Universal Electricity API.
	 */
	public static final String MAJOR_VERSION = "@MAJOR@";
	public static final String MINOR_VERSION = "@MINOR@";
	public static final String REVISION_VERSION = "@REVIS@";
	public static final String BUILD_VERSION = "@BUILD@";
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;

	/**
	 * The Universal Electricity configuration file.
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "UniversalElectricity.cfg"));

	/**
	 * Is Universal Electricity currently being voltage sensitive? If so, all machines should
	 * explode under high voltage and react to different amounts of voltage differently.
	 */
	public static boolean isVoltageSensitive = false;

	/**
	 * Set this value to true if your mod contains and has the ability to transfer electricity via
	 * the ElectricityNetwork. Examples would be a mod that adds any sort of wire. This value will
	 * be true as long as there is a way to conduct electricity.
	 */
	@Deprecated
	public static boolean isNetworkActive = false;

	/**
	 * A general material that can be used by machines. Breakable by hand, suitable for machines.
	 */
	public static final Material machine = new Material(MapColor.ironColor);

	private static boolean INIT = false;

	static
	{
		initiate();
	}

	public static void initiate()
	{
		if (!INIT)
		{
			/**
			 * Loads the configuration and sets all the values.
			 */
			CONFIGURATION.load();
			isVoltageSensitive = CONFIGURATION.get("Compatiblity", "Is Voltage Sensitive", isVoltageSensitive).getBoolean(isVoltageSensitive);
			isNetworkActive = CONFIGURATION.get("Compatiblity", "Is Network Active", isNetworkActive).getBoolean(isNetworkActive);
			CONFIGURATION.save();

			try
			{
				MinecraftForge.EVENT_BUS.register(Class.forName("universalelectricity.core.electricity.ElectricityHelper").newInstance());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		INIT = true;
	}
}
