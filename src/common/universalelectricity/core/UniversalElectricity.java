package universalelectricity.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.MapColor;
import net.minecraft.src.Material;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeVersion;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;

/**
 * Instructions for using the Universal Electricity API.
 * 
 * The less you include of the API, the more compatible your
 * mod will be for future releases of Universal Electricity.
 * 
 * REQUIRED PACKAGES:
 * "universalelectricity"
 * "universalelectricity.electricity"
 * "universalelectricity.implements" - Some interfaces can be removed if not needed.
 * 
 * The rest of the classes should be removed if you are not going to use them.
 * 
 * @author Calclavia
 * 
 */
public class UniversalElectricity
{
	/**
	 * The version of the Universal Electricity API.
	 */
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final int REVISION_VERSION = 0;
	public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION;

	/**
	 * The Universal Electricity configuration file.
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "UniversalElectricity/UniversalElectricity.cfg"));

	/**
	 * Conversion ratios between Buildcraft and Industrialcraft energy.
	 */
	// EU to Watts ratio
	public static final float IC2_RATIO = (float) UEConfig.getConfigData(CONFIGURATION, "IndustrialCraft Conversion Ratio", 7);
	// MJ to Watts ratio.
	public static final float BC3_RATIO = (float) UEConfig.getConfigData(CONFIGURATION, "BuildCraft Conversion Ratio", 85);;
	public static final float TO_IC2_RATIO = 1 / IC2_RATIO;
	public static final float TO_BC_RATIO = 1 / BC3_RATIO;

	/**
	 * Use this material for all your machine blocks. It can be breakable by
	 * hand.
	 */
	public static final Material machine = new Material(MapColor.ironColor);

	public static final List<Object> mods = new ArrayList<Object>();

	/**
	 * You must register your mod with Universal Electricity. Call this in your
	 * mod's pre-initialization stage.
	 */
	public static void register(Object mod, int major, int minor, int revision, boolean strict)
	{
		if (MAJOR_VERSION != major) { throw new RuntimeException("Universal Electricity wrong version! Require v" + major + "." + minor + "." + revision); }

		if (MINOR_VERSION < minor) { throw new RuntimeException("Universal Electricity minor version is too old! Require v" + major + "." + minor + "." + revision); }

		if (REVISION_VERSION < revision)
		{
			if (strict)
			{
				throw new RuntimeException("Universal Electricity is too old! Require v" + major + "." + minor + "." + revision);
			}
			else
			{
				FMLLog.warning("Universal Electricity is not the specified version. Odd things might happen. Recommend to have v" + major + "." + minor + "." + revision);
			}
		}

		mods.add(mod);
		
		FMLLog.fine(mod.getClass().getSimpleName()+" has been registered to Universal Electricity.");
		
		UELoader.INSTANCE.initiate();
	}

	/**
	 * A function that allows you to lock your mod to a specific version of
	 * Forge.
	 */
	public static void forgeLock(int major, int minor, int revision, boolean strict)
	{
		if (ForgeVersion.getMajorVersion() != major) { throw new RuntimeException("Universal Electricity: Wrong Minecraft Forge version! Require " + major + "." + minor + "." + revision); }

		if (ForgeVersion.getMinorVersion() < minor) { throw new RuntimeException("Universal Electricity: Minecraft Forge minor version is too old! Require " + major + "." + minor + "." + revision); }

		if (ForgeVersion.getRevisionVersion() < revision)
		{
			if (strict)
			{
				throw new RuntimeException("Universal Electricity: Minecraft Forge revision version is too old! Require " + major + "." + minor + "." + revision);
			}
			else
			{
				System.out.println("Universal Electricity Warning: Minecraft Forge is not the specified version. Odd things might happen. Require " + major + "." + minor + "." + revision);
			}
		}
	}

	public static void forgeLock(int major, int minor, int revision)
	{
		forgeLock(major, minor, revision, false);
	}
}
