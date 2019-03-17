package mekanism.common.config;

import mekanism.generators.common.MekanismGenerators;
import mekanism.tools.common.MekanismTools;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;

public class MekanismConfig
{
	private static MekanismConfig LOCAL = new MekanismConfig();
	private static MekanismConfig SERVER = null;

	/**
	 * Current config, for use when querying the config
	 *
	 * @return when connected to a server, SERVER, otherwise LOCAL.
	 */
	public static MekanismConfig current()
	{
		return SERVER != null ? SERVER : LOCAL;
	}

	/**
	 * Local config, mainly for the config GUI
	 * @return LOCAL
	 */
	public static MekanismConfig local()
	{
		return LOCAL;
	}

	public static void setSyncedConfig(@Nullable MekanismConfig newConfig)
	{
		if (newConfig != null)
			newConfig.client = LOCAL.client;
		SERVER = newConfig;
	}

	public GeneralConfig general = new GeneralConfig();
	public ClientConfig client = new ClientConfig();
	public UsageConfig usage = new UsageConfig();

	public GeneratorsConfig generators = Loader.isModLoaded(MekanismGenerators.MODID) ? new GeneratorsConfig() : null;

	public ToolsConfig tools = Loader.isModLoaded(MekanismTools.MODID) ? new ToolsConfig() : null;

}
