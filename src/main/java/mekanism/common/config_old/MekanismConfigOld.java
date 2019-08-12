package mekanism.common.config_old;

import javax.annotation.Nullable;
import mekanism.generators.common.MekanismGenerators;
import mekanism.tools.common.MekanismTools;
import net.minecraftforge.fml.ModList;

public class MekanismConfigOld {

    private static MekanismConfigOld LOCAL = new MekanismConfigOld();
    private static MekanismConfigOld SERVER = null;

    /**
     * Current config, for use when querying the config
     *
     * @return when connected to a server, SERVER, otherwise LOCAL.
     */
    public static MekanismConfigOld current() {
        return SERVER != null ? SERVER : LOCAL;
    }

    /**
     * Local config, mainly for the config GUI
     *
     * @return LOCAL
     */
    public static MekanismConfigOld local() {
        return LOCAL;
    }

    public static void setSyncedConfig(@Nullable MekanismConfigOld newConfig) {
        if (newConfig != null) {
            //newConfig.client = LOCAL.client;
        }
        SERVER = newConfig;
    }

    public GeneralConfig general = new GeneralConfig();

    public ToolsConfig tools = ModList.get().isLoaded(MekanismTools.MODID) ? new ToolsConfig() : null;
}